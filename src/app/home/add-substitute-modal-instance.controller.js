(function () {
	'use strict';
	angular.module('topicRouter').controller('AddSubstituteModalInstanceCtrl', AddSubstituteModalInstanceCtrl);

	AddSubstituteModalInstanceCtrl.$inject = ['$log', '$scope', '$modalInstance', 'topicRouterApi'];

	function AddSubstituteModalInstanceCtrl($log, $scope, $modalInstance, topicRouterApi){
		//$scope.employeeFilter = "";
		$scope.modalAlerts = [];
		$scope.search = {
			municipalityId: $scope.user.municipality.id,
			offset: 0,
			pageSize: 10,
			nameTerm: '',
			initialsTerm: ''
		};
		$scope.searchResult = {
			totalMatches: 0,
			results: []
		};

		$scope.employmentSearch = employmentSearch;
		$scope.loadMoreEmployments = loadMoreEmployments;
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.setCurrentEmployment = setCurrentEmployment;
		$scope.closeAlert = closeAlert;

		var currentEmployment = $scope.user.currentRole.employment;

		activate();

		function activate(){
			$log.info("AddSubstituteModal::activate");
			topicRouterApi.getEmployments($scope.user.municipality.id, currentEmployment, true).then(function(employments){
				$scope.employments = employments;
			});
		}

		function employmentSearch(){
			$scope.search.offset = 0;
			topicRouterApi.employmentSearch($scope.search).then(function(result){
				$scope.searchResult = result;
				$scope.employments = result.results;
			});
		}

		function loadMoreEmployments(){
			$log.warn('fetching more employments...');
			$scope.search.offset = $scope.search.offset + $scope.search.pageSize;
			topicRouterApi.employmentSearch($scope.search).then(function(result){
				$scope.searchResult = result;
				$scope.employments = $scope.employments.concat(result.results);
			});
		}


		function ok(){
			if($scope.currentEmployment){
				$modalInstance.close($scope.currentEmployment);
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal vælge en medarbejder.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function setCurrentEmployment(employment){
			$scope.currentEmployment = employment;
		}

		function addAlert(alert) {
			$scope.modalAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.modalAlerts.splice(index, 1);
		}
	}
})();
