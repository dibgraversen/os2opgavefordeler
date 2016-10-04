(function () {
	'use strict';

	var app = angular.module('topicRouter');

	app.controller('EditResponsibilityModalInstanceCtrl', EditResponsibilityModalInstanceCtrl);

	EditResponsibilityModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'topicRouterApi', 'topic', 'municipality'];

	function EditResponsibilityModalInstanceCtrl($scope, $modalInstance, topicRouterApi, topic, municipality){
		$scope.topic = topic;
		$scope.orgUnitFilter = "";
		$scope.modalAlerts = [];
		$scope.loading = false;

		$scope.ok = ok;
		$scope.cancel = cancel;
		$scope.setCurrentOrgUnit = setCurrentOrgUnit;
		$scope.closeAlert = closeAlert;
		$scope.loadAll = loadAll;
		$scope.firstManagedParent = firstManagedParent;

		var currentEmployment = $scope.user.currentRole.employment;
		var allMissing = true;
		var orgUnits = {};

		activate();

		function activate(){
			topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment, true).then(function(orgUnits) {
				_.each(orgUnits, function(org){ loadParent(org); });
				$scope.orgUnits = orgUnits;
			});
		}

		function loadAll(){
			orgUnits = {};
			if(allMissing){
				$scope.loading = true;
				topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment, false).then(function (orgUnits) {
					_.each(orgUnits, function(org){ loadParent(org); });
					$scope.loading = false;
					$scope.orgUnits = orgUnits;
				});
				allMissing = false;
			}
		}

		function loadParent(org){
			if(orgUnits[org.id]){
				// make sure it's overwritten so we only use one instance of each org.
				org = orgUnits[org.id];
			} else {
				orgUnits[org.id] = org; // make sure we don't work on duplicates.
				if(org.parentId && org.parent === undefined) {
					if(orgUnits[org.parentId]){
						org.parent = orgUnits[org.parentId];
					} else {
						topicRouterApi.getOrgUnit(org.parentId).then(function (parent) {
							org.parent = parent;
							if (parent.parentId) {
								loadParent(parent);
							}
						});
					}
				}
			}
		}

		function firstManagedParent(org){
			if(org && typeof org.manager === 'object'){
				return org;
			} else if(org.parent){
				if(org.parent){
					return firstManagedParent(org.parent);
				}
			} else {
				return  { manager: {name: 'ingen leder fundet...' }};
			}
		}


		function ok(){
			if($scope.currentOrgUnit){
				$scope.topic.responsible = $scope.currentOrgUnit;
				topicRouterApi.updateDistributionRule(topic, 'responsibility');
				$modalInstance.close(/* pass some value? */);
			} else {
				addAlert({
					type: 'warning',
					msg: 'Du skal vælge en organisatorisk enhed.'
				});
			}
		}

		function cancel(){
			$modalInstance.dismiss('cancel');
		}

		function setCurrentOrgUnit(orgUnit){
			$scope.currentOrgUnit = orgUnit;
		}

		function addAlert(alert) {
			$scope.modalAlerts.push(alert);
		}

		function closeAlert(index) {
			$scope.modalAlerts.splice(index, 1);
		}
	}
})();
