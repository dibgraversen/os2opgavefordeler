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
			updateDistributionRule: updateDistributionRule,
			getRuleChildren: getRuleChildren,
			updateUser: updateUser,
			deleteUser: deleteUser,
			getRoles: getRoles,
			getAllUsers: getAllUsers,
			getSettings: getSettings,
			updateSettings: updateSettings,
			setMunicipalityAdmin: setMunicipalityAdmin,
			setAdmin: setAdmin,
			setKleAdmin: setKleAdmin,
			getOrgUnitsForResponsibility: getOrgUnitsForResponsibility,
			getOrgUnit: getOrgUnit,
			getEmployments: getEmployments,
			getEmployment: getEmployment,
			employmentSearch: employmentSearch,
			getSubstitutes: getSubstitutes,
			addSubstitute: addSubstitute,
			removeSubstitute: removeSubstitute,
			getMunicipalities: getMunicipalities,
			createMunicipality: createMunicipality,
			updateMunicipality: updateMunicipality,
			deleteMunicipality: deleteMunicipality,
			buildRules: buildRules,
			getKlesForMunicipality: getKlesForMunicipality,
			saveMunicipalityKle: saveMunicipalityKle,
			deleteMunicipalityKle: deleteMunicipalityKle,
			getApiKey: getApiKey,
			saveApiKey: saveApiKey,
			getTextParamsForMunicipality: getTextParamsForMunicipality,
			getDateParamsForMunicipality: getDateParamsForMunicipality,
			setDefaultTextParamForMunicipality: setDefaultTextParamForMunicipality,
			getDefaultTextParamForMunicipality: getDefaultTextParamForMunicipality,
			setDefaultDateParamForMunicipality: setDefaultDateParamForMunicipality,
			getDefaultDateParamForMunicipality: getDefaultDateParamForMunicipality,
			deleteTextParameter: deleteTextParameter,
			deleteDateParameter: deleteDateParameter,
			updateDateParameterName: updateDateParameterName,
			updateTextParameterName: updateTextParameterName,
			getFullLog: getFullLog,
			getFullLogCsv: getFullLogCsv,

			//
			getFiltersForRule: getFiltersForRule,
			removeFilter: removeFilter,
			createFilter: createFilter
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

		var cache = $cacheFactory('cache');

		return service;

		function getFiltersForRule(ruleId) {
			return httpGet("/distributionrulefilter/" + ruleId + "/filters");
		}

		function createFilter(model) {
			return httpPost("/distributionrulefilter/", model);
		}

		function removeFilter(ruleId, filterId) {
			return httpDelete("/distributionrulefilter/" + ruleId + "/" + filterId);
		}

		function updateFilter(model) {
			return post("/distributionrulefilter/" + model.distributionRuleId + "/filters/" + model.filterId, model);
		}

		function getIdentityProviders() {
			return httpGet("/auth/providers");
		}

		function updateUser(model) {
			return httpPost("/users/", model);
		}

		function deleteUser(userId) {
			return httpDelete('/users/' + userId);
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

		function setMunicipalityAdmin(roleId, municipalityAdmin) {
			return httpPost('/roles/' + roleId + '/municipalityadmin/' + (municipalityAdmin ? '1' : '0'));
		}

		function setAdmin(roleId, admin) {
			return httpPost('/roles/' + roleId + '/admin/' + (admin ? '1' : '0'));
		}

		function setKleAdmin(roleId, kleAdmin) {
			return httpPost('/roles/' + roleId + '/kleAdmin/' + (kleAdmin ? '1' : '0'));
		}

		function getTopicRoutes(role, scope) {
			var deferred = $q.defer();

			httpGet('/distribution-rules', {
				"role": role,
				"scope": scope
			})
					.then(function (data) {
						var objectMap = {};

						_.each(data, function (rule) {
							objectMap[rule.id] = rule;
							rule.children = [];
						});

						var rulePromises = [];
						_.each(data, function (rule) {
							rulePromises.push(processRule(rule, objectMap));
						});

						$q.all(rulePromises).then(function () {
							deferred.resolve(data);
						});
					}, function(error) {
						deferred.reject(error);
					}
			);
			return deferred.promise;
		}

		function getRuleChildren(ruleId, employment, scope) {
			return httpGet('/distribution-rules/' + ruleId + '/children', {
				"employment": employment,
				"scope": scope
			}).then(function (rules) {
				var objectMap = {};

				_.each(rules, function (rule) {
					objectMap[rule.id] = rule;
					rule.children = [];
				});

				_.each(rules, function (rule) {
					processRule(rule, objectMap);
				});

				_.each(rules, function (rule) {
					rule.kle.serviceTextPopover = htmlsave.truncate(rule.kle.serviceText, maxPopoverLength, {breakword: false});
				});

				return rules;
			});
		}

		function processRule(rule, objectMap) {
			var deferred = $q.defer();

			rule.visible = true;
			rule.open = (rule.children && rule.children.length > 0);
			rule.kle.serviceTextPopover = htmlsave.truncate(rule.kle.serviceText, maxPopoverLength, {breakword: false});

			var promises = [];

			if (rule.org > 0) {
				promises.push(getOrgUnit(rule.org).then(function (orgUnit) {
					rule.org = orgUnit;
				}));
			}

			if (rule.responsible > 0) {
				promises.push(getOrgUnit(rule.responsible).then(function (orgUnit) {
					rule.responsible = orgUnit;
				}));
			}

			if (rule.employee > 0) {
				promises.push(getEmployment(rule.employee).then(function (employee) {
					rule.employee = employee;
				}));
			}

			if (rule.parent) {
				var parent = objectMap[rule.parent];

				if (parent) {
					rule.parent = parent;
					parent.children.push(rule.id);
					parent.open = true;
				}
			}

			$q.all(promises).then(function () {
				deferred.resolve();
			});

			return deferred.promise;
		}

		function getOrgUnit(orgId) {
			var deferred = $q.defer();

			var promises = [];

			promises.push(httpGet('/org-units/' + orgId).then(function (orgUnit) {
				if (orgUnit.managerId > 0) {
					promises.push(getEmployment(orgUnit.managerId).then(function (employment) {
						orgUnit.manager = employment;
					}));
				}
				if (orgUnit.parentId > 0 && orgUnit.parent === undefined) {
					promises.push(getOrgUnit(orgUnit.parentId).then(function (loadedParent) {
						orgUnit.parent = loadedParent;
					}));
				}
				$q.all(promises).then(function () {
					deferred.resolve(orgUnit);
				});
			}));
			return deferred.promise;
		}

		function getRoles(userId) {
			return httpGet('/users/' + userId + '/roles');
		}

		function getAllUsers() {
			return httpGet('/users');
		}

		/**
		 * Returns a list of orgUnits to choose from.
		 * @returns {Object[]} OrgUnit - A list of all OrgUnits.
		 */
		function getOrgUnitsForResponsibility(municipalityId, currentEmploymentId, managedOnly) {
			var params = {municipalityId: municipalityId};
			if (managedOnly && currentEmploymentId) {
				params.employmentId = currentEmploymentId;
			}
			return httpGet('/org-units', params).then(function (orgUnits) {
				// fetch manager employments
				_.each(orgUnits, function (orgUnit) {
					if (orgUnit.managerId > 0) {
						getEmployment(orgUnit.managerId).then(function (employment) {
							orgUnit.manager = employment;
						});
					}
				});
				if (currentEmploymentId) {
					setSubordinate(orgUnits, currentEmploymentId);
				}
				return orgUnits;
			});
		}

		function setSubordinate(orgUnits, currentEmploymentId) {
			if (orgUnits && orgUnits.length && orgUnits.length > 0) {
				var orgUnitMap = {};
				_.each(orgUnits, function (orgUnit) {
					orgUnitMap[orgUnit.id] = orgUnit;
				});
				_.each(orgUnits, function (orgUnit) {
					isSubordinate(orgUnit, currentEmploymentId, orgUnitMap);
				});
			}
		}

		function isSubordinate(orgUnit, currentEmploymentId, orgUnitMap) {
			if (orgUnit.managerId === currentEmploymentId) orgUnit.subordinate = true;
			else if (orgUnit.parentId > 0) {
				var parent = orgUnitMap[orgUnit.parentId];
				orgUnit.subordinate = isSubordinate(parent, currentEmploymentId, orgUnitMap);
			} else {
				orgUnit.subordinate = false;
			}
			return orgUnit.subordinate;
		}

		function getEmployments(municipalityId, employmentId, managedOnly) {
			var params = {
				municipalityId: municipalityId,
				employmentId: employmentId
			};
			if (employmentId && managedOnly) {
				params.managedOnly = true;
			}
			return httpGet('/employments', params);
		}

		function getEmployment(empId) {
			return httpGet('/employments/' + empId);
		}

		function employmentSearch(search) {
			return httpPost('/search/employments', search);
		}

		// updateType is either 'distribution' for distribution rules or 'responsibility' for assigning responsibility
		function updateDistributionRule(distributionRule, updateType) {
			var employee = null;
			if (distributionRule.employee) {
				employee = distributionRule.employee.id;
			}
			var distRule = new DistributionRule(distributionRule.id, distributionRule.parent.id,
					new KLE(distributionRule.kle.id, distributionRule.number, distributionRule.name, distributionRule.serviceText),
					distributionRule.org.id, employee, distributionRule.responsible.id);
			return httpPost('/distribution-rules/' + distributionRule.id + '?type=' + updateType, distRule);
		}

		function getSubstitutes(userRole) {
			return httpGet("/roles/" + userRole + "/substitutes");
		}

		function addSubstitute(userRole, substituteEmployment) {
			return httpPost("/roles/" + userRole + "/substitutes/add?employmentId=" + substituteEmployment);
		}

		function removeSubstitute(substitute) {
			$log.info("trying to remove ", substitute);
			return httpDelete("/roles/" + substitute.roleId);
		}

		function getMunicipalities() {
			return httpGet('/municipalities');
		}

		/**
		 * @param name The name of the municipality
		 */
		function createMunicipality(name) {
			return httpPost('/municipalities', {name: name});
		}

		function updateMunicipality(municipality) {
			return httpPost('/municipalities/' + municipality.id, municipality);
		}

		function deleteMunicipality(municipality) {
			return httpDelete('/municipalities/' + municipality.id);
		}

		function buildRules(municipality) {
			return httpGet('/distribution-rules/buildRules', {
				"municipalityId": municipality.id
			});
		}

		function getKlesForMunicipality(municipality) {
			if (municipality) {
				$log.info(municipality);
				return httpGet('/municipalities/' + municipality.id + '/kle');
			}
		}

		function saveMunicipalityKle(kle) {
			return httpPost('/municipalities/' + kle.municipalityId + '/kle', kle);
		}

		function deleteMunicipalityKle(kle) {
			return httpDelete('/municipalities/' + kle.municipalityId + '/kle/' + kle.id);
		}

		function getApiKey(municipality) {
			if (municipality) {
				return httpGet('/municipalities/' + municipality.id + '/apikey');
			}
		}

		function saveApiKey(municipality, apiKey) {
			return httpPost('/municipalities/' + municipality + '/apikey/' + apiKey);
		}

		function getTextParamsForMunicipality(municipality) {
			return httpGet('/distribution-rules/text/names', {"municipalityId": municipality.id});
		}

		function getDateParamsForMunicipality(municipality) {
			return httpGet('/distribution-rules/date/names', {"municipalityId": municipality.id});
		}

		function setDefaultTextParamForMunicipality(municipality, textParam) {
			return httpPost('/distribution-rules/text/names/default/' + textParam.id + '?municipalityId=' + municipality.id);
		}

		function getDefaultTextParamForMunicipality(municipality) {
			return httpGet('/distribution-rules/text/names/default' + '?municipalityId=' + municipality.id);
		}

		function setDefaultDateParamForMunicipality(municipality, dateParam) {
			return httpPost('/distribution-rules/date/names/default/' + dateParam.id + '?municipalityId=' + municipality.id);
		}

		function getDefaultDateParamForMunicipality(municipality) {
			return httpGet('/distribution-rules/date/names/default' + '?municipalityId=' + municipality.id);
		}

		function deleteDateParameter(municipality, parameter) {
			return httpDelete('/distribution-rules/date/names/' + parameter.id + '?municipalityId=' + municipality.id);
		}

		function deleteTextParameter(municipality, parameter) {
			return httpDelete('/distribution-rules/text/names/' + parameter.id + '?municipalityId=' + municipality.id);
		}

		function updateDateParameterName(municipality, parameter) {
			$log.info('Updating date parameter: ' + JSON.stringify(parameter));

			return httpPost('/distribution-rules/date/names' + '?municipalityId=' + municipality.id, parameter);
		}

		function updateTextParameterName(municipality, parameter) {
			$log.info('Updating text parameter: ' + JSON.stringify(parameter));

			return httpPost('/distribution-rules/text/names' + '?municipalityId=' + municipality.id, parameter);
		}

		function getFullLog() {
			return httpGet('/auditlog');
		}

		function getFullLogCsv() {
			return httpGet('/auditlog/csv');
		}

		// DTO classes.

		/**
		 * @class User
		 * @private
		 * @type {Object}
		 * @param id {number}
		 * @param name {string}
		 * @param loggedIn {boolean}
		 * @param municipality {Municipality}
		 * @returns {{id: *, name: *, loggedIn: *, municipality: *}}
		 * @constructor
		 */
		function User(id, name, loggedIn, municipality) {
			return {
				id: id,
				name: name,
				loggedIn: loggedIn,
				municipality: municipality
			};
		}

		/**
		 * @class Municipality
		 * @private
		 * @type {Object}
		 * @param id {number}
		 * @param name {string}
		 * @param active {boolean}
		 * @returns {{id: *, name: *, active: *}}
		 * @constructor
		 */
		function Municipality(id, name, active) {
			return {
				id: id,
				name: name,
				active: active
			};
		}

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
				id: id,
				parentId: parentId,
				managerId: managerId,
				name: name,
				esdhId: esdhId,
				email: email,
				phone: phone
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
		function KLE(id, number, name, serviceText) {
			return {
				id: id,
				number: number,
				name: name,
				serviceText: serviceText
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
		function DistributionRule(id, parent, kle, org, employee, responsible) {
			return {
				id: id,
				parent: parent,
				kle: kle,
				org: org,
				employee: employee,
				responsible: responsible
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
		function Employment(id, name, email, esdhId, initials, jobTitle) {
			return {
				id: id,
				name: name,
				email: email,
				esdhId: esdhId,
				intitials: initials,
				jobTitle: jobTitle
			};
		}

		/**
		 * @class EmploymentSearch
		 * @private
		 * @type Object
		 * @param page {number} page for the search
		 * @param pageSize {number} pageSize
		 * @param nameTerm {string}
		 * @param initialsTerm {string}
		 * @returns {{page: *, pageSize: *, nameTerm: *, initialsTerm: *}}
		 * @constructor
		 */
		function EmploymentSearch(page, pageSize, nameTerm, initialsTerm, totalMatches, results) {
			return {
				page: page,
				pageSize: pageSize,
				nameTerm: nameTerm,
				initialsTerm: initialsTerm
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
			cache.removeAll();
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