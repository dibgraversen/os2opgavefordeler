(function () {
    'use strict';

    angular.module('topicRouter').controller('ShellCtrl', ShellCtrl);

    ShellCtrl.$inject = ['$rootScope', 'flash'];

    function ShellCtrl($rootScope, flash) {
        /* jshint validthis:true */
        var vm = this;

        vm.deleteMessage = deleteMessage;

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

        vm.messages = flash.messages;

        activate();

        function activate() {

        }

        $rootScope.$on('spinner.toggle', function (event, args) {
            vm.showSpinner = args.show;
            if (args.message) {
                vm.spinnerMessage = args.message;
            }
        });

        function deleteMessage(message){
            flash.deleteMessage(message);
        }
    }
})();
