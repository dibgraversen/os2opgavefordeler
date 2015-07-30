(function () {
	'use strict';

	angular.module('topicRouter').factory('topicRouterApi', topicRouterApi);

	topicRouterApi.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	var maxPopoverLength = 150;

	// NOTE consider implementing ngResource to manage RESTful resource endpoints.
	function topicRouterApi($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {
		var service = {
			getIdentityProviders: getIdentityProviders,
			getUserInfo: getUserInfo,
			logoutUser: logoutUser,
			getTopicRoutes: getTopicRoutes,
			getRoles: getRoles,
			getSettings: getSettings,
			updateSettings: updateSettings,
			getOrgUnitsForResponsibility: getOrgUnitsForResponsibility,
			getEmployments: getEmployments,
			getEmployment: getEmployment,
			updateDistributionRule: updateDistributionRule,
			addSubstitute: addSubstitute,
			removeSubstitute: removeSubstitute,
			getMunicipalities: getMunicipalities,
			createMunicipality: createMunicipality
		};

		var baseUrl = serverUrl;
		var requestConfig = {
			headers: {
				'Content-Type': 'application/json',
				'Cache-Control': 'no-cache'
			}
		};

		var cache = $cacheFactory('cache');

		return service;

		function getIdentityProviders() {
			return httpGet("/auth/providers");
		}

		function getUserInfo() {
			return httpGet('/users/me');
		}

		function logoutUser() {
			return httpPost('/auth/logout', {});
		}

		function getSettings(userId) {
			return httpGet('/users/' + userId + '/settings');
		}

		function updateSettings(userId, settings) {
			return httpPost('/users/' + userId + '/settings', settings);
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
					if(rule.responsible > 0) {
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
			return httpGet('/org-units/'+orgId).then(function(orgUnit){
				if(orgUnit.managerId > 0){
					getEmployment(orgUnit.managerId).then(function(employment){
						orgUnit.manager = employment;
					});
				}
				return orgUnit;
			});
		}

		function getRoles(userId) {
			return httpGet('/users/' + userId + '/roles');
		}

		/**
		 * Returns a list of orgUnits to choose from.
		 * @returns {Object[]} OrgUnit - A list of all OrgUnits.
		 */
		function getOrgUnitsForResponsibility(municipalityId){
			return httpGet('/org-units', { municipalityId: municipalityId }).then(function(orgUnits){
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

		function getEmployments(municipalityId){
			return httpGet('/employments', { municipalityId: municipalityId });
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

		function addSubstitute(userRole, substituteEmployment) {
			return httpPost("/roles/" + userRole + "/substitutes/add?employmentId=" + substituteEmployment);
		}

		function removeSubstitute(substitute) {
			$log.info("trying to remove ", substitute);
			return httpDelete("/roles/" + substitute.roleId);
		}

		function getMunicipalities(){
			return httpGet('/municipalities');
		}

		/**
		 * @param name The name of the municipality
		 */
		function createMunicipality(name){
			return httpPost('/municipalities', { name: name });
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
				cache: cache
			};
			if (params) {
				//options.params = encodeURIComponent( JSON.stringify(params) );
				options.params = params;
			}
			return httpExecute(url, 'GET', options);
		}

		function httpDelete(url) {
			return httpExecute(url, 'DELETE', {});
		}

		function httpPost(url, data) {
			cache.removeAll();
			return httpExecute(url, 'POST', {data: data});
		}

		function httpExecute(requestUrl, method, options) {
			var defaults = {
				url: baseUrl + requestUrl,
				method: method,
				withCredentials: true,
				headers: requestConfig.headers
			};
			angular.extend(options, defaults); // merge defaults into options.
			appSpinner.showSpinner();
			//TODO: replace the following with Interceptor, so calling code can use success/error.
			return $http(options).then(
					function (response) {
						appSpinner.hideSpinner();
						$log.info('**response from EXECUTE', response);
						return response.data;
					},
					function(reason){
						appSpinner.hideSpinner();
						$log.info('**response from EXECUTE', reason);
					});
		}
	}
})();