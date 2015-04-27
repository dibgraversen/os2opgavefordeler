(function () {
    'use strict';

    angular.module('topicRouter').controller('HomeCtrl', HomeCtrl);

    HomeCtrl.$inject = ['$scope', 'topicRouterApi', '$state', '$q'];

    /* @ngInject */
    function HomeCtrl($scope, topicRouterApi, $state, $q) {
        /* jshint validthis: true */
        //var vm = this;
        //vm.notesCollapsed = true;
        //vm.navigate = navigate;
        //vm.activate = activate;

        $scope.filterText = '';
        $scope.topicRoutes = null;
        var nodes = {};

        activate();

        // API
        $scope.save = save;
        $scope.toggle = toggle;

        ////////////////

        function activate() {
            console.log('home controller init');
            // TODO byg nodes map;

            getTopicRoutes().then(function(data){
                $scope.topicRoutes = data;
                _.each(data, function(item){
                    nodes[item.id] = item;
                });
            });

            // TODO $q this.
        }

        function navigate(){
            //$state.go('leagues');
        }

        function getTopicRoutes(){
            return topicRouterApi.getTopicRoutes();
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


        function toggleChildren(children, visible){
            if(children){
                _.each(children, function(childId){
                    var child = nodes[childId];
                    child.visible = visible;
                    toggleChildren(child.children, child.open && visible);
                });
            }
        }

    }
})();