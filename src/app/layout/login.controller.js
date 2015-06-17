(function () {
	'use strict';

	angular.module('topicRouter').controller('LoginCtrl', LoginCtrl);

	LoginCtrl.$inject = ['$scope', '$state', '$timeout', '$q', '$modal', 'topicRouterApi'];

	function LoginCtrl($scope, $state, $timeout, $q, $modal, topicRouterApi) {
		/* jshint validthis:true */
		var vm = this;

		activate();

		function activate() {
		}
	}
})();
