(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('HomeCtrl', HomeCtrl);

	HomeCtrl.$inject = ['$scope', 'topicRouterApi', '$state', '$q', '$modal', 'serverUrl'];

	/* @ngInject */
	function HomeCtrl($scope, topicRouterApi, $state, $q, $modal, serverUrl) {
		/* jshint validthis: true */

		$scope.topicRoutes = [];
		$scope.filteredTopicRoutes = [];
		var nodes = {};

		activate();

		// API
		$scope.save = save;
		$scope.toggle = toggle;
		$scope.responsibility = responsibility;
		$scope.editRule = editRule;
		$scope.editResponsibility = editResponsibility;
		$scope.deleteResponsibility = deleteResponsibility;
		$scope.responsible = responsible;
		$scope.distributed = distributed;
		$scope.distribution = distribution;

		////////////////

		function activate() {
			$scope.$watch("user.currentRole", function(newValue, oldValue){
				if(newValue && newValue.employment > 0){
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
				//scope: $scope,
				resolve: {
					topic: function(){
						return topic;
					}
				},
				templateUrl: 'app/home/edit-rule-modal.html',
				controller: 'EditRuleModalInstanceCtrl',
				size: 'lg'
			});
		}

		function editResponsibility(topic){
			$modal.open({
				resolve: {
					topic: function(){
						return topic;
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
	}
})();