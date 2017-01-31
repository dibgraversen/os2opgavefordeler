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
		$scope.filterStr="";

		$scope.search = {};
		$scope.search.municipality = $scope.user.municipality;	

		activate();

		function activate() {
			topicRouterApi.getMunicipalities().then(function(municipalities){
				$scope.municipalities = municipalities;
				});

			orgUnitService.getOrgUnits().then(function(orgUnits){
				$scope.ous = orgUnits;
			});
			//refreshUserList();
			
		}

		function filter(filterStr){
			$scope.filterStr=filterStr;
			$log("filterStr value : " + $scope.filterStr);
		}

		function setCurrentOrgUnit(orgUnit){
			$scope.currentOrgUnit = orgUnit;
		}

		// API methods go here 

	}
})();
