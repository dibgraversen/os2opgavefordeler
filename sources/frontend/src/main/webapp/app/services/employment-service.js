(function () {
	'use strict';

	angular.module('topicRouter').factory('employmentService', employmentService);

	employmentService.$inject = ['topicRouterClient', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	// NOTE consider implementing ngResource to manage RESTful resource endpoints.
	function employmentService(client, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {
		return {
			getSubordinates: getSubordinates
		};

		function getSubordinates(employmentId){
			return client.get('/employments/'+employmentId+'/subordinates');
		}
	}
})();