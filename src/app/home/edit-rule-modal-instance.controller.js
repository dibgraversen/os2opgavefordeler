(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('EditRuleModalInstanceCtrl', EditRuleModalInstanceCtrl);

	EditRuleModalInstanceCtrl.$inject = ['$scope', '$modalInstance', '$log', 'topicRouterApi', 'topic', 'municipality'];

	function EditRuleModalInstanceCtrl($scope, $modalInstance, $log, topicRouterApi, topic, municipality){
		$scope.topic = topic;
		$scope.orgUnits = [];
		$scope.employments = [];
		$scope.orgFilter = "";
		$scope.empFilter = "";
		$scope.ruleAlerts = [];
		$scope.showSubordinate = false;
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
		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.setSelectedOrgUnit = setSelectedOrgUnit;
		$scope.setSelectedEmp = setSelectedEmp;
		$scope.employmentSearch = employmentSearch;
		$scope.loadMoreEmployments = loadMoreEmployments;
		$scope.closeAlert = closeAlert;

		var currentEmployment = $scope.user.currentRole.employment;

		activate();

		function activate(){
			// load some org. stuff.
			topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment).then(function(orgUnits){
				$scope.orgUnits = orgUnits;
			});

			topicRouterApi.getEmployments(municipality.id, currentEmployment).then(function(employments){
				$scope.employments = employments;
			});
		}

		function ok(){
			if($scope.topic.org || $scope.selectedOrgUnit){ // validate org unit either present or selected.
				if($scope.selectedOrgUnit || $scope.selectedEmp){ // validate something has been selected.
					if($scope.selectedOrgUnit){
						$scope.topic.org = $scope.selectedOrgUnit;
					}
					if($scope.selectedEmp){
						$scope.topic.employee = $scope.selectedEmp;
					}
					topicRouterApi.updateDistributionRule($scope.topic);
					$modalInstance.close(/* pass some value? */);
				} else {
					addAlert({
						type: 'warning',
						msg: 'Du skal vælge organisatorisk enhed eller medarbejder.'
					});
				}
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal som minimum vælge en organisatorisk enhed.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function setSelectedOrgUnit(orgUnit){
			$scope.selectedOrgUnit = orgUnit;
		}

		function setSelectedEmp(emp){
			$scope.selectedEmp = emp;
		}

		function employmentSearch(){
			$log.warn("searching");
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

		function addAlert(alert) {
			$scope.ruleAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.ruleAlerts.splice(index, 1);
		}
	}
})();
