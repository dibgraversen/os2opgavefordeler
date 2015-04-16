(function(){
	'use strict';

	angular.module('topicRouter').factory('topicRouterApi', topicRouterApi);

	topicRouterApi.$inject = ['$http']; // TODO add appSpinner

	function topicRouterApi($http) { // TODO add appSpinner
		var service = {
			getTopicRoutes: getTopicRoutes
		};

		var baseUrl = ''; // TODO add server base url
		var requestConfig = {
			headers: {
				'SOME-HEADER': 'ASDFASDFASDFASDF'
			}
		};

		return service;

		function getTopicRoutes(){
			return mockTopicRoutes();
		}


		// private methods

		function httpGet(url){
			return httpExecute(url, 'GET');
		}

		function httpExecute(requestUrl, method, data){
			//appSpinner.showSpinner(); // TODO enable
			return $http({
				url: baseUrl + requestUrl,
				method: method,
				data: data,
				headers: requestConfig.headers }).then(function(response){

				appSpinner.hideSpinner();
				console.log('**response from EXECUTE', response);
				return response.data;
			});
		}

		// mock functions

		function mockTopicRoutes(){
			return [
					{
						id: '1',
						name: 'Første fordeling'
					},
					{
						id: '2',
						name: 'Anden fordeling'
					},
					{
						id: '3',
						name: 'Tredje fordeling'
					}
			]
		}

	}

})();