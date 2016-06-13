(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('AddParameterNameModalInstanceCtrl', AddParameterNameModalInstanceCtrl);

	AddParameterNameModalInstanceCtrl.$inject = ['$scope', '$modalInstance', '$log', 'topicRouterApi', 'parameter', 'type', 'municipality'];

	function AddParameterNameModalInstanceCtrl($scope, $modalInstance, $log, topicRouterApi, parameter, type, municipality) {
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.createParameterNameAlerts = [];
		$scope.closeAlert = closeAlert;
		$scope.municipality = municipality;
		$scope.parameter = parameter;
		$scope.type = type;
		$scope.model = {};
		$scope.titleText = 'Opret parameternavn';
		$scope.okText = 'Opret';

		activate();

		function activate() {
			if (parameter) {
				$scope.model = {
					id: parameter.id,
					type: parameter.type,
					name: parameter.name
				};

				$scope.titleText = 'Rediger parameternavn';
				$scope.okText = 'Gem';
			}
			else {
				$scope.model = {
					name: '',
					type: $scope.type,
					defaultName: false
				};
			}
		}

		function ok() {
			if ($scope.model.name.length > 0) {
				if ($scope.type == 'TextDistributionRuleFilter') {
					topicRouterApi.updateTextParameterName($scope.municipality, $scope.model).then(function(updatedParameter){
						$modalInstance.close(updatedParameter);
					});
				}
				else {
					topicRouterApi.updateDateParameterName($scope.municipality, $scope.model).then(function(updatedParameter){
						$modalInstance.close(updatedParameter);
					});
				}
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal indtaste et parameternavn.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function addAlert(alert) {
			$scope.createParameterNameAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.createParameterNameAlerts.splice(index, 1);
		}
	}
})();
