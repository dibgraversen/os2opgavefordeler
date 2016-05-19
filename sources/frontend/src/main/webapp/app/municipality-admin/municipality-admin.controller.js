(function () {
	'use strict';
	angular.module('topicRouter').controller('MunicipalityAdminCtrl', MunicipalityAdminCtrl);

	angular.module('topicRouter').directive('fileModel', ['$parse', '$log', function ($parse, $log) {
		return {
			restrict: 'A',
			scope: {
				uploadFile: '='
			},
			link: function(scope, element, attrs) {
				var model = $parse(attrs.fileModel);
				var modelSetter = model.assign;
				element.bind('change', function(){
					$log.info('changing file');
					var parent = scope.$parent.$parent.$parent; // TODO >.< had to go 3 levels up because of tabs...
					parent.$apply(function(){
						$log.warn(parent);
						modelSetter(parent, element[0].files[0]);
					});
				});
			}
		};
	}]);

	MunicipalityAdminCtrl.$inject = ['$scope', '$state', '$log', '$modal', 'uiUploader', 'serverUrl',
		'appSpinner', 'topicRouterApi'];

	function MunicipalityAdminCtrl($scope, $state, $log, $modal, uiUploader, serverUrl,
																 appSpinner, topicRouterApi) {
		/* jshint validthis:true */
		var vm = this;
		$scope.uploadFile = null;
		$scope.mAdminAlerts = [];
		$scope.kles = [];
		$scope.apiKey = {};

		$scope.upload = upload;
		$scope.closeAlert = closeAlert;
		$scope.addKle = addKle;
		$scope.editKle = editKle;
		$scope.deleteKle = deleteKle;
		$scope.editApiKey = editApiKey;

		activate();

		function activate() {
			$log.info(serverUrl);
			$log.info('MunicipalityAdmin::activate');
			if(!($scope.user.loggedIn && $scope.user.currentRole.municipalityAdmin)) {
				$log.info("not privileged, redirecting to home");
				$state.go("home");
			}
			topicRouterApi.getKlesForMunicipality($scope.user.municipality).then(function(kles){
				$scope.kles = kles;
			});

			topicRouterApi.getApiKey($scope.user.municipality).then(function(apiKey){
				$scope.apiKey = apiKey;
			});
		}

		function upload(){
			clearMessages();
			if($scope.uploadFile){
				appSpinner.showSpinner();
				uiUploader.addFiles([$scope.uploadFile]);
				uiUploader.startUpload({
					url: serverUrl+'/org-units/fileImport',
					concurrency: 1,
					data: {
						withCredentials: true
					},
					onProgress: function(file){
						$scope.$apply();
					},
					onCompleted: function(file, response, status) {
						appSpinner.hideSpinner();
						if(status != 200){
							$scope.mAdminAlerts = [{
								type: 'danger',
								msg: 'Filen kunne ikke parses, verificér syntaks og prøv igen.'
							}];
						} else {
							$scope.mAdminAlerts = [{
								type: 'success',
								msg: 'Filen er uploadet.'
							}];
						}
						$scope.$apply();
						$scope.uploadFile = false;
						document.getElementById('orgFileInput').value = null;
					},
					onCompletedAll: function(){} // to avoid errors...
				});
			} else {
				$scope.mAdminAlerts = [{
					type: 'warning',
					msg: 'Du har ikke valgt en fil.'
				}];
			}
		}

		function closeAlert(index) {
			$scope.mAdminAlerts.splice(index, 1);
		}

		function addAlert(alert){
			$scope.mAdminAlerts.push(alert);
		}

		function editKle(kle){
			clearMessages();
			$modal.open({
				resolve: {
					municipality: function(){
						return $scope.user.municipality;
					},
					kle: function(){
						return kle;
					}

				},
				templateUrl: 'app/municipality-admin/add-kle-modal.html',
				controller: 'AddKleModalInstanceCtrl'
			}).result.then(
					function(updatedKle){
						if(kle.number !== updatedKle.number){
							var oldSplitted = kle.number.split('.');
							var newSplitted = updatedKle.number.split('.');
							if(kle.type == 'GROUP'){
								if(oldSplitted[0] !== newSplitted[0]){ // change 'main'
									_.each($scope.kles, function(item){
										var split = item.number.split('.');
										if(split[0] == oldSplitted[0] && split[1] == oldSplitted[1]){
											item.number = newSplitted[0] + '.' + split[1] + '.' + split[2];
										}
									});
								}
								if(oldSplitted[1] !== newSplitted[1]){ // change 'topic'
								// if group number changed, check for topics to update.
									_.each($scope.kles, function(item){
										var split = item.number.split('.');
										if(split[0] == newSplitted[0] && split[1] == oldSplitted[1]){
											item.number = split[0] + '.' + newSplitted[1] + '.' + split[2];
										}
									});
								}
							}
						}
						kle.name = updatedKle.name;
						kle.number = updatedKle.number;
						kle.serviceText = updatedKle.serviceText;
					});
		}

		function editApiKey() {
			clearMessages();
			$modal.open({
				resolve: {
					municipality: function(){
						return $scope.user.municipality;
					},
					apiKey: function(){
						return $scope.apiKey;
					}
				},
				templateUrl: 'app/municipality-admin/edit-api-key-modal.html',
				controller: 'EditApiKeyModalInstanceCtrl'
			}).result.then(
					function(apiKey){
						$scope.apiKey = apiKey;
					});
		}

		function addKle(){
			clearMessages();
			$modal.open({
				resolve: {
					municipality: function(){
						return $scope.user.municipality;
					},
					kle: false
				},
				templateUrl: 'app/municipality-admin/add-kle-modal.html',
				controller: 'AddKleModalInstanceCtrl'
			}).result.then(
					function(kle){
						 $scope.kles.push(kle);
					});
		}

		function clearMessages(){
			$scope.mAdminAlerts = [];
		}

		function deleteKle(kle){
			clearMessages();
			$modal.open({
				templateUrl: 'app/common/confirmation-modal.html',
				controller: 'ConfirmationModalInstanceCtrl',
				resolve: {
					message: function(){
						return 'Vil du slette kle?';
					}
				}
			}).result.then(
					function(reply) {
						if (reply) {
							topicRouterApi.deleteMunicipalityKle(kle).then(
									function (kleId) {
										_.remove($scope.kles, function (current) {
											if (kleId === current.id) {
												$log.warn('deleting kle');
												return true;
											}
											else {
												return false;
											}
										});
									}, function (error) {
										addAlert({type: 'warning', msg: error.data});
									});
						}
					});
		}
	}
})();
