(function(){
	'use strict';
	angular.module('topicRouter').controller('KleServicetextModalInstanceCtrl', KleServicetextModalInstanceCtrl);

	KleServicetextModalInstanceCtrl.$inject = ['$log', '$scope', '$modalInstance', '$sce', '$sanitize', 'kle'];

	function KleServicetextModalInstanceCtrl($log, $scope, $modalInstance, $sce, $sanitize, kle){
		$scope.cancel = function(){
			$modalInstance.dismiss('cancel');
		};

		activate();

		function activate(){
			$scope.kle = kle;
			kle.serviceTextTrusted = $sce.trustAsHtml(kle.serviceText);
		}
	}
})();