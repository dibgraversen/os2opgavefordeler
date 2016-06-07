(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('DeleteUserModalInstanceCtrl', DeleteUserModalInstanceCtrl);

	DeleteUserModalInstanceCtrl.$inject = ['$log', '$scope', '$modalInstance', 'topicRouterApi', 'user'];

	function DeleteUserModalInstanceCtrl($log, $scope, $modalInstance, topicRouterApi, user) {
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.deleteUserAlerts = [];
		$scope.closeAlert = closeAlert;

		$scope.userName = '';

		$scope.titleText = 'Slet bruger';
		$scope.okText = 'Slet';

		activate();

		function activate() {
			if (user) {
				$scope.userName = user.name;

				$scope.titleText = 'Slet bruger';
				$scope.okText = 'Slet';
			}
		}

		function ok() {
			if (user) {
				topicRouterApi.deleteUser(user.userId).then(function() {
					$modalInstance.close(user);
				});
			}
		}

		function cancel() {
			$modalInstance.dismiss('cancel');
		}

		function addAlert(alert) {
			$scope.deleteUserAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.deleteUserAlerts.splice(index, 1);
		}
	}
})();
