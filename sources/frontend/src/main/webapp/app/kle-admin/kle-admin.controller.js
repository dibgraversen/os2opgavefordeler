		(function () {
			'use strict';	

			angular.module('topicRouter').controller('KleAdminCtrl', KleAdminCtrl);

			KleAdminCtrl.$inject = ['$scope', '$state', '$log', '$interval' , 'topicRouterApi', 'orgUnitService', 'treeService', '$modal'];

			function KleAdminCtrl($scope, $state, $log, $interval, topicRouterApi, orgUnitService, treeService, $modal) {
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
				$scope.ieIndeterminateCheckboxFix = ieIndeterminateCheckboxFix;
				activate();

				function activate() {
					orgUnitService.getKles().then( function(kles){ $scope.kles = kles;});

					orgUnitService.getOrgUnits().then(
											function(ous){
												$scope.ous = ous;
												setCurrentOrgUnit($scope.ous[0].id);				
											});

					orgUnitService.getOrgUnitsAsTree().then( function(tree){ $scope.ousAsTree = tree; });																		
				}

				function setCurrentOrgUnit(ouId){	
					orgUnitService.getOrgUnit(ouId).then(
						function(ou){
							$scope.currentOrgUnit = ou;
						    $scope.currentOrgUnit.displayKles = [];
						    angular.copy($scope.kles, $scope.currentOrgUnit.displayKles);

							refreshTree($scope.currentOrgUnit.displayKles);
						    addIndeterminateProperty($scope.currentOrgUnit.displayKles);

							// initialize current kle tree
							$scope.currentKLETree = { tree : []};

							_.each($scope.currentOrgUnit.displayKles, function(kle){
								var tmp = JSON.parse(JSON.stringify(kle));
								tmp.children = null;
								$scope.currentKLETree.tree.push(tmp);
							});
					});		
				}

				function refreshTree(nodes){
					if(nodes === null || nodes === "undefined"){
						return ;
					}

					for(var i = 0; i < nodes.length; i++){
						nodes[i].interest = isKleAssigned($scope.currentOrgUnit,nodes[i].number,'INTEREST');
						nodes[i].performing = isKleAssigned($scope.currentOrgUnit,nodes[i].number,'PERFORMING');
						/* jshint loopfunc:true */

						if(nodes[i].performing){
							updateCheckboxStatus(nodes[i], nodes[i].performing, "performing");
						}
						if(nodes[i].interest){
							updateCheckboxStatus(nodes[i], nodes[i].interest, "interesting");
						}
						refreshTree(nodes[i].children);
					}
				}

				function addIndeterminateProperty(kle){
					if(kle === null || kle === undefined){
						return ;
					}

					for(var	 i = 0; i < kle.length; i++){
						/* jshint loopfunc:true */
						kle[i].indeterminatePerforming = childrenContainkles(kle[i].children, "performing");

						kle[i].indeterminateInterest = childrenContainkles(kle[i].children, "interest");
						
						if(kle.children !== null){
							addIndeterminateProperty(kle[i].children);					
						}
					}				
				}

				function childrenContainkles(children, collumn){
					var isIndeterminate = false;
					
					var fullList = treeService.asList(children);

					_.each(fullList, function(anotherItem){
						if(collumn === "performing"){
							if(anotherItem.performing){
								isIndeterminate = true;
							}
						}
						else {
							if (anotherItem.interest){
								isIndeterminate = true;
							}
						}
					});
					
					return isIndeterminate;
				}

				function toggleChildren(kle,scope){
					var kleFromTree = getKle(kle.number,treeService.asList(scope.currentKLETree.tree));
					var kleFromDisplay = getKle(kle.number,treeService.asList(scope.currentOrgUnit.displayKles));
					//addIndeterminateProperty($scope.currentKLETree.kleFromDisplay);

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
				    var kleFromTree = getKle(kle.number,treeService.asList($scope.currentKLETree.tree));
					var kleFromDisplay = getKle(kle.number,treeService.asList($scope.currentOrgUnit.displayKles));

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
						});
					}
					else {
							orgUnitService.removeKle(kle,ou,assignment).then(function(){
							if(assignment==='PERFORMING'){
								kleFromDisplay.performing = checked;
								treeService.remove(kle.number,ou.performingKLE);	
									
								updateCheckboxStatus( kleFromDisplay, checked, "performing");
								updateCheckboxStatus( kleFromTree, checked, "performing");							
							}
							else{
								kleFromDisplay.interest = checked;
								treeService.remove(kle.number,ou.interestKLE);	

								updateCheckboxStatus( kleFromDisplay, checked, "interesting");
								updateCheckboxStatus( kleFromTree, checked, "interesting");
							}								
						});
					}

					updateKlesAssignement(ou, checked);
					addIndeterminateProperty($scope.currentOrgUnit.displayKles);
					addIndeterminateProperty($scope.currentKLETree.tree);
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
							var ouToUpdate = treeService.getOuFromTree(ou, $scope.ousAsTree);
							var ouFromSearchBox = _.find($scope.ous, function(anOu){
													return anOu.id===ou.id;
												});

							if(changeValue === true) {
								ouToUpdate.klesAssigned = true;
								ouFromSearchBox.klesAssigned = true;
							}
							else {
								if ((!ou.interestKLE || ou.interestKLE.length===0) && (!ou.performingKLE || ou.performingKLE.length===0)){
									ouToUpdate.klesAssigned = false;
									ouFromSearchBox.klesAssigned = false;
								}					
							}
				}

				function getKle(kleNumber,list){
					var filtered = _.filter(list, 
											function(item){
												return(item.number === kleNumber);
											});
					return filtered[0];
				}
				
				function toogleChildrenVisibility(ou){
					_.each(ou.children, function(element){
						element.visible = !element.visible;
					});
					ou.expanded =  (ou.expanded === null) ? false : !ou.expanded;
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

  				 function ieIndeterminateCheckboxFix (event) {
				 	var checkbox = event.target;

				 	if(navigator.userAgent.indexOf('MSIE')!==-1 || navigator.appVersion.indexOf('Trident/') > 0){
  					 /* Microsoft Internet Explorer detected in. */

	  					if(checkbox.type.toLowerCase() == 'checkbox'){
							if(checkbox.indeterminate){
								checkbox.checked = true;
								var anEvent = document.createEvent("HTMLEvents");
								anEvent.initEvent("change",true,false);
								checkbox.dispatchEvent(anEvent);
							}			
						}
					}			
				}
			}
		})();
