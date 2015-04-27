(function () {
    'use strict';

    angular.module('topicRouter').controller('ShellCtrl', ShellCtrl);

    ShellCtrl.$inject = ['$scope', '$rootScope'];
    function ShellCtrl($scope, $rootScope) {
        /* jshint validthis:true */
        var vm = this;

        $scope.messages = [];
        $scope.addMessage = addMessage;
        $scope.deleteMessage = deleteMessage;
        $scope.alerts = [];
        $scope.addAlert = addAlert;
        $scope.closeAlert = closeAlert;
        $scope.sidemenuVisible = false;
        $scope.viewSettings = {
            showResponsible: false
        };
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

        }

        $rootScope.$on('spinner.toggle', function (event, args) {
            vm.showSpinner = args.show;
            if (args.message) {
                vm.spinnerMessage = args.message;
            }
        });

        function addMessage(message){
            $scope.messages.push(message);
        }

        function deleteMessage(message){
            _.remove($scope.messages, {
                text: message.text
            });
        }

        function addAlert(alert){
            $scope.alerts.push(alert);
        }

        function closeAlert(index){
            $scope.alerts.splice(index, 1);
        }
    }
})();
