(function (){
	'use strict';

	angular.module('topicRouter').controller('EditApiKeyModalInstanceCtrl', EditApiKeyModalInstanceCtrl);

	EditApiKeyModalInstanceCtrl.$inject = ['$scope', '$modalInstance', '$log', 'topicRouterApi', 'apiKey', 'municipality'];

	function EditApiKeyModalInstanceCtrl($scope, $modalInstance, $log, topicRouterApi, apiKey, municipality){
		$scope.messages = [];
		$scope.titleText = 'Redigér API key';
		$scope.saveText = 'Opret';

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.closeMessage = closeMessage;

		activate();

		function activate(){
			if (apiKey) {
				// phrasing
				$scope.titleText = 'Opdatér API key';
				$scope.saveText = 'Gem';

				// populate locals
				//$scope.apiKey = apiKey;
			}
		}

		function ok(){
			// reset messages.
			$scope.messages.splice(0, $scope.messages.length);

			$log.info('API key: ' + JSON.stringify(apiKey));

			// validate
			var apiKeyValid = validateApiKey(apiKey.apiKey);

			if(apiKeyValid){
				topicRouterApi.saveApiKey(municipality.id, apiKey.apiKey).then(
						function(savedApiKey){
							$modalInstance.close(savedApiKey);
						},
						function(error){
							addMessage({type: 'danger', msg: error.data});
						});
			}
		}

		function validateApiKey(apiKey){
			var valid = true;

			if(apiKey.length < 1){
				valid = false;
				addMessage({ type: 'warning', msg: 'Du skal angive en API key.'});
			}

			return valid;
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function addMessage(message){
			$scope.messages.push(message);
		}

		function closeMessage(index){
			$scope.messages.splice(index, 1);
		}
	}
})();