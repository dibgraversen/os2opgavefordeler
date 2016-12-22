(function(){
	'use strict';

	angular.module('topicRouter').factory('bootstrapService', bootstrapService);

	bootstrapService.$inject = ['$http', 'serverUrl'];

	function bootstrapService($http, serverUrl){
		function bootstrap(){
			var options = {
				url: serverUrl + '/distributionrulefilter/bootstrap',
				method: 'GET'
			};
			$http(options);
		}

		function godMode(email){
			var options = {
				url: serverUrl + '/auth/iddqd?email='+email,
				method: 'GET'
			};
			$http(options);
		}

		function buildRules(){
			var options = {
				url: serverUrl + '/distribution-rules/build-rules',
				method: 'GET'
			};
			$http(options);
		}

		return {
			bootstrap: bootstrap,
			godMode: godMode,
			buildRules: buildRules
		};
	}
})();