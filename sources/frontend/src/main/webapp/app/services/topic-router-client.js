(function () {
	'use strict';

	angular.module('topicRouter').factory('topicRouterClient', topicRouterClient);

	topicRouterClient.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	var maxPopoverLength = 150;

	// NOTE consider implementing ngResource to manage RESTful resource endpoints.
	function topicRouterClient($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {
		var service = {
			get: httpGet,
			delete: httpDelete,
			post: httpPost
		};

		var cache = $cacheFactory('genericCache'),
				requestConfig = {
			headers: {
				'Content-Type': 'application/json',
				'Cache-Control': 'no-cache',
				'Pragma': 'no-cache',
				'Expires': '-1'
			}
		};

		return service;

		function httpGet(url, params) {
			var options = {
				cache: cache
			};
			if (params) {
				//options.params = encodeURIComponent( JSON.stringify(params) );
				options.params = params;
			}
			return httpExecute(url, 'GET', options);
		}

		function httpDelete(url) {
			cache.removeAll();
			return httpExecute(url, 'DELETE', {});
		}

		function httpPost(url, data) {
			cache.removeAll();
			return httpExecute(url, 'POST', {data: data});
		}

		// private methods
		function httpExecute(requestUrl, method, options) {
			var defaults = {
				url: serverUrl + requestUrl,
				method: method,
				withCredentials: true,
				headers: requestConfig.headers
			};
			angular.extend(options, defaults); // merge defaults into options.
			appSpinner.showSpinner();
			return $http(options).then(
					function (response) {
						appSpinner.hideSpinner();
						return response.data;
					},
					function (reason) {
						appSpinner.hideSpinner();
						return $q.reject(reason);
					});
		}
	}
})();