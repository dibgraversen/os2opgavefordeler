(function(){
	'use strict';

	angular.module('topicRouter').factory('topicRouterApi', topicRouterApi);

	topicRouterApi.$inject = ['$http', '$q']; // TODO add appSpinner

	function topicRouterApi($http, $q) { // TODO add appSpinner
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
			var deferred = $q.defer();
			deferred.resolve(mockTopicRoutes());
			return deferred.promise;
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

			var allan = {
				id: 2,
				name: 'Allan Gyldendal Frederiksen'
			};

			var jon = {
				id: 3,
				name: 'Jon Badstue Pedersen'
			};


			return [
				{
					id: 1,
					kle: {
						number: '01',
						name: 'Fysisk planlægning og naturbeskyttelse',
						type: 'main',
						serviceText: 'Dette er en servicetekst til 01.'
					},
					rule: {
						orgUnit: 'Udvikling',
						externalUnit: 'Plan, udvikling og kultur'
					},
					responsible: {
						id: 2,
						name: 'Kultur Planlægning og Erhverv (Kenneth Jensen)'
					},
					children: [2],
					open: true,
					visible: true
				}, {
					id: 2,
					kle: {
						number: '01.06',
						name: 'Geografiske informationssystemer',
						type: 'group',
						serviceText: 'Dette er en servicetekst til 01.06.'
					},
					rule: {
						orgUnit: 'It',
						externalUnit: 'Digitalisering og It',
						employee: allan
					},
					responsible: {
						id: 2,
						name: 'Kultur Planlægning og Erhverv (Kenneth Jensen)'
					},
					children: [3,4],
					open: true,
					visible: true
				},{
					id: 3,
					kle: {
						number: '01.06.00',
						name: 'Geografiske informationssystemer i almindelighed',
						type: 'topic'
					},
					rule: {
						orgUnit: 'Digitalisering',
						externalUnit: 'Digitalisering og It',
						employee: allan
					},
					responsible: digiJon,
					visible: true
				},{
					id: 4,
					kle: {
						number: '01.06.01',
						name: 'Fikspunkter',
						type: 'topic'
					},
					rule: {
						orgUnit: 'Digitalisering',
						externalUnit: 'Digitalisering og It',
						employee: allan
					},
					responsible: digiJon,
					visible: true
				},{
					id: 5,
					kle: {
						number: '85',
						name: 'Kommunens administrative systemer',
						type: 'main',
						serviceText: 'Dette er en servicetekst til 85.'
					},
					rule: {
						orgUnit: 'Digitalisering',
						externalUnit: 'Digitalisering og It',
						employee: jon
					},
					responsible: {
						id: 3,
						name: 'HR og Digitalisering (Eva Due)'
					},
					children: [6],
					open: true,
					visible: true
				},{
					id: 6,
					kle: {
						number: '85.04',
						name: 'Blanketter og formularer',
						type: 'group'
					},
					rule: {
						orgUnit: 'Digitalisering',
						externalUnit: 'Digitalisering og It',
						employee: jon
					},
					responsible: digiJon,
					children: [7,8],
					open: true,
					visible: true
				},{
					id: 7,
					kle: {
						number: '85.04.00',
						name: 'Blanketter og formularer i almindelighed',
						type: 'topic'
					},
					rule: {
						orgUnit: 'Digitalisering',
						externalUnit: 'Digitalisering og It',
						employee: jon
					},
					responsible: digiJon,
					visible: true
				},{
					id: 8,
					kle: {
						number: '85.04.02',
						name: 'KL autoriserede standardblanketter',
						type: 'topic'
					},
					rule: {
						orgUnit: 'Direktionssekretariatet',
						externalUnit: 'Ledelsessekretariatet'
					},
					responsible: digiJon ,
					visible: true
				}
			];
		}
	}
})();