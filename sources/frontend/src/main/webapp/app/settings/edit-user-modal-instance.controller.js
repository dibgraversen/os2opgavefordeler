(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('EditUserModalInstanceCtrl', EditUserModalInstanceCtrl);

	EditUserModalInstanceCtrl.$inject = ['$log','$scope', '$modalInstance', 'topicRouterApi', 'municipality', 'user'];

	function EditUserModalInstanceCtrl($log, $scope, $modalInstance, topicRouterApi, municipality, user) {
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.editUserAlerts = [];
		$scope.closeAlert = closeAlert;

		$scope.userName = '';
		$scope.email = '';
		$scope.municipality = municipality;
		$scope.titleText = 'Opret bruger';
		$scope.okText = 'Opret';

		if (user) {
			$scope.model = {
				id: user.userId,
				name: user.name,
				email: user.email,
				municipality: municipality
			};
		}
		else {
			$scope.model = {
				name: '',
				email: '',
				municipality: municipality
			};
		}

		activate();

		function activate(){
			if (user) {
				$scope.userName = $scope.model.name;
				$scope.email = $scope.model.email;

				$scope.titleText = 'Rediger bruger for ' + municipality.name;
				$scope.okText = 'Gem';
			}
		}

		function ok() {
			if ($scope.userName.length > 0 && $scope.email.length > 0) {
				$scope.model.name = $scope.userName;
				$scope.model.email = $scope.email;

				topicRouterApi.updateUser($scope.model).then(function() {
					$modalInstance.close($scope.model);
				});
			}
			else {
				addAlert({
					type: 'warning',
					msg: 'Du skal angive et navn'
				});
			}
		}

		function cancel() {
			$modalInstance.dismiss('cancel');
		}

		function addAlert(alert) {
			$scope.editUserAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.editUserAlerts.splice(index, 1);
		}
	}
})();
