		(function () {
			'use strict';	

			//var hasKlesCustomFilter = ;

			angular.module('topicRouter').controller('KleAdminCtrl', KleAdminCtrl);

			KleAdminCtrl.$inject = ['$scope', '$state', '$log', '$interval' , 'topicRouterApi', 'orgUnitService', '$modal'];

			function KleAdminCtrl($scope, $state, $log, $interval, topicRouterApi, orgUnitService, $modal) {
			/* jshint validthis: true */
				var vm = this;
				$scope.$log=$log;
				$scope.currentOrgUnit = { "displayKles" : []};
				$scope.filterStr = "";

				$scope.currentKLETree = { tree : [] };
				$scope.ous = [];
				$scope.ousAsTree = [];
				$scope.kles = [];

				$scope.setCurrentOrgUnit = setCurrentOrgUnit;
				$scope.toogleChildrenVisibility = toogleChildrenVisibility;
				$scope.modifyKle = modifyKle;
				$scope.toggleChildren = toggleChildren;
				$scope.filterByNameOrParent = filterByNameOrParent;
				$scope.filterByContainsKles = filterByContainsKles;

				activate();

				function activate() {
					orgUnitService.getKles().then(
											function(kles){
												$scope.kles = kles;
											});

					orgUnitService.getOrgUnits().then(
											function(ous){
												$scope.ous = ous;
												setCurrentOrgUnit($scope.ous[0].id);				
											});

					orgUnitService.getOrgUnitsAsTree().then(
											function(tree){
												$scope.ousAsTree = tree;
											});																		
				}

				function filterByContainsKles(ou){
					return !ou.klesAssigned;
				}

				function filterByNameOrParent(ou) {
					var name = ou.name.toLowerCase();
					var filterStr = $scope.filterStr.toLowerCase();
					var parent = (ou.parentName ===null) ? "" : ou.parentName.toLowerCase();
        			
        			if(_.includes(name, filterStr)){
        				return true;
        			}
        			
        			if(_.includes(parent, filterStr)){
        				return true;
        			}
        			
        			return false;
  				 }

				function setCurrentOrgUnit(ouId){	
					orgUnitService.getOrgUnit(ouId).then(
						function(ou){
							$scope.currentOrgUnit = ou;
						    $scope.currentOrgUnit.displayKles = [];
						    angular.copy($scope.kles, $scope.currentOrgUnit.displayKles);
							refreshTree($scope.currentOrgUnit.displayKles);
							initCurrentKleTree();
					});		
				}

				function refreshTree(kles){
					if(kles === null || kles === "undefined"){
						return ;
					}

					for(var i = 0; i < kles.length; i++){
						kles[i].interest = isKleAssigned($scope.currentOrgUnit,kles[i].number,'INTEREST');
						kles[i].performing = isKleAssigned($scope.currentOrgUnit,kles[i].number,'PERFORMING');
						/* jshint loopfunc:true */

						if(kles[i].performing){
							updateCheckboxStatus(kles[i], kles[i].performing, "performing");
						}
						if(kles[i].interest){
							updateCheckboxStatus(kles[i], kles[i].interest, "interesting");

						}
						refreshTree(kles[i].children);
					}
				}

				function initCurrentKleTree(){
					$scope.currentKLETree = { tree : []};

					_.each($scope.currentOrgUnit.displayKles, function(kle){
						var tmp = JSON.parse(JSON.stringify(kle));
						tmp.children = null;
						$scope.currentKLETree.tree.push(tmp);
					});
				}

				function toggleChildren(kle,scope){
					var kleFromTree = getKle(kle.number,klesAsList(scope.currentKLETree.tree));
					var kleFromDisplay = getKle(kle.number,klesAsList(scope.currentOrgUnit.displayKles));

					kleFromTree.expanded = (kleFromTree.expanded === null) ? true : !kleFromTree.expanded;				
					
					if(kleFromTree.expanded === true) {
						var childrenList = JSON.parse(JSON.stringify(kleFromDisplay.children));
						
						// delete all children 
						_.each(childrenList, function(item) {
							item.children = null;
						});

						kleFromTree.children = childrenList;
					}
					else if(kleFromTree.expanded === false ){
						kleFromTree.children = null;
					}
				}

				function isKleAssigned(ou, kleNumber,assignmentType){
						if(assignmentType === "INTEREST") {
							return _.contains(ou.interestKLE, kleNumber);
						}
						else if (assignmentType === "PERFORMING") {
							return _.contains(ou.performingKLE, kleNumber);
						}
						return false;
				}

				function modifyKle(checked,kle,ou,assignment){
				    var kleFromTree = getKle(kle.number,klesAsList($scope.currentKLETree.tree));
					var kleFromDisplay = getKle(kle.number,klesAsList($scope.currentOrgUnit.displayKles));

					if(checked){
							orgUnitService.addKle(kle,ou,assignment).then(function(){
							if(assignment==='PERFORMING'){
								kleFromDisplay.performing = checked;
								
								updateCheckboxStatus( kleFromDisplay, checked, "performing");
								updateCheckboxStatus( kleFromTree, checked, "performing");						
							}
							else{
								kleFromDisplay.interest = checked;
								ou.interestKLE.push(kle.number);
								
								updateCheckboxStatus( kleFromDisplay, checked, "interesting");
								updateCheckboxStatus( kleFromTree, checked, "interesting");				
							}										

							updateKlesAssignement(ou, checked);
						});
					}
					else {
							orgUnitService.removeKle(kle,ou,assignment).then(function(){
							if(assignment==='PERFORMING'){
								kleFromDisplay.performing = checked;
								removeFromArray(ou.performingKLE, kle.number);	
									
								updateCheckboxStatus( kleFromDisplay, checked, "performing");
								updateCheckboxStatus( kleFromTree, checked, "performing");						
								
							}
							else{
								kleFromDisplay.interest = checked;
								removeFromArray(ou.interestKLE, kle.number);	

								updateCheckboxStatus( kleFromDisplay, checked, "interesting");
								updateCheckboxStatus( kleFromTree, checked, "interesting");
							}		

							updateKlesAssignement(ou, checked);
						});
					}
				}

				function updateCheckboxStatus(item, status, target){
					if(item.children === null) {
						return;
					}
					if (target === 'performing'){
						_.each(item.children, function(anItem) {
							anItem.performingDisabled = status;
						});
					}
					else {
						_.each(item.children, function(anotherItem) {
							anotherItem.interestingDisabled = status;
						});
					}
					_.each(item.children, function(newItem){
						updateCheckboxStatus(newItem, status, target);
					});
				}

				function updateKlesAssignement(ou, changeValue) {
							var ouToUpdate = getOuFromTree(ou);
							var ouFromSearchBox = getOuFromSearchBox(ou);
							if(changeValue === true) {
								ouToUpdate.klesAssigned = true;
								ouFromSearchBox.klesAssigned = true;
							}
							else {
								if ((!ou.interestKLE || ou.interestKLE.length===0) && (!ou.performingKLE || ou.performingKLE.length===0)){
									ouToUpdate.klesAssigned = false;
									ouFromSearchBox.klesAssigned = false;
								}

								// if any of the children elements
							}
				}

				function getOuFromTree(ou){
					var flatOuList = [];

					_.each($scope.ousAsTree, function(item){
						flatOuList = _.union(flatten(item,flatOuList));
					});

					var result = _.find(flatOuList, function(anOu){
						return anOu.id===ou.id;
					});
					return result;
				}

				function getOuFromSearchBox(ou){
					var result = _.find($scope.ous, function(anOu){
						return anOu.id===ou.id;
					});
					return result;
				}


				function getKle(kleNumber,list){
					var filtered = _.filter(list, 
											function(item){
												return(item.number === kleNumber);
											});
					return filtered[0];
				}

				function klesAsList(tree){
					var flat = [];
					_.each(tree, function(item){
						flat = _.union(flatten(item,flat));
					});
					return flat; 
				}

				function flatten(kle,listOfChildren){
					listOfChildren.push(kle);

					if (kle.children !==null) {
						_.each(kle.children, function(item){
								flatten(item,listOfChildren);
						});
					}
					return listOfChildren;
				}
			
				function toogleChildrenVisibility(ou){
					_.each(ou.children, function(element){
						element.visible = !element.visible;
					});
					ou.expanded =  (ou.expanded === null) ? false : !ou.expanded;
				}

				function removeFromArray(list,item){
					for (var i=0; i<list.length; i++){
						if(list[i] == item) {
							list.splice(i,1);
							break;
						}
					}
				}
			}
		})();
