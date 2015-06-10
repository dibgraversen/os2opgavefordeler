(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('EditRuleModalInstanceCtrl', EditRuleModalInstanceCtrl);

	EditRuleModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topicRouterApi', 'topic'];

	function EditRuleModalInstanceCtrl($scope, $modalInstance, topicRouterApi, topic){
		$scope.topic = topic;
		$scope.orgUnits = [];
		$scope.employments = [];
		$scope.orgFilter = "";
		$scope.empFilter = "";
		$scope.ruleAlerts = [];

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.setSelectedOrgUnit = setSelectedOrgUnit;
		$scope.setSelectedEmp = setSelectedEmp;
		$scope.closeAlert = closeAlert;

		activate();

		function activate(){
			// load some org. stuff.
			topicRouterApi.getOrgUnitsForResponsibility().then(function(orgUnits){
				$scope.orgUnits = orgUnits;
			});

			topicRouterApi.getEmployments().then(function(employments){
				$scope.employments = employments;
			});
		}

		function ok(){
			// TODO below, add topic parent org unit presence validation.
			if($scope.topic.org || $scope.selectedOrgUnit){ // validate org unit either present or selected.
				if($scope.selectedOrgUnit || $scope.selectedEmp){ // validate something has been selected.
					if($scope.selectedOrgUnit){
						$scope.topic.org = $scope.selectedOrgUnit;
					}
					if($scope.selectedEmp){
						$scope.topic.employee = $scope.selectedEmp;
					}
					topicRouterApi.updateDistributionRule($scope.topic);
					$modalInstance.close(/* pass some value? */);
				} else {
					addAlert({
						type: 'warning',
						msg: 'Du skal vælge organisatorisk enhed eller medarbejder.'
					});
				}
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal som minimum vælge en organisatorisk enhed.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function setSelectedOrgUnit(orgUnit){
			$scope.selectedOrgUnit = orgUnit;
		}

		function setSelectedEmp(emp){
			$scope.selectedEmp = emp;
		}

		function addAlert(alert) {
			$scope.ruleAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.ruleAlerts.splice(index, 1);
		}
	}
})();
