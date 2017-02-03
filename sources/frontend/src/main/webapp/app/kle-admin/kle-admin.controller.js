(function () {
	'use strict';

	angular.module('topicRouter').controller('KleAdminCtrl', KleAdminCtrl);

	KleAdminCtrl.$inject = ['$scope', '$state', '$log', 'topicRouterApi', 'orgUnitService', '$modal'];

	function KleAdminCtrl($scope, $state, $log, topicRouterApi, orgUnitService, $modal) {
		/* jshint validthis: true */
		var vm = this;
		$scope.currentOrgUnit ="";
		$scope.filterStr = "";
		$scope.$log=$log;

		$scope.ous = [];
		$scope.restKles = [];


		$scope.setCurrentOrgUnit = setCurrentOrgUnit;
		$scope.modifyKle = modifyKle;

		activate();

		function activate() {
			orgUnitService.getRestKles().then(function(kles){
				$scope.restKles = kles;
			});

			orgUnitService.getOrgUnits().then(function(ous){
				$scope.ous = JSON.parse(JSON.stringify(ous));
				
				// add 'displayKles' to the ous (this maps to the checkboxes in the ui)
				for(var i = 0; i < $scope.ous.length; i++){
					$scope.ous[i].displayKles = JSON.parse(JSON.stringify($scope.restKles));			
				}
				setCurrentOrgUnit($scope.ous[0]);				
				});		
		}

		function setCurrentOrgUnit(ou){
			$scope.currentOrgUnit = ou;		
		}

		function isKleAssigned(orgunit, kleNumber){
			for(var i = 0; i<orgunit.kles.length; i++){
				if(orgunit.kles[i].number==kleNumber){
					return true;
				}
			}
			return false;
		}

		function expand(kle){
			kle.expanded = true;
		}

		function modifyKle(checked,kle,ou){
			if(checked){
					orgUnitService.addKle(kle,ou).then(function(){
													console.log("assigned kle '" + kle.number + "' to OrgUnit '" +  ou.name + "'");
													addKle(ou,kle);	// updates local ou model 
													kle.assigned=true;
												});
			}
			else {
				orgUnitService.removeKle(kle,ou).then(function(){
													console.log("unassigned kle '" + kle.number + "' to OrgUnit '" +  ou.name + "'");
													removeKle(ou,kle); // updates local ou model 
													kle.assigned=false;
												});
			}
					
		}

		function addKle(ou,kle){
			for(var i; i < $scope.ous.length; i++){
				if($scope.ous[i].id == ou.id){
					$scope.ous[i].kles.push(kle);
				}
			}

			for(var z; z < ou.displayKles.length; z++){
				if(ou.displayKles[z].number == kle.number){
					ou.displayKles[z].assigned=true;
				}
			}

		}

		function removeKle(ou,kle){
			for(var i; i < $scope.ous.length; i++){
				if($scope.ous[i].id == ou.id){
					$scope.ous[i].kles.splice(i,1);
				}
			}
		}
	}
})();
