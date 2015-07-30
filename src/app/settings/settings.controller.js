(function () {
	'use strict';
	angular.module('topicRouter').controller('SettingsCtrl', SettingsCtrl);

	SettingsCtrl.$inject = ['$scope', '$state', '$log', 'topicRouterApi', '$modal'];

	function SettingsCtrl($scope, $state, $log, topicRouterApi, $modal) {
		/* jshint validthis: true */
		var vm = this;
		$scope.municipalities = [];

		$scope.openCreateMunicipality = openCreateMunicipality;

		activate();

		function activate() {
			$log.info("Settings::activate");
			if(!($scope.user.loggedIn && $scope.user.currentRole.admin)) {
				$log.info("not privileged, redirecting to home");
				$state.go("home");
			}
			topicRouterApi.getMunicipalities().then(function(municipalities){
				$scope.municipalities = municipalities;
			});
		}

		// API

		function openCreateMunicipality(){
			var modalInstance = $modal.open({
				templateUrl: 'app/municipality-admin/add-municipality-modal.html',
				controller: 'AddMunicipalityModalInstanceCtrl',
				size: 'md'
			});

			modalInstance.result.then(function(municipality){
				$scope.municipalities.push(municipality);
			});
		}
	}
})();
