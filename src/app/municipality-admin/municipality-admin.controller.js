(function () {
	'use strict';
	angular.module('topicRouter').controller('MunicipalityAdminCtrl', MunicipalityAdminCtrl);

	MunicipalityAdminCtrl.$inject = ['$scope', '$state', '$log'];

	function MunicipalityAdminCtrl($scope, $state, $log) {
		/* jshint validthis:true */
		var vm = this;

		activate();

		function activate() {
			$log.info('MunicipalityAdmin::activate');
			if(!($scope.user.loggedIn && $scope.user.currentRole.municipalityAdmin)) {
				$log.info("not privileged, redirecting to home");
				$state.go("home");
			}
		}
	}
})();
