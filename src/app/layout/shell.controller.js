(function () {
    'use strict';

    angular.module('topicRouter').controller('ShellCtrl', ShellCtrl);

    ShellCtrl.$inject = ['$scope', '$rootScope', '$state', 'topicRouterApi'];

    function ShellCtrl($scope, $rootScope, $state, topicRouterApi) {
        /* jshint validthis:true */
        var vm = this;
        $scope.$state = $state;

        $scope.alerts = [];
        $scope.addAlert = addAlert;
        $scope.closeAlert = closeAlert;

        $scope.sidemenuVisible = true;
        $scope.settings = {
            showResponsible: false
        };

        $scope.roles = [];
        $scope.changeRole = changeRole;

        $scope.filter = {
            text: '',
            whichTasks: 'all'
        };
        $scope.updateFilter = updateFilter;

        vm.showSpinner = false;
        vm.spinnerMessage = 'Retrieving data...';

        vm.spinnerOptions = {
            radius: 40,
            lines: 8,
            length: 0,
            width: 30,
            speed: 1.7,
            corners: 1.0,
            trail: 100,
            color: '#428bca'
        };

        activate();

        function activate() {
            topicRouterApi.getRoles().then(function(data){
                $scope.roles = data;
                $scope.settings.currentUser = data[0];
            });
        }

        $rootScope.$on('spinner.toggle', function (event, args) {
            vm.showSpinner = args.show;
            if (args.message) {
                vm.spinnerMessage = args.message;
            }
        });

        function addAlert(alert){
            $scope.alerts.push(alert);
        }

        function closeAlert(index){
            $scope.alerts.splice(index, 1);
        }

        function changeRole(){
            console.log('switching user');
            console.log($scope.settings.currentUser);
            console.log($state);
        }

        function updateFilter(){
            console.log($scope.filter);
        }
    }
})();
