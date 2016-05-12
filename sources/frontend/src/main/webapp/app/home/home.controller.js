(function () {
	'use strict';
	angular.module('topicRouter').controller('HomeCtrl', HomeCtrl);

	HomeCtrl.$inject = ['$scope', 'topicRouterApi', '$state', '$q', '$modal', '$log', 'serverUrl'];

	/* @ngInject */
	function HomeCtrl($scope, topicRouterApi, $state, $q, $modal, $log, serverUrl) {
		/* jshint validthis: true */

		$scope.topicRoutes = [];
		$scope.filteredTopicRoutes = [];
		$scope.substitutes = [];
		$scope.listAlerts = [];

		activate();

		// API
		$scope.addSubstitute = addSubstitute;
		$scope.removeSubstitute = removeSubstitute;

		$scope.save = save;
		$scope.toggle = toggle;
		$scope.responsibility = responsibility;
		$scope.editRule = editRule;
		$scope.listFilters = listFilters;
		$scope.deleteRule = deleteRule;
		$scope.editResponsibility = editResponsibility;
		$scope.deleteResponsibility = deleteResponsibility;
		$scope.responsible = responsible;
		$scope.distributed = distributed;
		$scope.distribution = distribution;
		$scope.responsibilityChangeAllowed = responsibilityChangeAllowed;
		$scope.distributionChangeAllowed = distributionChangeAllowed;
		$scope.getChildren = getChildren;
		$scope.responsibleEmployee = responsibleEmployee;
		$scope.closeAlert = closeAlert;
		$scope.showServiceText = showServiceText;
		$scope.getManager = getManager;

		////////////////

		// keep all for data integrity;
		var orgUnits = {};

		function activate() {
			$log.info("Home::activate");
			if(!$scope.user.loggedIn) {
				$state.go("login");
			}
			$scope.$watch("user.currentRole", function(newValue, oldValue){
				$log.info('Home:: user.currentRole changed', oldValue, " --> ", newValue);
				if (newValue && newValue.employment > 0) {
					refreshSubstitutes();
					refreshTopicRoutes();
				}
				else {
					$scope.topicRoutes = [];
				}
			});

			$scope.$watch("settings.scope", function(){
				if($scope.user.currentRole && $scope.user.currentRole.employment)
					refreshTopicRoutes();
			});
		}

		function addSubstitute(){
			$modal.open({
				scope: $scope,
				templateUrl: 'app/home/add-substitute-modal.html',
				controller: 'AddSubstituteModalInstanceCtrl',
				size: 'lg'
			}).result.then(function(sub) {
					topicRouterApi.addSubstitute($scope.user.currentRole.id, sub.id).then(
						function(substitute) {
							$scope.substitutes.push(substitute);
						}
					);
				});
		}

		function removeSubstitute(substitute) {
			topicRouterApi.removeSubstitute(substitute)
				.then(function() {
					_.remove($scope.substitutes, function(sub) {
						return sub === substitute;
					});
				});
		}

		function refreshSubstitutes() {
			topicRouterApi.getSubstitutes($scope.user.currentRole.id)
				.then(function(substitutes) {
					$scope.substitutes = substitutes;
				});
		}

		function addToOrgCache(org){
			var existing = orgUnits[org.id];
			if(existing){
				org = existing;
			} else {
				orgUnits[org.id] = org;
			}
			return org;
		}

		function refreshTopicRoutes(){
			$scope.listAlerts = [];
			getTopicRoutes().then(
					function(rules){
						$scope.topicRoutes = rules;
						$scope.filteredTopicRoutes = rules;

						if (rules.length > 0) {
							_.each(rules, function(rule) {
								if (rule.org) {
									rule.org = addToOrgCache(rule.org);
								}

								if (rule.responsible) {
									rule.responsible = addToOrgCache(rule.responsible);
								}
							});

							_.each(rules, function(rule) {
								if (rule.parent && !rule.parent.childrenLoaded) {
									getChildren(rule.parent, true).then(function(children){
										rule.parent.childrenLoaded = true;
										toggleChildren(children, true);
									});
								}
							});
						}
						else {
							if ($scope.settings.scope === 'ALL'){
								addAlert({ type: 'info', msg: 'Der blev ikke fundet regler.' });
							}
							else {
								addAlert({ type: 'info', msg: 'Der blev ikke fundet regler for givne filtrering.' });
							}
						}
					},
					function(error){
					  $log.error('Error: '+ error.data);
					}
			);
		}

		function navigate(){
			//$state.go('leagues');
		}

		function getTopicRoutes(){
			return topicRouterApi.getTopicRoutes($scope.user.currentRole.employment, $scope.settings.scope);
		}

		function save(){
			$scope.addAlert({
				type: 'success',
				msg: "Dine ændringer er gemt"
			});
		}

		function toggle(node){
			getChildren(node).then(function(){
				node.open = !node.open;
				toggleChildren(node.children, node.open);
			});
		}

		/**
		 * Returns "true" if topic or parents has responsibility taken by org.
		 * @param topic
		 */
		function responsibility(topic) {
			return topic.responsible || (topic.parent && responsibility(topic.parent));
		}

		function deleteResponsibility(topic) {
			topic.responsible = 0;
			topicRouterApi.updateDistributionRule(topic);
		}

		function toggleChildren(children, visible) {
			if(children && children.length > 0) {
				_.each(children, function(child) {
					if (typeof child === 'number') {
						child = _.find($scope.topicRoutes, { 'id': child });
						child.visible = visible;
						toggleChildren(child.children, child.open && visible);
					}
					else if (typeof child === 'object') {
						child.visible = visible;
						toggleChildren(child.children, child.open && visible);
					}
				});
			}
		}

		function editRule(topic) {
			$modal.open({
				scope: $scope,
				resolve: {
					topic: function() {
						return topic;
					},
					municipality: function() {
						return $scope.user.municipality;
					}
				},
				templateUrl: 'app/home/edit-rule-modal.html',
				controller: 'EditRuleModalInstanceCtrl',
				size: 'lg'
			});
		}

		function deleteRule(topic) {
			topic.org = 0;
			topic.employee = 0;
			topicRouterApi.updateDistributionRule(topic);
		}

		function listFilters(topic) {
			$modal.open({
				scope: $scope,
				resolve: {
					topic: function(){
						return topic;
					},
					municipality: function(){
						return $scope.user.municipality;
					}
				},
				templateUrl: 'app/home/add-extended-responsibility-modal.html',
				controller: 'EditResponsibilityModalInstanceCtrl',
				size: 'lg'
			});
		}

		function editResponsibility(topic) {
			$modal.open({
				scope: $scope,
				resolve: {
					topic: function(){
						return topic;
					},
					municipality: function(){
						return $scope.user.municipality;
					}
				},
				templateUrl: 'app/home/edit-responsibility-modal.html',
				controller: 'EditResponsibilityModalInstanceCtrl',
				size: 'md'
			});
		}

		/**
		 * Used to find responsible among parent rules.
		 * @param {object} distributionRule
		 * @return {string} responsible
		 */
		function responsible(distributionRule) {
			if (distributionRule.responsible) {
				return distributionRule.responsible;
			}
			else if(distributionRule.parent) {
				return responsible(distributionRule.parent);
			}
			else {
				return '';
			}
		}

		/**
		 * Returns true if there is a distribution for current or parent node.
		 * @param distributionRule
		 */
		function distributed(distributionRule){
			return distributionRule.org || (distributionRule.parent && distributed(distributionRule.parent));
		}

		/**
		 * Returns first org name for current or parent node.
		 */
		function distribution(distributionRule){
			if (distributionRule.org && distributionRule.org.name){
				return distributionRule.org.name;
			}
			else if (distributionRule.parent){
				return distribution(distributionRule.parent);
			}
			else {
				return  '';
			}
		}

		/**
		 * Determines if the users current role allows edit for given distributionRule
		 * @param distributionRule
		 * @return {boolean} true if edit allowed.
		 */
		function responsibilityChangeAllowed(distributionRule){
			// handles case where none has taken responsibility
			if (!responsibility(distributionRule) && $scope.user.currentRole.manager) {
				return true; // not already handled.
			}

			if ($scope.user.currentRole.municipalityAdmin) {
				return true;
			}

			if (canManage(distributionRule)) {
				return true;
			}

			return false;
		}

		function canManage(rule){
			if(rule.responsible){ // org is set, chain stops here.
				return getManager(rule.responsible).id === $scope.user.currentRole.employment;
			} else {
				return rule.parent && canManage(rule.parent);
			}
		}

		function getManager(org){
			org = addToOrgCache(org);
			if(org.manager){
				return org.manager;
			} else if(org.parent) {
				return getManager(org.parent);
			}
		}

		function distributionChangeAllowed(distributionRule){
			if ($scope.user.currentRole.municipalityAdmin) {
				return true;
			}

			if (canManage(distributionRule)) {
				return true;
			}

			return false;
		}

		function getChildren(rule, force){
			var deferred = $q.defer();

			if (rule.type != 'topic'){
				if(force || !rule.children || rule.children.length < 1){
					topicRouterApi.getRuleChildren(rule.id, $scope.user.currentRole.employment, $scope.settings.scope).then(function(children){
						rule.children = _.collect(children, 'id');

						var promises = [];
						_.each(children, function(child){
							promises.push(prepareRule(child));
							child.parent = rule;
						});

						$q.all(promises).then(function(){
							addRules(children);
							deferred.resolve(children);
						});
					});
				} else {
					deferred.resolve();
				}
			} else {
				deferred.resolve();
			}
			return deferred.promise;
		}

		function responsibleEmployee(rule){
			// rule.org makes inherit chain break by explicit responsibility
			// i.e. resetting employee inheritance whenever an org is set on parent.
			if (rule.employee || rule.org) {
				return rule.employee.name;
			}
			else if (rule.parent) {
				return responsibleEmployee(rule.parent);
			}
			else {
				return '';
			}
		}

		function addRules(newRules){
			$scope.topicRoutes = _.sortBy(_.uniq(_.flatten([$scope.topicRoutes, newRules]), 'id'),
					function(rule) { return rule.kle.number; });
		}

		function prepareRule(rule){
			rule.open = false;
			rule.visible = true;

			if (typeof rule.org === "number" && rule.org > 0){
				return topicRouterApi.getOrgUnit(rule.org).then(function(org) {
					rule.org = org;
				});
			}
			else {
				var deferred = $q.defer();
			    deferred.resolve();
				return deferred.promise;
			}
		}

		function addAlert(message){
			$scope.listAlerts.push(message);
		}

		function closeAlert(index) {
			$scope.listAlerts.splice(index, 1);
		}

		function showServiceText(kle){
			$modal.open({
				templateUrl: 'app/common/kle-servicetext-modal.html',
				controller: 'KleServicetextModalInstanceCtrl',
				size: 'lg',
				resolve: {
					kle: function() {
						return kle;
					}
				}
			});
		}
	}
})();