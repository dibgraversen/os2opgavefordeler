(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('AddMunicipalityModalInstanceCtrl', AddMunicipalityModalInstanceCtrl);

	AddMunicipalityModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topicRouterApi', 'municipality'];

	function AddMunicipalityModalInstanceCtrl($scope, $modalInstance, topicRouterApi, municipality){
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.createMunicipalityAlerts = [];
		$scope.closeAlert = closeAlert;
		$scope.municipalityName = '';
		$scope.municipality = {};
		$scope.titleText = 'Opret kommune';
		$scope.okText = 'Opret';

		activate();

		function activate(){
			if(municipality){
				$scope.municipalityName = municipality.name;
				$scope.titleText = 'Rediger kommune';
				$scope.okText = 'Gem';
			}
		}

		function ok(){

			if($scope.municipalityName.length > 0){
				if(municipality){
					municipality.name = $scope.municipalityName;
					topicRouterApi.updateMunicipality(municipality).then(function(municipality){
						$modalInstance.close(municipality);
					});
				} else{
					topicRouterApi.createMunicipality($scope.municipalityName).then(function(municipality){
						$modalInstance.close(municipality);
					});
				}
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
