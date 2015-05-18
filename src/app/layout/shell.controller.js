(function () {
    'use strict';

    angular.module('topicRouter').controller('ShellCtrl', ShellCtrl);

    ShellCtrl.$inject = ['$scope', '$rootScope', '$state', '$timeout', 'topicRouterApi'];

    function ShellCtrl($scope, $rootScope, $state, $timeout, topicRouterApi) {
        /* jshint validthis:true */
        var vm = this;
        $scope.$state = $state;

        $scope.alerts = [];
        $scope.addAlert = addAlert;
        $scope.closeAlert = closeAlert;

        $scope.sidemenuVisible = true;
        $scope.settings = {};
        $scope.user = {};
        $scope.updateSettings = updateSettings;

        $scope.changeRole = changeRole;

        $scope.filter = {
            text: '',
            whichTasks: 'all'
        };
        $scope.updateFilter = updateFilter;

        vm.showSpinner = false;
        vm.spinnerMessage = 'Henter data...';

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
            topicRouterApi.getSettings(1).then(function(data){
                $scope.settings = data;
            });

            topicRouterApi.getRoles().then(function(data){
                $scope.user.roles = data;
                $scope.user.currentRole = data[0];
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

        function changeRole(role){
            console.log('switching user');
            //$scope.user.currentRole = role;
            console.log($scope.user);
        }

        function updateFilter(){
            console.log($scope.filter);
        }

        function updateSettings(){
            topicRouterApi.updateSettings(1, $scope.settings).then(
                function(data){
                    var alert = {
                        msg: "Indstillinger gemt",
                        type: 'success'
                    };
                    addAlert(alert);
                    $timeout(closeAlert, 2000, alert);
                }, function(reason){
                    addAlert({
                        msg: 'Indstillinger kunne ikke gemmes. Prøv igen senere...',
                        type: 'danger'
                    });
                });
        }
    }
})();
