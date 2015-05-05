(function () {
	'use strict';

	angular.module('topicRouter').controller('MunicipalityAdminCtrl', MunicipalityAdminCtrl);

	function MunicipalityAdminCtrl() {
		$scope.something = 'a string';

		activate();

		function activate() {
			console.log('municipality admin activated');
		}
	}
})();