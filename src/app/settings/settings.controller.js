(function () {
	'use strict';
	angular.module('topicRouter').controller('SettingsCtrl', SettingsCtrl);

	SettingsCtrl.$inject = ['$scope', '$state', '$log'];

	function SettingsCtrl($scope, $state, $log) {
		/* jshint validthis: true */
		var vm = this;

		activate();

		function activate() {
			$log.info("Settings::activate")
			if(!($scope.user.loggedIn && $scope.user.currentRole.admin)) {
				$log.info("not privileged, redirecting to home");
				$state.go("home");
			}
		}

		function navigate(){
		}
	}
})();
