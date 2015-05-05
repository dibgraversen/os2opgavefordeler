(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('EditRuleModalInstanceCtrl', EditRuleModalInstanceCtrl);

	EditRuleModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topic'];

	function EditRuleModalInstanceCtrl($scope, $modalInstance, topic){
		$scope.topic = topic;

		$scope.ok = ok;
		$scope.cancel = cancel;

		activate();

		function activate(){
			console.log(topic);
			// load some org. stuff.
		}

		function ok(){
			console.log("ok'ed modal.");
			$modalInstance.close(/* pass some value? */);
		}

		function cancel(){
			console.log("cancelled modal");
			$modalInstance.dismiss('cancel');
		}

	}
})();
