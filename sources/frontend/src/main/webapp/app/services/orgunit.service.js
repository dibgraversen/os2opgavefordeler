(function () {
	'use strict';

	angular.module('topicRouter').factory('orgUnitService', orgUnitService);

	orgUnitService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	function orgUnitService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {		
		var service = {
			getOrgUnits: getOrgUnits,
			getOrgUnit: getOrgUnit,
			addKle : addKle,
			removeKle : removeKle,
			getKles : getKles
		};

		var baseUrl = serverUrl;

		var requestConfig = {
			headers: {
				'Content-Type': 'application/json',
				'Cache-Control': 'no-cache',
				'Pragma': 'no-cache',
				'Expires': '-1'
			}
		};
		return service;

		function getOrgUnits() {
			//console.log("GET /ou");

			return httpGet('/ou');
		}

		function getOrgUnit(id) {
			//console.log("GET /ou/" + id);

			return httpGet('/ou/' + id);
		}

		function getKles(){	
			//console.log("GET /kle2'");
	
			return httpGet('/kle2');
		}

		function addKle(kle,orgunit){
			//console.log("httpPost " + "/ou/" + orgunit.id + "/addKLE");
			var payload = {"number": kle.number, "assignmentType" : kle.assignmentType};

			return httpPost("/ou/" + orgunit.id + "/addKLE", payload);
		}

		function removeKle(kle,orgunit){
		//	console.log("/ou/" + orgunit.id + "/removeKLE");

			var payload = {"number": kle.number, "assignmentType" : kle.assignmentType};

			return httpPost("/ou/" + orgunit.id + "/removeKLE", payload);
		}

		function simulateRestCall(functionToSimulate){ 
			return $timeout(functionToSimulate,10);
		}

		function httpGet(url, params) {
			var options = {
				//cache: cache
			};
			if (params) {
				//options.params = encodeURIComponent( JSON.stringify(params) );
				options.params = params;
			}
			return httpExecute(url, 'GET', options);
		}

		function httpPost(url, data) {
			return httpExecute(url, 'POST', {data: data});
		}

		function httpExecute(requestUrl, method, options) {
			var defaults = {
				url: baseUrl + requestUrl,
				method: method,
				withCredentials: true,
				headers: requestConfig.headers
			};

			$log.log(defaults.url);
			
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