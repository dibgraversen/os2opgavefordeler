(function () {
	'use strict';
	angular.module('topicRouter').controller('SettingsCtrl', SettingsCtrl);

	SettingsCtrl.$inject = ['$scope', '$state', '$log', 'topicRouterApi', '$modal'];

	function SettingsCtrl($scope, $state, $log, topicRouterApi, $modal) {
		/* jshint validthis: true */
		var vm = this;
		$scope.municipalities = [];
		$scope.users = [];
		$scope.settingsMessages = [];

		$scope.openCreateMunicipality = openCreateMunicipality;
		$scope.openEditMunicipality = openEditMunicipality;
		$scope.openDeleteMunicipality = openDeleteMunicipality;
		$scope.openEditUser = openEditUser;
		$scope.openDeleteUser = openDeleteUser;
		$scope.toggleActive = toggleActive;
		$scope.toggleMunicipalityAdmin = toggleMunicipalityAdmin;
		$scope.toggleKleAdmin = toggleKleAdmin;
		$scope.toggleAdmin = toggleAdmin;
		$scope.buildRules = buildRules;
		$scope.closeAlert = closeAlert;

		$scope.search = {};
		$scope.search.municipality = $scope.user.municipality;

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

			refreshUserList();
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

			modalInstance.result.then(function(municipality) {
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
			});
		}

		// User handling
		function openEditUser(user){
			var modalInstance = $modal.open({
				templateUrl: 'app/settings/edit-user-modal.html',
				controller: 'EditUserModalInstanceCtrl',
				size: 'md',
				resolve: {
					municipality: function(){
						return user.municipality;
					},
					user: function() {
						return user;
					}
				}
			});

			modalInstance.result.then(function(updatedUser){
				user.name = updatedUser.name;
			});
		}

		function openDeleteUser(user) {
			var modalInstance = $modal.open({
				templateUrl: 'app/settings/delete-user-modal.html',
				controller: 'DeleteUserModalInstanceCtrl',
				size: 'md',
				resolve: {
					user: function() {
						return user;
					}
				}
			});

			modalInstance.result.then(function(deletedUser){
				// reload user list
				topicRouterApi.getAllUsers().then(function(users) {
					$scope.users = users;
				});
			});
		}

		function toggleMunicipalityAdmin(user) {
			user.municipalityAdmin = !user.municipalityAdmin;

			topicRouterApi.setMunicipalityAdmin(user.roleId, user.municipalityAdmin)
					.then(function(response) {
						addMessage({
							type: 'success',
							msg: 'Brugeren blev opdateret.'
						});
					}, function(){
						user.municipalityAdmin = !user.municipalityAdmin;

						addMessage({
							type: "danger",
							msg: "Opdatering af bruger fejlede, prøv igen senere."
						});
					});
		}

		function toggleAdmin(user) {
			user.admin = !user.admin;

			topicRouterApi.setAdmin(user.roleId, user.admin)
					.then(function(response) {
						addMessage({
							type: 'success',
							msg: 'Brugeren blev opdateret.'
						});
					}, function(){
						user.admin = !user.admin;

						addMessage({
							type: "danger",
							msg: "Opdatering af bruger fejlede, prøv igen senere."
						});
					});
		}

		function toggleKleAdmin(user) {
			user.kleAssigner = !user.kleAssigner;

			topicRouterApi.setKleAdmin(user.roleId, user.kleAssigner)
					.then(function(response) {

						addMessage({
							type: 'success',
							msg: 'Brugeren blev opdateret.'
						});
					}, function(){
						user.kleAssigner = !user.kleAssigner;

						addMessage({
							type: "danger",
							msg: "Opdatering af bruger fejlede, prøv igen senere."
						});
					});
		}

		function refreshUserList() {
			topicRouterApi.getAllUsers().then(function(users) {
				$scope.users = users;
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
