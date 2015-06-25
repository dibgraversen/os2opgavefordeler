(function () {
	'use strict';

	angular.module('topicRouter').controller('LoginCtrl', LoginCtrl);

	LoginCtrl.$inject = ['$scope', 'topicRouterApi', 'serverUrl'];

	function LoginCtrl($scope, topicRouterApi, serverUrl) {
		/* jshint validthis:true */
		var vm = this;

		$scope.providers = [];

		activate();

		function activate() {
			topicRouterApi.getIdentityProviders().then(function(providers) {
				$scope.providers = providers;
				_.each($scope.providers, function(provider) {
					provider.url = serverUrl + "/auth/providers/" + provider.id + "/signin";
				});
			});
		}
	}
})();
