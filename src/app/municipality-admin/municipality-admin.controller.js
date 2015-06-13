(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('MunicipalityAdminCtrl', MunicipalityAdminCtrl);

	MunicipalityAdminCtrl.$inject = ['$scope'];

	function MunicipalityAdminCtrl($scope) {
		$scope.something = 'a string';

		activate();

		function activate() {
			console.log('municipality admin activated');
		}
	}
})();