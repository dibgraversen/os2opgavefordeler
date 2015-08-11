(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('AddMunicipalityModalInstanceCtrl', AddMunicipalityModalInstanceCtrl);

	AddMunicipalityModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topicRouterApi'];

	function AddMunicipalityModalInstanceCtrl($scope, $modalInstance, topicRouterApi){
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.createMunicipalityAlerts = [];
		$scope.closeAlert = closeAlert;
		$scope.municipalityName = '';

		activate();

		function activate(){

		}

		function ok(){
			if($scope.municipalityName.length > 0){
				topicRouterApi.createMunicipality($scope.municipalityName).then(function(municipality){
					$modalInstance.close(municipality);
				});
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal taste et navn ;-)'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function addAlert(alert) {
			$scope.createMunicipalityAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.createMunicipalityAlerts.splice(index, 1);
		}
	}
})();
