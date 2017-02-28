(function () {
	'use strict';
	var app = angular.module('topicRouter', [
// Angular modules
		'ngSanitize',
		'ngAnimate',

		// 3rd Party Modules
		'ui.alias',
		'ui.bootstrap',
		'ui.router',
		'ui.uploader',
		'ui.tree',
		'ui.indeterminate',
		'app.config'
	]);

	//app.config(['$routeProvider', configRoutes]);
	app.config(['$stateProvider', '$urlRouterProvider', configRoutes]);

	function configRoutes($stateProvider, $urlRouterProvider) {
		$stateProvider
				.state('home', {
					url: '/',
					templateUrl: '/app/home/home.html',
					controller: 'HomeCtrl',
					controllerAs: 'vm'
				})
				.state('settings', {
					url: '/settings',
					templateUrl: '/app/settings/settings.html',
					controller: 'SettingsCtrl',
					controllerAs: 'vm'
				})
				.state('municipalityAdmin', {
					url: '/municipality-admin',
					templateUrl: '/app/municipality-admin/municipality-admin.html',
					controller: 'MunicipalityAdminCtrl',
					controllerAs: 'vm'
				})
				.state('login', {
					url: "/login",
					templateUrl: '/app/layout/login.html',
					controller: 'LoginCtrl',
					controllerAs: 'vm'
				})
				.state('kleadmin', {
					url: "/kleadmin",
					templateUrl: '/app/kle-admin/kle-admin.html',
					controller: 'KleAdminCtrl',
					controllerAs: 'vm',
					//scope: '{}'
				});
		$urlRouterProvider.otherwise('/');
	}

	app.config(['$httpProvider', setupCors]);

	function setupCors($httpProvider) {
		$httpProvider.defaults.useXDomain = true;
		delete $httpProvider.defaults.headers.common['X-Requested-With'];
	}

	app.run(['$state', function ($state) {
		// Include $route to kick start the router.
	}]);

})();