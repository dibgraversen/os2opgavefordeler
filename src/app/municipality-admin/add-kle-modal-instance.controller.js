(function (){
	'use strict';

	angular.module('topicRouter').controller('AddKleModalInstanceCtrl', AddKleModalInstanceCtrl);

	AddKleModalInstanceCtrl.$inject = ['$scope', '$modalInstance', '$log', 'topicRouterApi', 'municipality', 'kle'];

	function AddKleModalInstanceCtrl($scope, $modalInstance, $log, topicRouterApi, municipality, kle){
		$scope.messages = [];
		$scope.titleText = 'Tilføj kle';
		$scope.saveText = 'Opret';
		$scope.kle = {
			name: '',
			number: '',
			serviceText: ''
		};

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.closeMessage = closeMessage;

		activate();

		var edit = false;

		function activate(){
			if(kle){
				$scope.titleText = 'Opdatér kle';
				$scope.saveText = 'Gem';
				$scope.kle = kle;
				edit = true;
			}
		}

		function ok(){
			// reset messages.
			$scope.messages.splice(0, $scope.messages.length);
			// validate
			var kle = $scope.kle;
			var numberValid = validateNumber(kle.number);
			var nameValid = validateName(kle.name);
			if(numberValid && nameValid){
				kle.municipalityId = municipality.id;
				topicRouterApi.saveMunicipalityKle(kle).then(
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