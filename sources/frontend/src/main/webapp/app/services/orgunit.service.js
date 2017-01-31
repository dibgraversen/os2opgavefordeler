(function () {
	'use strict';

	angular.module('topicRouter').factory('orgUnitService', orgUnitService);

	orgUnitService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	// NOTE consider implementing ngResource to manage RESTful resource endpoints.
	function orgUnitService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {		
		var service = {
			getIdentityProviders: getIdentityProviders,
			getOrgUnits: getOrgUnits
		};

		return service;

		function getOrgUnits() {
			 return $timeout(function() {
			 	 var ou1 = new OrgUnit('1','2','3','Aarhus Kommune', '2', 'aa@aarhus.dk',
						   			 	[
						   			 		new KLEExtended (1,'01.01.01','Bob KLE', 'Bla'),
						   			 		new KLEExtended (2,'01.01.02','Alice KLE', 'Bla'),
						   			 	]);
			 	 var ou2 = new OrgUnit('2','20','30','Aalborg Kommune', '3', 'alborg@aalborg.dk',
						   			 	[
						   			 		new KLEExtended (1,'01.01.01','Bob KLE', 'Bla'),
						   			 		new KLEExtended (2,'01.01.02','Alice KLE', 'Bla'),
						   			 	]);
       			 return[ou1,ou2];
       			}, 10);   		
		}

		function getIdentityProviders() {
			return httpGet("/auth/providers");
		}

		function OrgUnit(id, parentId, managerId, name, esdhId, email, phone, kles) {
			return {
				id: id,
				parentId: parentId,
				managerId: managerId,
				name: name,
				esdhId: esdhId,
				email: email,
				phone: phone,
				kles : kles
			};
		}

		function KLEExtended (kle, assignmentType){
			return {
				kle : kle,
				assignmentType : assignmentType
			};
		}

		function KLE(id, number, name, serviceText) {
			return {
				id: id,
				number: number,
				name: name,
				serviceText: serviceText
			};
		}

		function DistributionRule(id, parent, kle, org, employee, responsible) {
			return {
				id: id,
				parent: parent,
				kle: kle,
				org: org,
				employee: employee,
				responsible: responsible
			};
		}
	}
})();