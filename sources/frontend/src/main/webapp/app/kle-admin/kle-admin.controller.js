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
		$scope.restKles = [];


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
			getRestKles();
		}

		function initDisplayObjects(ous) {
			for (var i = 0; i < ous.length; i++) {
				var ouWithDisplayData = ous[i];
				ouWithDisplayData.displayKles = getDisplayKles(ous[i]);
				$scope.displayOus.push(ouWithDisplayData);				
			}	
		}

		function getDisplayKles(orgunit) {
			var klesWithDisplayData = [];
			
			 for (var i = 0; i < $scope.kles.length; i++){
				var newKle= JSON.parse(JSON.stringify($scope.kles[i]));
				newKle.checked = orgUnitService.containsKle($scope.kles[i],orgunit,$scope.kles[i].assignmentType);
				klesWithDisplayData.push(newKle);				
			}
			
			return klesWithDisplayData;
		}
		

		function getDisplayKlesFromScope(orgunit) {	
			for (var i = 0; i < $scope.displayOus.length; i++){
				if(orgunit.id== $scope.displayOus[i].id) {
					return orgunit.id;
				}				
			}
			
			return null;
		}

		function getRestKles(){
			orgUnitService.getRestKles().then( function(restKles){
				$scope.restKles = restKles;
			});
		}

		function setCurrentOrgUnit(orgUnit){
			$scope.currentOrgUnit = orgUnit;
		}

		function expand(kle){
			kle.expanded = true;
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
