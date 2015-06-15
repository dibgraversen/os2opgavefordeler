(function () {
	'use strict';

	angular.module('topicRouter').factory('topicRouterApi', topicRouterApi);

	topicRouterApi.$inject = ['$http', '$q', '$timeout', 'serverUrl', 'appSpinner'];

	var maxPopoverLength = 150;

	// NOTE consider implementing ngResource to manage RESTful resource endpoints.
	function topicRouterApi($http, $q, $timeout, serverUrl, appSpinner) {
		var service = {
			getTopicRoutes: getTopicRoutes,
			getRoles: getRoles,
			getSettings: getSettings,
			updateSettings: updateSettings,
			getOrgUnitsForResponsibility: getOrgUnitsForResponsibility,
			getEmployments: getEmployments,
		  getEmployment: getEmployment,
			updateDistributionRule: updateDistributionRule,
			addSubstitute: addSubstitute,
			removeSubstitute: removeSubstitute
		};

		var baseUrl = serverUrl;
		var requestConfig = {
			headers: {
				'Content-Type': 'application/json'
			}
		};

		return service;

		function getSettings(userId) {
			return httpGet('/user/' + userId + '/settings');
		}

		function updateSettings(userId, settings) {
			return httpPost('/user/' + userId + '/settings', settings);
		}

		function getTopicRoutes(employment, scope) {
			//MOCK
			//var deferred = $q.defer();
			//deferred.resolve(mockTopicRoutes());
			//return deferred.promise;
			//MOCK
			var deferred = $q.defer();

			httpGet('/distribution-rules', {
				"employment": employment,
				"scope": scope
			}).then(function (data) {
				var objectMap = {};
				_.each(data, function(rule){
					objectMap[rule.id] = rule;
					rule.children = [];
				});
				_.each(data, function (rule) {
					if (rule.parent) {
						var parent = objectMap[rule.parent];
						rule.parent = parent;
						parent.children.push(rule.id);
						//console.log('rule has parent: '+rule.parent);
					}
					if(rule.employee > 0){
						getEmployment(rule.employee).then(function(employee){
							rule.employee = employee;
						});
					}
					if(rule.org > 0){
						getOrgUnit(rule.org).then(function(orgUnit){
							rule.org = orgUnit;
						});
					}
					rule.open = true;
					rule.visible = true;
					if(rule.responsible > 0){
						//if(rule.responsible === 2 ) rule.responsible = 3;
						getOrgUnit(rule.responsible).then(function(orgUnit){
							rule.responsible = orgUnit;
						});
					}
					rule.kle.serviceTextPopover = htmlsave.truncate(rule.kle.serviceText, maxPopoverLength, { breakword:false });
				});
				deferred.resolve(data);
			});
			return deferred.promise;
		}

		function getOrgUnit(orgId){
			return httpGet('/org-unit/'+orgId).then(function(orgUnit){
				if(orgUnit.managerId > 0){
					getEmployment(orgUnit.managerId).then(function(employment){
						orgUnit.manager = employment;
					});
				}
				return orgUnit;
			});
		}

		function getRoles(userId) {
			return httpGet('/user/' + userId + '/roles');
		}

		/**
		 * Returns a list of orgUnits to choose from.
		 * @returns {Object[]} OrgUnit - A list of all OrgUnits.
		 */
		function getOrgUnitsForResponsibility(){
			return httpGet('/org-unit').then(function(orgUnits){
				_.each(orgUnits, function(orgUnit){
					if(orgUnit.managerId > 0){
						getEmployment(orgUnit.managerId).then(function(employment){
							orgUnit.manager = employment;
						});
					}
				});
				return orgUnits;
			});
		}

		function getEmployments(){
			return httpGet('/employments');
		}

		function getEmployment(empId){
			return httpGet('/employments/'+empId);
		}

		function updateDistributionRule(distributionRule){
			var distRule = new DistributionRule(distributionRule.id, distributionRule.parent.id,
					new KLE(distributionRule.kle.id, distributionRule.number, distributionRule.name, distributionRule.serviceText),
			distributionRule.org.id, distributionRule.employee.id, distributionRule.responsible.id);
			return httpPost('/distribution-rules/'+distributionRule.id, distRule);
		}

		function addSubstitute(userEmployment, substituteEmployment){
			return true;
		}

		function removeSubstitute(userEmployment, substituteEmployment){
			var deferred = $q.defer();
			deferred.resolve(substituteEmployment);
			return deferred.promise;
		}

		// DTO classes.

		/**
		 @class OrgUnit
		 @private
		 @type {Object}
		 @property {number} id The system id.
		 @property {number} parentId The id of the OrgUnit parent.
		 @property {number} managerId The id of the employment that is the manager.
		 @property {string} name The name of the OrgUnit.
		 @property {string} esdhId The id of the OrgUnit in a esdh system.
		 @property {string} email The email address of the OrgUnit.
		 @property {string} phone The phone number of the OrgUnit.
		 */
		function OrgUnit(id, parentId, managerId, name, esdhId, email, phone) {
			return {
				id:id,
				parentId:parentId,
				managerId:managerId,
				name:name,
				esdhId:esdhId,
				email:email,
				phone:phone
			};
		}

		/**
		 * @class KLE
		 * @private
		 * @type {Object}
		 * @property {number} id The system id of the KLE.
		 * @property {string} number The public number of the KLE in form nn.nn.nn where each KLE can have 1, 2 or all three parts.
		 * @property {string} name The common name of the KLE topic.
		 * @property {string} serviceText A description of the KLE including HTML formatting.
		 */
		function KLE(id, number, name, serviceText){
			return {
				id:id,
				number:number,
				name:name,
				serviceText:serviceText
			};
		}

		/**
		 * @class DistributionRule
		 * @private
		 * @type {Object}
		 * @property {number} id The system id.
		 * @property {number} parent The system id of the parent rule.
		 * @property {KLE} kle The KLE that this rule applies for.
		 * @property {number} org The system id of the org that handles request on KLE.
		 * @property {number} employee The system id of the employment that handles request on KLE.
		 * @property {number} responsible The system id of the employment that manages this rule.
		 */
		function DistributionRule(id, parent, kle, org, employee, responsible){
			return {
				id:id,
				parent: parent,
				kle:kle,
				org:org,
				employee:employee,
				responsible:responsible
			};
		}

		/**
		 * @class Employment
		 * @private
		 * @type Object
		 * @property {number} id The system id.
		 * @property {string} name The name of the employee.
		 * @property {string} email The email of the employee.
		 * @property {string} esdhId Reference to employee id in ESDH system.
		 * @property {string} initials The initials of the employee.
		 * @property {string} jobTitle The job title of the employee.
		 */
		function Employment(id, name, email, esdhId, initials, jobTitle){
			return {
				id:id,
				name:name,
				email:email,
				esdhId:esdhId,
				intitials:initials,
				jobTitle: jobTitle
			};
		}

		// private methods

		function httpGet(url, params) {
			var options = {
				cache: true
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
				headers: requestConfig.headers
			};
			angular.extend(options, defaults); // merge defaults into options.
			appSpinner.showSpinner();
			return $http(options).then(function (response) {
						appSpinner.hideSpinner();
						console.log('**response from EXECUTE', response);
						return response.data;
					});
		}

		// mock functions

		function mockTopicRoutes() {
			var users = getUsers();
			var digiJon = users[4];
			var allan = users[1];
			var jon = users[2];


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
					children: [3, 4],
					open: true,
					visible: true
				}, {
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
				}, {
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
				}, {
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
				}, {
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
					children: [7, 8],
					open: true,
					visible: true
				}, {
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
				}, {
					id: 8,
					kle: {
						number: '85.04.02',
						name: 'KL autoriserede standardblanketter',
						type: 'topic'
					},
					orgUnit: 'Direktionssekretariatet',
					externalUnit: 'Ledelsessekretariatet',
					responsible: digiJon,
					visible: true
				}
			];
		}

		function getMockRoles() {
			return [
				{
					id: 1,
					employment: 1,
					name: 'Henrik (dig)',
					admin: false,
					municipalityAdmin: false,
					substitute: false
				}, {
					id: 2,
					name: 'Admin',
					admin: true,
					municipalityAdmin: false,
					substitute: false
				}, {
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
				2: {
					id: 5,
					name: 'Jon Badstue Pedersen'
				}
			};
		}

		function getOrganisations() {
			return {
				1: "Udvikling",
				2: "Direktionssekretariatet",
				3: "It",
				4: "Digitalisering"
			};
		}

		function createKLE(number, name, type, serviceText) {
			return {
				number: number,
				name: name,
				type: type,
				serviceText: serviceText
			};
		}
	}
})();