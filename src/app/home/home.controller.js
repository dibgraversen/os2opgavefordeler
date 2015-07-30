(function () {
	'use strict';
	angular.module('topicRouter').controller('HomeCtrl', HomeCtrl);

	HomeCtrl.$inject = ['$scope', 'topicRouterApi', '$state', '$q', '$modal', '$log', 'serverUrl'];

	/* @ngInject */
	function HomeCtrl($scope, topicRouterApi, $state, $q, $modal, $log, serverUrl) {
		/* jshint validthis: true */

		$scope.topicRoutes = [];
		$scope.filteredTopicRoutes = [];
		var nodes = {};

		activate();

		// API
		$scope.substitutes = [];
		$scope.addSubstitute = addSubstitute;
		$scope.removeSubstitute = removeSubstitute;

		$scope.save = save;
		$scope.toggle = toggle;
		$scope.responsibility = responsibility;
		$scope.editRule = editRule;
		$scope.deleteRule = deleteRule;
		$scope.editResponsibility = editResponsibility;
		$scope.deleteResponsibility = deleteResponsibility;
		$scope.responsible = responsible;
		$scope.distributed = distributed;
		$scope.distribution = distribution;
		$scope.responsibilityChangeAllowed = responsibilityChangeAllowed;
		$scope.distributionChangeAllowed = distributionChangeAllowed;

		////////////////

		function activate() {
			$log.info("Home::activate");
			if(!$scope.user.loggedIn) {
				$state.go("login");
			}
			$scope.$watch("user.currentRole", function(newValue, oldValue){
				$log.info('Home:: user.currentRole changed', oldValue, " --> ", newValue);

				if(newValue && newValue.employment > 0) {
					refreshSubstitutes();
					refreshTopicRoutes();
				} else {
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
				controller: 'AddSubstituteModalInstanceCtrl'
			}).result.then(function(sub) {
					topicRouterApi.addSubstitute($scope.user.currentRole.id, sub.id).then(
						function(substitute) {
							$log.info("Substitute was added: ", substitute);
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

		function refreshTopicRoutes(){
			getTopicRoutes().then(function(data){
				$scope.topicRoutes = data;
				$scope.filteredTopicRoutes = data;
				_.each(data, function(item){
					nodes[item.id] = item;
				});
			});
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
			node.open = !node.open;
			toggleChildren(node.children, node.open);
		}

		/**
		 * Returns "true" if topic or parents has responsibility taken by org.
		 * @param topic
		 */
		function responsibility(topic){
			return topic.responsible || (topic.parent && responsibility(topic.parent));
		}

		function deleteResponsibility(topic){
			topic.responsible = 0;
			topicRouterApi.updateDistributionRule(topic);
		}

		function toggleChildren(children, visible){
			if(children){
				_.each(children, function(childId){
					var child = nodes[childId];
					child.visible = visible;
					toggleChildren(child.children, child.open && visible);
				});
			}
		}

		function editRule(topic){
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

		function editResponsibility(topic){
			$modal.open({
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
		function responsible(distributionRule){
			if(distributionRule.responsible){
				return distributionRule.responsible.name;
			} else if(distributionRule.parent){
				return responsible(distributionRule.parent);
			} else {
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
			if(distributionRule.org.name){
				return distributionRule.org.name;
			} else if(distributionRule.parent){
				return distribution(distributionRule.parent);
			} else {
				return  '';
			}
		}

		/**
		 * Determines if the users current role allows edit for given distributionRule
		 * @param distributionRule
		 * @return {boolean} true if edit allowed.
		 */
		function responsibilityChangeAllowed(distributionRule){
			$log.info($scope.user.currentRole.employment);
			if(!responsibility(distributionRule)) return true; // not already handled.
			if($scope.user.currentRole.municipalityAdmin) return true;
			if(canManage(distributionRule)) return true;
			return false;
		}

		function canManage(distributionRule){
			return (distributionRule.responsible && distributionRule.responsible.managerId > 0 &&
			distributionRule.responsible.managerId === $scope.user.currentRole.employment) ||
					(distributionRule.parent && canManage(distributionRule.parent));
		}

		function distributionChangeAllowed(distributionRule){
			if($scope.user.currentRole.municipalityAdmin) return true;
			if(canManage(distributionRule)) return true;
			return false;
		}
	}
})();