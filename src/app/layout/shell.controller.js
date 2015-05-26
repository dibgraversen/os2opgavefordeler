(function () {
	'use strict';

	angular.module('topicRouter').controller('ShellCtrl', ShellCtrl);

	ShellCtrl.$inject = ['$scope', '$rootScope', '$state', '$timeout', 'topicRouterApi'];

	function ShellCtrl($scope, $rootScope, $state, $timeout, topicRouterApi) {

		/* jshint validthis:true */
		var vm = this;
		$scope.$state = $state;

		$scope.alerts = [];
		$scope.addAlert = addAlert;
		$scope.closeAlert = closeAlert;

		$scope.sidemenuVisible = true;
		$scope.settings = {};
		$scope.user = {};
		$scope.updateSettings = updateSettings;

		$scope.changeUser = changeUser;
		$scope.changeRole = changeRole;

		$scope.filter = {
			text: '',
			whichTasks: 'all'
		};
		$scope.updateFilter = updateFilter;

		vm.showSpinner = false;
		vm.spinnerMessage = 'Henter data...';

		vm.spinnerOptions = {
			radius: 40,
			lines: 8,
			length: 0,
			width: 30,
			speed: 1.7,
			corners: 1.0,
			trail: 100,
			color: '#428bca'
		};

		activate();

		function activate() {
			//user = getUser();
			//if(user){
				changeUser({
					name: 'Henrik',
					id: 1
				});
			//}
			//getSettings($scope.user.id);
			//getRoles($scope.user);
		}

		$rootScope.$on('spinner.toggle', function (event, args) {
			vm.showSpinner = args.show;
			if (args.message) {
				vm.spinnerMessage = args.message;
			}
		});

		function addAlert(alert) {
			$scope.alerts.push(alert);
		}

		function closeAlert(index) {
			$scope.alerts.splice(index, 1);
		}

		function changeUser(user) {
			$scope.user = user;
			getSettings(user);
			getRoles(user);
		}

		function getSettings(user) {
			topicRouterApi.getSettings(user.id).then(
					function (data) {
						$scope.settings = data;
					},
					function (reason) {
						addAlert({
							msg: 'Gemte indstillinger for filter kunne ikke findes.',
							type: 'warning'
						});
						$scope.settings = {
							scope: 'RESPONSIBLE'
						};
					});
		}

		function getRoles(user) {
			topicRouterApi.getRoles($scope.user.id).then(function (data) {
				$scope.user.roles = data;
				$scope.user.currentRole = data[0];
			});
		}

		function changeRole(role) {
			console.log('switching user');
			//$scope.user.currentRole = role;
			console.log($scope.user);
		}

		function updateFilter() {
			console.log($scope.filter);
		}

		function updateSettings() {
			topicRouterApi.updateSettings($scope.user.id, $scope.settings).then(
					function (data) {
						var alert = {
							msg: "Indstillinger gemt",
							type: 'success'
						};
						addAlert(alert);
						$timeout(closeAlert, 2000, alert);
					}, function (reason) {
						addAlert({
							msg: 'Indstillinger kunne ikke gemmes. Prøv igen senere...',
							type: 'danger'
						});
					});
		}
	}
})();
