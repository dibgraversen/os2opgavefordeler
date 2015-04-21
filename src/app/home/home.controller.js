(function () {
    'use strict';

    angular.module('topicRouter').controller('HomeCtrl', HomeCtrl);

    HomeCtrl.$inject = ['$scope', 'topicRouterApi', '$state', 'flash'];

    /* @ngInject */
    function HomeCtrl($scope, topicRouterApi, $state, flash) {
        /* jshint validthis: true */
        var vm = this;
        vm.notesCollapsed = true;
        vm.navigate = navigate;
        vm.activate = activate;

        $scope.topicRoutes = getTopicRoutes();

        activate();

        // API
        vm.save = save;

        ////////////////

        function activate() {
            console.log('home controller init');
            //console.log('current state data', $state.current.data);
        }

        function navigate(){
            //$state.go('leagues');
        }

        function getTopicRoutes(){
            return topicRouterApi.getTopicRoutes();
        }

        function save(){
            flash.addMessage({
                level: 'success',
                text: "Dine ændringer er gemt"
            });

        }

    }
})();