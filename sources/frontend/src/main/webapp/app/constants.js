(function(){
	'use strict';
	angular.module("app.config", [])

	.constant("serverUrl", "${application.backendUrl}")
	.constant("version", "${application.version}")
	;
})();
