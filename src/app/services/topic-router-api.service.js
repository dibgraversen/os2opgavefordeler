(function(){
	'use strict';

	angular.module('topicRouter').factory('topicRouterApi', topicRouterApi);

	topicRouterApi.$inject = ['$http', '$q', 'serverUrl']; // TODO add appSpinner

	function topicRouterApi($http, $q, serverUrl) { // TODO add appSpinner
		var service = {
			getTopicRoutes: getTopicRoutes,
			getRoles: getRoles
		};

		var baseUrl = serverUrl;
		var requestConfig = {
			headers: {
				//'SOME-HEADER': 'ASDFASDFASDFASDF'
			}
		};

		return service;

		function getTopicRoutes(){
			//MOCK
			//var deferred = $q.defer();
			//deferred.resolve(mockTopicRoutes());
			//return deferred.promise;
			//MOCK
			var deferred = $q.defer();

			var users = getUsers();
			var orgs = getOrganisations();

			httpGet('/taskRoutes', {
				params: {
					employment: '1',
					scope: 'all'
				}
			}).then(function(data){
				var objectMap = {};
				_.each(data, function(rule){
					objectMap[rule.id] = rule;
					rule.children = [];
					if(rule.parent){
						var parent = objectMap[rule.parent];
						// TODO consider putting object itself here.
						// TODO consider adding parent as object.
						parent.children.push(rule.id);
						//console.log('rule has parent: '+rule.parent);
					}
					rule.employee = users[rule.employee];
					rule.org = orgs[rule.org];
					rule.open = true;
					rule.visible = true;
				});
				deferred.resolve(data);
			});
			return deferred.promise;
		}

		function getRoles(){
			var deferred = $q.defer();
			deferred.resolve(getMockRoles());
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

				//appSpinner.hideSpinner();
				console.log('**response from EXECUTE', response);
				return response.data;
			});
		}

		// mock functions

		function mockTopicRoutes(){
			var users = getUsers();
			var digiJon = users[4];
			var allan = users[1];
			var jon = users[5];


			return [
				{
					id: 1,
					kle: createKLE('01', 'Fysisk planlægning og naturbeskyttelse',
							'main', 'Dette er en servicetekst til 01.'
					),
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
					orgUnit: 'It',
					externalUnit: 'Digitalisering og It',
					employee: allan,
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
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: allan,
					responsible: digiJon,
					visible: true
				},{
					id: 4,
					kle: {
						number: '01.06.01',
						name: 'Fikspunkter',
						type: 'topic'
					},
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: allan,
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
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: jon,
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
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: jon,
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
					orgUnit: 'Digitalisering',
					externalUnit: 'Digitalisering og It',
					employee: jon,
					responsible: digiJon,
					visible: true
				},{
					id: 8,
					kle: {
						number: '85.04.02',
						name: 'KL autoriserede standardblanketter',
						type: 'topic'
					},
					orgUnit: 'Direktionssekretariatet',
					externalUnit: 'Ledelsessekretariatet',
					responsible: digiJon ,
					visible: true
				}
			];
		}

		function getMockRoles(){
			return [
				{
					id: 1,
					employment: 1,
					name: 'Henrik (dig)',
					admin: false,
					municipalityAdmin: false,
					substitute: false
				},{
					id: 2,
					name: 'Admin',
					admin: true,
					municipalityAdmin: false,
					substitute: false
				},{
					id: 3,
					name: 'Jørgen Jensen',
					admin: false,
					municipalityAdmin: true,
					substitute: true
				}, {
					id: 4,
					name: 'Hans Jørgensen',
					admin: true,
					municipalityAdmin: true,
					substitute: true
				}
			];
		}

		function getUsers() {
			return {
				4: {
					id: 1,
					name: 'Digitalisering (Jon Badstue Pedersen)'
				},
				1: {
					id: 4,
					name: 'Allan Gyldendal Frederiksen'
				},
				5: {
					id: 5,
					name: 'Jon Badstue Pedersen'
				}
			};
		}

		function getOrganisations(){
			return {
				1: "Udvikling",
				2: "Direktionssekretariatet",
				3: "It",
				4: "Digitalisering"
			};
		}

		function createKLE(number, name, type, serviceText){
			return {
				number: number,
				name: name,
				type: type,
				serviceText: serviceText
			};
		}
	}
})();