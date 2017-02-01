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

		$scope.kles = [];
		$scope.displayOus = [];

		$scope.setCurrentOrgUnit = setCurrentOrgUnit;
		$scope.getDisplayKlesFromScope = getDisplayKlesFromScope;
		$scope.modifyKle = modifyKle;
		activate();

		function activate() {
			topicRouterApi.getMunicipalities().then(function(municipalities){
				$scope.municipalities = municipalities;
			});

			orgUnitService.getKLEs().then(function(kles){
				$scope.kles = kles;
			});

			orgUnitService.getOrgUnits().then(function(orgUnits){
				setCurrentOrgUnit(orgUnits[0]);
				initDisplayObjects(orgUnits);

			});
		}

		function initDisplayObjects(ous) {
			for (let ou of ous){
				var ouWithDisplayData = ou;
				ouWithDisplayData.displayKles = getDisplayKles(ou);
				$scope.displayOus.push(ouWithDisplayData);				
			}
		}

		function getDisplayKles(orgunit) {
			var klesWithDisplayData = [];
			
			for (let kle of $scope.kles){
				var newKle= JSON.parse(JSON.stringify(kle));
				newKle.checked = orgUnitService.containsKle(kle,orgunit,kle.assignmentType);
				klesWithDisplayData.push(newKle);				
			}
			return klesWithDisplayData;
		}
		

		function getDisplayKlesFromScope(orgunit) {				
			for (let ou of $scope.displayOus){
				if(orgunit.id== ou.id){
					return ou.displayKles;
				}				
			}
			return null;
		}

		function setCurrentOrgUnit(orgUnit){
			$scope.currentOrgUnit = orgUnit;
		}

		function modifyKle(checked,kle,orgunit){
			if(checked){
					orgUnitService.addKle(kle,orgunit).then(function(){
													console.log("added kle");
												});
			}
			else {
				orgUnitService.removeKle(kle,orgunit).then(function(){
													console.log("removed kle");
												});
			}
					
		}
	}
})();
