(function () {
	'use strict';

	angular.module('topicRouter').controller('LoginCtrl', LoginCtrl);

	LoginCtrl.$inject = ['$scope', 'topicRouterApi', 'bootstrapService', 'serverUrl'];

	function LoginCtrl($scope, topicRouterApi, bootstrapService, serverUrl) {
		/* jshint validthis:true */
		$scope.providers = [];
		$scope.dev = false;

		$scope.bootstrap = function() { bootstrapService.bootstrap(); };
		$scope.email = "";
		// $scope.buildRules = function() { bootstrapService.buildRules() };

		activate();

		function activate() {
			$scope.dev = _.contains(serverUrl, 'localhost') || _.contains(serverUrl, '127.0.0.1');

			topicRouterApi.getIdentityProviders().then(function(providers) {
				$scope.providers = providers;
				_.each($scope.providers, function(provider) {
					provider.url = serverUrl + "/auth/providers/" + provider.id + "/signin";
				});
			});
		}

	}
})();
