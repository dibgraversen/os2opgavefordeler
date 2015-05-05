(function () {
    'use strict';

    var app = angular.module('topicRouter');

    app.controller('HomeCtrl', HomeCtrl);

    HomeCtrl.$inject = ['$scope', 'topicRouterApi', '$state', '$q', '$modal', 'serverUrl'];

    /* @ngInject */
    function HomeCtrl($scope, topicRouterApi, $state, $q, $modal, serverUrl) {
        /* jshint validthis: true */

        $scope.topicRoutes = [];
        $scope.filteredTopicRoutes = [];
        var nodes = {};

        activate();

        // API
        $scope.save = save;
        $scope.toggle = toggle;
        $scope.editRule = editRule;

        ////////////////

        function activate() {
            console.log(serverUrl);
            getTopicRoutes().then(function(data){
                $scope.topicRoutes = data;
                $scope.filteredTopicRoutes = data;
                _.each(data, function(item){
                    nodes[item.id] = item;
                });
            });
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

        function editRule(topic){
            $modal.open({
                //scope: $scope,
                resolve: {
                    topic: function(){
                        return topic;
                    }
                },
                templateUrl: 'app/home/edit-rule-modal.html',
                controller: 'EditRuleModalInstanceCtrl',
                size: 'lg'
            });
        }
    }
})();