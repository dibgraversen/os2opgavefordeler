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
		$scope.loadAll = loadAll;
		$scope.loading = false;

		var currentEmployment = $scope.user.currentRole.employment;
		var allMissing = true;

		activate();

		function activate(){
			topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment, true).then(function(orgUnits) {
				$scope.orgUnits = orgUnits;
			});
		}

		function loadAll(){
			if(allMissing){
				$scope.loading = true;
				topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment, false).then(function (orgUnits) {
					$scope.loading = false;
					$scope.orgUnits = orgUnits;
				});
				allMissing = false;
			}
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
