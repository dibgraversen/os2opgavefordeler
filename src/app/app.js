(function(){
  'use strict';
  var app = angular.module('topicRouter', [
    // Angular modules
    'ngSanitize',
    'ngAnimate',

    // 3rd Party Modules
    'ui.bootstrap',
    'ui.router',
    'ui.alias',
    'app.config'
  ]);

  //app.config(['$routeProvider', configRoutes]);
  app.config(['$stateProvider', '$urlRouterProvider', configRoutes]);

  function configRoutes($stateProvider, $urlRouterProvider){
    $stateProvider
        .state('home', {
          url: '/',
          templateUrl: 'app/home/home.html',
          controller: 'HomeCtrl',
          controllerAs: 'vm'
        })
        .state('settings', {
          url: '/settings',
          templateUrl: 'app/settings/settings.html',
          controller: 'SettingsCtrl',
          controllerAs: 'vm'
        })
        .state('municipalityAdmin', {
          url: '/municipalityAdmin',
          templateUrl: 'app/municipality-admin/municipality-admin.html',
          controller: 'MunicipalityAdminCtrl',
          controllerAs: 'vm'
        });
    $urlRouterProvider.otherwise('/');
  }

  app.config(['$httpProvider', setupCors]);

  function setupCors($httpProvider){
    $httpProvider.defaults.useXDomain = true;
    delete $httpProvider.defaults.headers.common['X-Requested-With'];
  }

  app.run(['$state', function($state){
    // Include $route to kick start the router.
  }]);

})();