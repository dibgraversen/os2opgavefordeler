(function () {
	'use strict';

	angular.module('topicRouter').controller('SettingsCtrl', SettingsCtrl);

	/* @ngInject */
	function SettingsCtrl() {
		/* jshint validthis: true */
		var vm = this;
		vm.notesCollapsed = true;
		vm.navigate = navigate;
		vm.activate = activate;

		activate();

		////////////////

		function activate() {
			console.log('settings controller');
		}

		function navigate(){
		}

	}
})();