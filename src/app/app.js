(function(){
  'use strict';
  var app = angular.module('topicRouter', [
    // Angular modules
    'ngSanitize',
    'ngAnimate',

    // 3rd Party Modules
    'ui.bootstrap',
    'ui.router',
    'ui.alias'
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
        });
    $urlRouterProvider.otherwise('/');
  }

  app.run(['$state', function($state){
    // Include $route to kick start the router.
  }]);

})();