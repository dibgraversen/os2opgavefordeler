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

			$scope.ous = [];
			$scope.ousAsTree = [];
			$scope.kles = [];

			$scope.setCurrentOrgUnit = setCurrentOrgUnit;
			$scope.toogleChildrenVisibility = toogleChildrenVisibility;
			$scope.modifyKle = modifyKle;
			$scope.openTab = openTab;

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
						console.log("set current orgunit9"); 
					    $scope.currentOrgUnit.displayKles = [];
					    angular.copy($scope.kles, $scope.currentOrgUnit.displayKles);
						//$scope.currentOrgUnit.displayKles = JSON.parse(JSON.stringify($scope.kles));
					    // console.log("$scope.kles", $scope.kles);
					   // console.log("$scope.currentOrgUnit.displayKles", $scope.currentOrgUnit.displayKles);

					//     = Array.from($scope.kles);
						refreshTree($scope.currentOrgUnit.displayKles);
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

			function expand(kle){
				kle.expanded = true;
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
				if(checked){
						orgUnitService.addKle(kle,ou,assignment).then(function(){
							//refreshTree(ou);
						});
				}
				else {
					orgUnitService.removeKle(kle,ou,assignment).then(function(){
							//kle.interest = false;
					});
				}			
			}

			function toogleChildrenVisibility(ou){
				for (var i = 0; i < ou.children.length; i++){
						ou.children[i].visible = !ou.children[i].visible;
				}
				setCurrentOrgUnit(ou.id);		
			}
		}
	})();
