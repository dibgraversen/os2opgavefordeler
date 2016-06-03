(function () {
	'use strict';
	angular.module('topicRouter').controller('SettingsCtrl', SettingsCtrl);

	SettingsCtrl.$inject = ['$scope', '$state', '$log', 'topicRouterApi', '$modal'];

	function SettingsCtrl($scope, $state, $log, topicRouterApi, $modal) {
		/* jshint validthis: true */
		var vm = this;
		$scope.municipalities = [];
		$scope.settingsMessages = [];

		$scope.openCreateMunicipality = openCreateMunicipality;
		$scope.openEditMunicipality = openEditMunicipality;
		$scope.openDeleteMunicipality = openDeleteMunicipality;
		$scope.toggleActive = toggleActive;
		$scope.buildRules = buildRules;
		$scope.closeAlert = closeAlert;

		activate();

		function activate() {
			$log.info("Settings::activate");
			if(!($scope.user.loggedIn && $scope.user.currentRole.admin)) {
				$log.info("not privileged, redirecting to home");
				$state.go("home");
			}
			topicRouterApi.getMunicipalities().then(function(municipalities){
				$scope.municipalities = municipalities;
			});
		}

		// API

		function openCreateMunicipality(){
			var modalInstance = $modal.open({
				templateUrl: 'app/settings/add-municipality-modal.html',
				controller: 'AddMunicipalityModalInstanceCtrl',
				size: 'md',
				resolve: {
					municipality: false
				}
			});

			modalInstance.result.then(function(municipality){
				$scope.municipalities.push(municipality);
			});
		}

		function openEditMunicipality(municipality){
			var modalInstance = $modal.open({
				templateUrl: 'app/settings/add-municipality-modal.html',
				controller: 'AddMunicipalityModalInstanceCtrl',
				size: 'md',
				resolve: {
					municipality: function(){
						return municipality;
					}
				}
			});

			modalInstance.result.then(function(updatedMunicipality){
				municipality = updatedMunicipality;
			});
		}
		
		function openDeleteMunicipality(municipality) {
			var modalInstance = $modal.open({
				templateUrl: 'app/settings/delete-municipality-modal.html',
				controller: 'DeleteMunicipalityModalInstanceCtrl',
				size: 'md',
				resolve: {
					municipality: function(){
						return municipality;
					}
				}
			});

			modalInstance.result.then(function(municipality) {
				// remove the municipality from the list
				$scope.municipalities = $scope.municipalities.filter(function (currMunicipality) {
					return currMunicipality.id !== municipality.id;
				});
			});
		}

		function toggleActive(municipality){
			municipality.active = !municipality.active;
			topicRouterApi.updateMunicipality(municipality)
					.then(function(response) {
						addMessage({
							type: 'success',
							msg: 'Kommunen blev opdateret.'
						});
					}, function(){
						municipality.active = !municipality.active;
						addMessage({
							type: "danger",
							msg: "opdatering af kommune fejlede, prøv igen senere."
						});
					});
		}

		function buildRules(municipality) {
			topicRouterApi.buildRules(municipality).then(function(response) {
				addMessage({
					type: 'success',
					msg: 'Regler er ved at blive oprettet. Dette kan tage et par minutter.'
				});
			}, function(){
				addMessage({
					type: "danger",
					msg: "Oprettelse af regler fejlede."
				});
			});
		}

		function addMessage(message){
			$scope.settingsMessages.push(message);
		}

		function closeAlert(index) {
			$scope.settingsMessages.splice(index, 1);
		}
	}
})();
