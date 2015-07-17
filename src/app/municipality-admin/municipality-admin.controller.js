(function () {
	'use strict';
	angular.module('topicRouter').controller('MunicipalityAdminCtrl', MunicipalityAdminCtrl);

	angular.module('topicRouter').directive('fileModel', ['$parse', '$log', function ($parse, $log) {
		return {
			restrict: 'A',
			link: function(scope, element, attrs) {
				var model = $parse(attrs.fileModel);
				var modelSetter = model.assign;
				element.bind('change', function(){
					$log.info('changing file');
					scope.$apply(function(){
						modelSetter(scope, element[0].files[0]);
					});
				});
			}
		};
	}]);

	MunicipalityAdminCtrl.$inject = ['$scope', '$state', '$log', 'uiUploader', 'serverUrl'];

	function MunicipalityAdminCtrl($scope, $state, $log, uiUploader, serverUrl) {
		/* jshint validthis:true */
		var vm = this;
		$scope.uploadFile = null;
		$scope.mAdminAlerts = [];

		$scope.upload = upload;
		$scope.closeAlert = closeAlert;

		activate();

		function activate() {
			$log.info(serverUrl);
			$log.info('MunicipalityAdmin::activate');
			if(!($scope.user.loggedIn && $scope.user.currentRole.municipalityAdmin)) {
				$log.info("not privileged, redirecting to home");
				$state.go("home");
			}
		}

		function upload(){
			$log.info('uploading');
			$scope.mAdminAlerts = [];
			if($scope.uploadFile){
				uiUploader.addFiles([$scope.uploadFile]);
				uiUploader.startUpload({
					url: serverUrl+'/org-unit/fileImport',
					concurrency: 1,
					onProgress: function(file){
						$log.info(file.name + '=' + file.humanSize);
						$scope.$apply();
					},
					onCompleted: function(file, response, status) {
						if(status != 200){
							$log.info('request failed with: '+status);
							$scope.mAdminAlerts = [{
								type: 'danger',
								msg: 'Filen kunne ikke parses, verificér syntaks og prøv igen.'
							}];
						} else {
							$scope.mAdminAlerts = [{
								type: 'success',
								msg: 'Filen er uploadet.'
							}];
							$log.info(file + 'response' + response);
						}
						$scope.$apply();
						$scope.uploadFile = false;
						document.getElementById('orgFileInput').value = null;
					},
					onCompletedAll: function(){} // to avoid errors...
				});
			} else {
				$log.info('no file selected');
				$scope.mAdminAlerts = [{
					type: 'warning',
					msg: 'Du har ikke valgt en fil.'
				}];
			}
		}

		function closeAlert(index) {
			$scope.mAdminAlerts.splice(index, 1);
		}
	}
})();
