		(function () {
			'use strict';

			angular.module('topicRouter').controller('KleAdminCtrl', KleAdminCtrl);

			KleAdminCtrl.$inject = ['$scope', '$state', '$log', '$interval' , 'topicRouterApi', 'orgUnitService', '$modal'];

			function KleAdminCtrl($scope, $state, $log, $interval, topicRouterApi, orgUnitService, $modal) {
				/* jshint validthis: true */
				var vm = this;
				$scope.$log=$log;
				$scope.currentOrgUnit = { "displayKles" : []};
				$scope.filterStr = "";
				$scope.tabperforming = true;
				$scope.tabinterest=false;

				$scope.currentKLETree = [];
				$scope.ous = [];
				$scope.ousAsTree = [];
				$scope.kles = [];

				$scope.setCurrentOrgUnit = setCurrentOrgUnit;
				$scope.toogleChildrenVisibility = toogleChildrenVisibility;
				$scope.modifyKle = modifyKle;
				$scope.openTab = openTab;
				$scope.toggleChildren = toggleChildren;

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
						refreshTree(kles[i].children);
					}
				}

				function initCurrentKleTree(){
					$scope.currentKLETree = [];

					_.each($scope.currentOrgUnit.displayKles, function(kle){
						var tmp = JSON.parse(JSON.stringify(kle));
						tmp.children = [];
						$scope.currentKLETree.push(tmp);
					});
				}

				function toggleChildren(kle,scope){
					kle.expanded = (kle.expanded === null) ? true : !kle.expanded;

					if(kle.expanded === true){
						refreshTree($scope.currentOrgUnit.displayKles);
						kle.children = 	getSubTree(kle,scope.currentOrgUnit.displayKles);
					}
					if(kle.expanded === false){
						kle.children = null;
					}
				}

				function getSubTree(kle, tree){
					for (var i=0; i < tree.length; i++){
						if(tree[i].number == kle.number){
							if(tree[i].children === null){
								return [];
							}

							var subTree = JSON.parse(JSON.stringify(tree[i].children));
							// set children to null, so that sublevels are discarded
							_.each(subTree, function (treeElement) {
								treeElement.children = null;
							});
							return subTree;
						}
					}

					for (var j=0; j < tree.length; j++){
						if(tree[j].children !== null){
							if(getSubTree(kle,tree[j].children) !== null){
							return getSubTree(kle,tree[j].children);
							}
						}				
					}
					return null;
				}

				function isKleAssigned(ou, kleNumber,assignmentType){
					if(assignmentType === "INTEREST"){
						if (ou.interestKLE === null){
							return false;
						}

						for(var i = 0; i < ou.interestKLE.length; i++){
							if(ou.interestKLE[i]==kleNumber){

								return true;
							}
						}
						return false;
					}
					
					if(assignmentType === "PERFORMING"){
						if (ou.performingKLE === null){
							return false;
						}
						for(var j = 0; j < ou.performingKLE.length; j++){
							if(ou.performingKLE[j]==kleNumber){
								return true;
							}
						}
						return false;
					}
					return false;	
				}

				function openTab(type){
					if(type ==="INTEREST"){
						$scope.tabinterest = true;
						$scope.tabperforming = false;
						console.log("hit interest," + $scope.tabinterest);
					}
					else if(type === "PERFORMING"){
						$scope.tabinterest = false;
						$scope.tabperforming = true;	
						console.log("hit performing," + $scope.tabperforming);

					}
				}

				function modifyKle(checked,kle,ou,assignment){
					console.log("called modifyKle(checked,kle,ou,assignment)");
					console.log("assignment: ", assignment);
					console.log("ou: ", ou);

					if(checked){
							orgUnitService.addKle(kle,ou,assignment).then(function(){
							});
					}
					else {
						orgUnitService.removeKle(kle,ou,assignment).then(function(){
						});
					}			
				}
			
				function toogleChildrenVisibility(ou){
					_.each(ou.children, function(element){
						element.visible = !element.visible;
					});
					
					setCurrentOrgUnit(ou.id);		
				}
			}
		})();
