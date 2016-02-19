(function(){
	'use strict';

	angular.module('topicRouter').controller('ConfirmationModalInstanceCtrl', ConfirmationModalInstanceCtrl);

	ConfirmationModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'message'];

	function ConfirmationModalInstanceCtrl($scope, $modalInstance, message){
		$scope.message = message;
		$scope.ok = ok;
		$scope.cancel = cancel;

		function ok(){
			$modalInstance.close(true);
		}

		function cancel(){
			$modalInstance.close(false);
		}
	}
})();