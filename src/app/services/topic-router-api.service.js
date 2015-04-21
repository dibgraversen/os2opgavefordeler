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
			var digiJon = {
				id: 1,
				name: 'Digitalisering (Jon Badstue Pedersen)'
			};


			return [
				{
					id: 1,
					topic: '01 Fysisk planlægning og naturbeskyttelse',
					orgUnit: 'Udvikling',
					externalUnit: 'Plan, udvikling og kultur',
					employee: '',
					owner: {
						id: 2,
						name: 'Kultur Planlægning og Erhverv (Kenneth Jensen)'
					},
					open: true,
					visible: true
				},{
					id: 2,
					topic: '01.06 Geografiske informationssystemer',
					orgUnit: 'It',
					externalUnit: 'Digitalisering og It',
					employee: 'Allan Gyldendal Frederiksen',
					owner: {
						id: 2,
						name: 'Kultur Planlægning og Erhverv (Kenneth Jensen)'
					},
					open: true,
					visible: true
				},{
					id: 3,
					topic: '01.06.00 Geografiske informationssystemer i almindelighed',
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: 'Allan Gyldendal Frederiksen',
					owner: digiJon,
					open: true,
					visible: true
				},{
					id: 4,
					topic: '01.06.01 Fikspunkter',
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: 'Allan Gyldendal Frederiksen',
					owner: digiJon,
					open: true,
					visible: true
				},{
					id: 5,
					topic: '85 Kommunens administrative systemer',
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: 'Jon Badstue Pedersen',
					owner: {
						id: 3,
						name: 'HR og Digitalisering (Eva Due)'
					},
					open: true,
					visible: true
				},{
					id: 6,
					topic: '85.04 Blanketter og formularer',
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: 'Jon Badstue Pedersen',
					owner: digiJon,
					open: true,
					visible: true
				},{
					id: 7,
					topic: '85.04.00 Blanketter og formularer i almindelighed',
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: 'Jon Badstue Pedersen',
					owner: digiJon,
					open: true,
					visible: true
				},{
					id: 8,
					topic: '85.04.02 KL autoriserede standardblanketter',
					orgUnit: 'Direktionssekretariatet',
					externalUnit: 'Ledelsessekretariatet',
					employee: '',
					owner: digiJon ,
					open: true,
					visible: true
				}
			];
		}
	}
})();