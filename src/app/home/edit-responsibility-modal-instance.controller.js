(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('EditResponsibilityModalInstanceCtrl', EditResponsibilityModalInstanceCtrl);

	EditResponsibilityModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topicRouterApi', 'topic', 'municipality'];

	function EditResponsibilityModalInstanceCtrl($scope, $modalInstance, topicRouterApi, topic, municipality){
		$scope.topic = topic;
		$scope.orgUnitFilter = "";
		$scope.modalAlerts = [];

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.setCurrentOrgUnit = setCurrentOrgUnit;
		$scope.closeAlert = closeAlert;

		var currentEmployment = $scope.user.currentRole.employment;

		activate();

		function activate(){
			topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment).then(function(orgUnits){
				$scope.orgUnits = orgUnits;
			});
		}

		function ok(){
			if($scope.currentOrgUnit){
				$scope.topic.responsible = $scope.currentOrgUnit;
				topicRouterApi.updateDistributionRule(topic);
				$modalInstance.close(/* pass some value? */);
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal vælge en organisatorisk enhed.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function setCurrentOrgUnit(orgUnit){
			$scope.currentOrgUnit = orgUnit;
		}

		function addAlert(alert) {
			$scope.modalAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.modalAlerts.splice(index, 1);
		}
	}
})();
