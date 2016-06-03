(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('DeleteMunicipalityModalInstanceCtrl', DeleteMunicipalityModalInstanceCtrl);

	DeleteMunicipalityModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topicRouterApi', 'municipality'];

	function DeleteMunicipalityModalInstanceCtrl($scope, $modalInstance, topicRouterApi, municipality){
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.deleteMunicipalityAlerts = [];
		$scope.closeAlert = closeAlert;
		$scope.municipalityName = '';
		$scope.municipality = {};
		$scope.titleText = 'Slet kommune';
		$scope.okText = 'Slet';

		activate();

		function activate() {
			if (municipality) {
				$scope.municipalityName = municipality.name;
				$scope.titleText = 'Slet kommune';
				$scope.okText = 'Slet';
			}
		}

		function ok() {
			if (municipality) {
				topicRouterApi.deleteMunicipality(municipality).then(function() {
					$modalInstance.close(municipality);
				});
			}
		}

		function cancel() {
			$modalInstance.dismiss('cancel');
		}

		function addAlert(alert) {
			$scope.deleteMunicipalityAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.deleteMunicipalityAlerts.splice(index, 1);
		}
	}
})();
