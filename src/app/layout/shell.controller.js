(function () {
	'use strict';
	angular.module('topicRouter').controller('ShellCtrl', ShellCtrl);

	ShellCtrl.$inject = ['$scope', '$rootScope', '$state', '$timeout', '$q', '$modal', '$log',
		'topicRouterApi', 'version'];

	function ShellCtrl($scope, $rootScope, $state, $timeout, $q, $modal, $log, topicRouterApi, version) {
		/* jshint validthis:true */
		var vm = this;

		$scope.$state = $state;
		$scope.version = version;

		$scope.alerts = [];
		$scope.addAlert = addAlert;
		$scope.closeAlert = closeAlert;

		$scope.sidemenuVisible = true;
		$scope.settings = {};
		$scope.user = {};
		$scope.updateSettings = updateSettings;

		$scope.changeUser = changeUser;
		$scope.logoutUser = logoutUser;
		$scope.changeRole = changeRole;

		$scope.substitutes = [];
		$scope.addSubstitute = addSubstitute;
		$scope.removeSubstitute = removeSubstitute;

		$scope.filter = {
			text: '',
			whichTasks: 'all'
		};
		$scope.updateFilter = updateFilter;

		vm.showSpinner = false;
		vm.spinnerMessage = 'Opdaterer...';

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
			$log.info("Shell::activate");
			topicRouterApi.getUserInfo().then(function(user) {
				if(user.loggedIn) {
					changeUser(user);
				} else {
					$state.go("login");
				}
			});
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

		function logoutUser() {
			topicRouterApi.logoutUser()
			.then(function(message) {
				$log.info("User logged out");
				$scope.user = {
					loggedIn: false
				};
				$state.reload();
			});

			/* TODO: after fixing http post so we can use success/error, add logout-error code:
			 else {
			 $log.warn("Couldn't log out");
			 addAlert({
			 msg: 'Kunne ikke logge ud',
			 type: 'danger'
			 });
			 */
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
			var deferred = $q.defer();
			topicRouterApi.getRoles(user.id).then(function (data) {
				if(data){
					$scope.user.roles = data;
					$scope.user.currentRole = data[0];
				}
				deferred.resolve();
			});
			return deferred.promise;
		}

		function changeRole(role) {
			if(role !== $scope.user.currentRole) {
				$log.info("changeRole: " + role);
				$scope.user.currentRole = role;
				$state.reload();
			}
		}

		function addSubstitute(){
			$modal.open({
				templateUrl: 'app/home/add-substitute-modal.html',
				controller: 'AddSubstituteModalInstanceCtrl'
			}).result.then(function(sub){
				console.log('getting a sub');
				topicRouterApi.addSubstitute($scope.user.currentRole.employment, sub);
				$scope.substitutes.push(sub);
			});
		}

		function removeSubstitute(substitute){
			topicRouterApi.removeSubstitute($scope.user.currentEmployment, substitute)
					.then(function(){
						_.remove($scope.substitutes, function(sub){
							return sub === substitute;
						});
					});
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
