(function () {
	'use strict';
	angular.module('topicRouter').controller('AddSubstituteModalInstanceCtrl', AddSubstituteModalInstanceCtrl);

	AddSubstituteModalInstanceCtrl.$inject = ['$log', '$scope', '$modalInstance', 'topicRouterApi'];

	function AddSubstituteModalInstanceCtrl($log, $scope, $modalInstance, topicRouterApi){
		$scope.employeeFilter = "";
		$scope.modalAlerts = [];

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.setCurrentEmployee = setCurrentEmployee;
		$scope.closeAlert = closeAlert;


		activate();

		function activate(){
			$log.info("AddSubstituteModal::activate");
			topicRouterApi.getEmployments($scope.user.municipality.id).then(function(employees){
				$scope.employees = employees;
			});
		}

		function ok(){
			if($scope.currentEmployee){
				$modalInstance.close($scope.currentEmployee);
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal vælge en medarbejder.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function setCurrentEmployee(employee){
			$scope.currentEmployee = employee;
		}

		function addAlert(alert) {
			$scope.modalAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.modalAlerts.splice(index, 1);
		}
	}
})();
