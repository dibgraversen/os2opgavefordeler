(function () {
	'use strict';

	angular.module('topicRouter').controller('SettingsCtrl', SettingsCtrl);

	/* @ngInject */
	function SettingsCtrl($state) {
		/* jshint validthis: true */
		var vm = this;
		vm.notesCollapsed = true;
		vm.navigate = navigate;
		vm.activate = activate;

		activate();

		////////////////

		function activate() {
			//console.log('current state data', $state.current.data);
			console.log('settings controller')
		}

		function navigate(){
			//$state.go('leagues');
		}

	}
})();