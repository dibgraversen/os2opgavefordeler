(function () {
	'use strict';

	angular.module('topicRouter').controller('KleAdminCtrl', KleAdminCtrl);

	KleAdminCtrl.$inject = ['$scope', '$state', '$log', 'topicRouterApi', 'orgUnitService', '$modal'];

	function KleAdminCtrl($scope, $state, $log, topicRouterApi, orgUnitService, $modal) {
		/* jshint validthis: true */
		var vm = this;
		$scope.municipalities = [];
		$scope.users = [];
		$scope.ous = [];
		$scope.setCurrentOrgUnit = setCurrentOrgUnit;
		$scope.containsKle = containsKle;
		$scope.filterStr="";
		$scope.kleChanged = kleChanged;

		$scope.kles = [];

		$scope.search = {};
		$scope.search.municipality = $scope.user.municipality;	

		activate();

		function activate() {
			topicRouterApi.getMunicipalities().then(function(municipalities){
				$scope.municipalities = municipalities;
				});

			orgUnitService.getKLEs().then(function(kles){
				$scope.kles = kles;
			});


			orgUnitService.getOrgUnits().then(function(orgUnits){
				$scope.ous = orgUnits;
				setCurrentOrgUnit(orgUnits[0]);
			});
			//refreshUserList();
			
		}

		function containsKle(kle, orgUnit){
			return orgUnitService.containsKle(kle,orgUnit);
		}

		function setCurrentOrgUnit(orgUnit){
			$scope.currentOrgUnit = orgUnit;
		}

		function addKle(kle,orgunit){
			$log("add kle: " + kle + " , " + orgUnit)
		}

		function removeKle(kle,orgunit){
			$log("remove kle: " + kle + " , " + orgUnit)
		}

		function kleChanged(value){
			if(value){
				console.log("value true");
			}
			else{
				console.log("value false");
			}
		}

		// API methods go here 

	}
})();
