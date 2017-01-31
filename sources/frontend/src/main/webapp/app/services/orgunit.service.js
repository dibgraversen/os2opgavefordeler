(function () {
	'use strict';

	angular.module('topicRouter').factory('orgUnitService', orgUnitService);

	orgUnitService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	function orgUnitService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {		
		var service = {
			getIdentityProviders: getIdentityProviders,
			getOrgUnits: getOrgUnits,
			getKLEs: getKLEs,
			containsKle: containsKle
		};

		return service;

		function getOrgUnits() {
			 return $timeout(function() {
			 	 var ou1 = new OrgUnit('1','2','3','Aarhus Kommune', '2', 'aa@aarhus.dk', '1234565789',
						   			 	[
						   			 		new KLEExtended (new KLE(1,"01.01.01", "Kontante ydelses"), 'AssignmentType1'),
						   			 		new KLEExtended (new KLE(2,"01.02.02", "Sociale pensioner"), 'AssignmentType2')
						   			 	]);
			 	 var ou2 = new OrgUnit('2','20','30','Aalborg Kommune', '3', 'alborg@aalborg.dk','1234565789',
						   			 	[
						   			 		new KLEExtended (new KLE(2,"01.02.02", "Sociale pensioner"), 'AssignmentType1'),
						   			 	]);
       			 return[ou1,ou2];
       			}, 10);   		
		}

		function getKLEs(){
			return $timeout(function() {
					var kle1 = new KLE(1,"01.01.01", "Kontante ydelses");
					var kle2 = new KLE(2,"01.02.02", "Sociale pensioner");
	 					return[kle1,kle2];
					}, 10);
		}

		function containsKle(kle,orgUnit) {
			if (orgUnit == null){
				console.log("null orgUnit");
				return false;
			}

			if (orgUnit.kles == null){
				console.log("null kles");
				return false;
			}

			console.dir(orgUnit);

 			for(let aKle of orgUnit.kles){
				if( aKle.kle.number == kle.number){
					return true;
				}
			}
			return false;
		}

		function addKle(kle,orgUnit){	
			console.log("add kle " + kle);
		}

		function removeKle(kle,orgUnit){	
			console.log("remove kle " + kle);		
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