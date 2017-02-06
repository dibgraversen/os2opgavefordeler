(function () {
	'use strict';

	angular.module('topicRouter').controller('KleAdminCtrl', KleAdminCtrl);

	KleAdminCtrl.$inject = ['$scope', '$state', '$log', '$interval' , 'topicRouterApi', 'orgUnitService', '$modal'];

	function KleAdminCtrl($scope, $state, $log, $interval, topicRouterApi, orgUnitService, $modal) {
		/* jshint validthis: true */
		var vm = this;
		$scope.$log=$log;
		$scope.currentOrgUnit = null;
		$scope.filterStr = "";

		$scope.ous = [];
		$scope.kles = [];

		$scope.setCurrentOrgUnit = setCurrentOrgUnit;
		$scope.modifyKle = modifyKle;

		activate();

		function activate() {
			orgUnitService.getKles().then(function(kles){
				$scope.kles = kles; 
			});

			orgUnitService.getOrgUnits().then(function(ous){
				$scope.ous = JSON.parse(JSON.stringify(ous));
				setCurrentOrgUnit($scope.ous[0].id);				
			});		
		}

		function setCurrentOrgUnit(ouId){
			orgUnitService.getOrgUnit(ouId).then(function(ou){
				$scope.currentOrgUnit=ou;
				$scope.currentOrgUnit.displayKles = JSON.parse(JSON.stringify($scope.kles));			
				refreshTree($scope.currentOrgUnit.displayKles);
			});	
		}

		function refreshTree(kles){
			if(kles === null){
				return ;
			}
			for(var i = 0; i < kles.length; i++){
				kles[i].assigned = isKleAssigned($scope.currentOrgUnit,kles[i].number);
				refreshTree(kles[i].children);
			}
		}

		function isKleAssigned(ou, kleNumber){
			for(var i = 0; i < ou.kles.length; i++){
				if(ou.kles[i].kleNumber==kleNumber){
					return true;
				}
			}
			return false;
		}

		function expand(kle){
			kle.expanded = true;
		}

		function modifyKle(checked,kle,ou){
			if(checked){
					orgUnitService.addKle(kle,ou);
			}
			else {
				orgUnitService.removeKle(kle,ou);
			}			
		}
	}
})();
