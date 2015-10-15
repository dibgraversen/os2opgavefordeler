(function (){
	'use strict';

	angular.module('topicRouter').controller('AddKleModalInstanceCtrl', AddKleModalInstanceCtrl);

	AddKleModalInstanceCtrl.$inject = ['$scope', '$modalInstance', '$log', 'topicRouterApi', 'municipality', 'kle'];

	function AddKleModalInstanceCtrl($scope, $modalInstance, $log, topicRouterApi, municipality, kle){
		$scope.messages = [];
		$scope.titleText = 'Tilføj kle';
		$scope.saveText = 'Opret';
		$scope.newName = '';
		$scope.newNumber = '';
		$scope.newServiceText = '';

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.closeMessage = closeMessage;

		activate();

		var oldGroup = false;

		function activate(){
			if(kle){
				// phrasing
				$scope.titleText = 'Opdatér kle';
				$scope.saveText = 'Gem';
				// populate locals
				$scope.newNumber = kle.number;
				$scope.newName = kle.name;
				$scope.newServiceText = kle.serviceText;
			}
		}

		function ok(){
			// reset messages.
			$scope.messages.splice(0, $scope.messages.length);
			// validate
			var numberValid = validateNumber($scope.newNumber);
			var nameValid = validateName($scope.newName);
			if(numberValid && nameValid){
				var newKle = {
					municipalityId: municipality.id,
					number: $scope.newNumber,
					name: $scope.newName,
					serviceText: $scope.newServiceText
				};
				if(kle){
					newKle.id = kle.id;
				} else {
					newKle.id = 0;
				}
				topicRouterApi.saveMunicipalityKle(newKle).then(
						function(savedKle){
							$modalInstance.close(savedKle);
						},
						function(error){
					    addMessage({type: 'danger', msg: error.data});
						});
			}
		}

		function validateNumber(number){
			var valid = true;
			if(number.length < 5 || number.length > 8){
				valid = false;
				addMessage({
					type: 'warning',
					msg: 'Du skal angive fulde nummer. Det er 5-8 tegn da du kun kan tilføje grupper og emner.'
				});
			}
			// tests digits and . only
			var allowed = /^[\d\.]*$/;
			if(!allowed.test(number)){
				valid = false;
				addMessage({ type: 'warning',
				msg: 'Du kan kun angive cifre og punktum.'});
			}
			// tests 2 digits then . then 80-99 then optional . and 80-99
			var range = /^[\d]{2}\.[8-9]\d(\.[8-9]\d)?$/;
			if(!range.test(number)){
				valid = false;
				addMessage({ type: 'warning',
				msg: 'Både gruppe og emne skal være fra 80-99 (begge inkl.). Hovedgruppe skal være eksisterende' });
			}
			return valid;
		}

		function validateName(name){
			var valid = true;
			if(name.length < 1){
				valid = false;
				addMessage({ type: 'warning', msg: 'Du skal angive en titel.'});
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