(function () {
	'use strict';


	angular.module('topicRouter').factory('orgUnitService', orgUnitService);

	orgUnitService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	function orgUnitService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {		
		var service = {
			getOrgUnits: getOrgUnits,
			getKLEs: getKLEs,
			containsKle: containsKle,
			addKle : addKle,
			removeKle : removeKle,
			getRestKles : getRestKles
		};

		return service;

		function getOrgUnits() {
			var getOrgUnits = function() {
			 	 var ou1 = new OrgUnit('1','2','3','Aarhus Kommune', '2', 'aa@aarhus.dk', '1234565789',
						   			 	[
						   			 		new KLEExtended (new KLE(1,"01.01.01", "Kontante ydelses"), 'AssignmentType1'),
						   			 		new KLEExtended (new KLE(2,"01.02.02", "Sociale pensioner"), 'AssignmentType2')
						   			 	]);
			 	 var ou2 = new OrgUnit('2','20','30','Aalborg Kommune', '3', 'alborg@aalborg.dk','1234565789',
						   			 	[
						   			 		new KLEExtended (new KLE(3,"01.02.02", "Sociale pensioner"), 'AssignmentType1'),
						   			 	]);
       			 return[ou1,ou2];
       			};   		
			return simulateRestCall(getOrgUnits);
		}

		function getKLEs(){
			var getKles = function() {
				var kle1 = new KLEExtended (new KLE(1,"01.01.01", "Kontante ydelses"), 'AssignmentType1');
				var kle2 = new KLEExtended (new KLE(2,"01.02.02", "Sociale pensioner"), 'AssignmentType2');
				var kle3 = new KLEExtended (new KLE(2,"01.02.02", "Sociale pensioner"), 'AssignmentType1');
	 				return[kle1,kle2,kle3];
				};
			return simulateRestCall(getKles)
		}

		function addKle(kle,orgunit){
			var func = function(orgunit,kle) {	
				//console.log(klesFromService);
				};
			return simulateRestCall(func);
		}

		function removeKle(kle,orgunit){
			var func = function(orgunit,kle) {		};
			return simulateRestCall(func);
		}

		function simulateRestCall(functionToSimulate){ 
			return $timeout(functionToSimulate,10);
		}

		function containsKle(kle,orgUnit,assignmentType) {
			if (orgUnit == null){
				return false;
			}

			if (orgUnit.kles == null){
				return false;
			}

 		   for(var i = 0; i < orgUnit.kles.length; i++){
				if( orgUnit.kles[i].kle.id == kle.id && assignmentType == orgUnit.kles[i].assignmentType){
					return true;
				}
			}


			return false;
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


	function getRestKles(){
		var simulateCallToGetKles = function (){
				return { "KLE": [
				  {
				    "number": "00",
				    "serviceText": "Kommunens styrelse",
				    "children": [
				      {
				        "number": "00.05",
				        "serviceText": "Besøg, repræsentation mv.",
				        "children": [
				          {
				            "number": "00.05.01",
				            "serviceText": "Venskabsbysamarbejde",
				            "children": []
				          },
				          {
				            "number": "00.05.10",
				            "serviceText": "Gaver til og fra kommunen/institutionen",
				            "children": []
				          }
				        ]
				      },
				      {
				        "number": "00.17",
				        "serviceText": "Kommunalt/tværsektorielt samarbejde  Servicetekst  Stikord",
				        "children": [
				          {
				            "number": "00.17.15",
				            "serviceText": "Kommunale samarbejder",
				            "children": []
				          },
				          {
				            "number": "00.17.20",
				            "serviceText": "Aftaler om samarbejde mellem kommuner (kommunale fællesskaber)",
				            "children": []
				          }
				        ]
				      }
				    ]
				  },
				  {
				    "number": "03",
				    "serviceText": "Boliger",
				    "children": [
				      {
				        "number": "03.01",
				        "serviceText": "Benyttelse af boliger",
				        "children": [
				          {
				            "number": "03.01.00",
				            "serviceText": "Benyttelse af boliger i almindelighed",
				            "children": []
				          },
				          {
				            "number": "03.01.03",
				            "serviceText": "Nedlæggelse af boliger",
				            "children": []
				          }
				        ]
				      },
				      {
				        "number": "03.22",
				        "serviceText": "Friplejeboliger",
				        "children": [
				          {
				            "number": "03.22.00",
				            "serviceText": "Friplejeboliger i almindelighed",
				            "children": []
				          },
				          {
				            "number": "03.22.05",
				            "serviceText": "Etablering af friplejeboliger",
				            "children": []
				          },
				          {
				            "number": "03.22.10",
				            "serviceText": "Drift og afhændelse af friplejeboliger",
				            "children": []
				          }
				        ]
				      }
				    ]
				  },
				  {
				    "number": "13",
				    "serviceText": "Forsyning",
				    "children": [
				      {
				        "number": "13.02",
				        "serviceText": "Vandforsyning",
				        "children": [
				          {
				            "number": "13.02.00",
				            "serviceText": "Vandforsyning i almindelighed",
				            "children": []
				          },
				          {
				            "number": "13.02.02",
				            "serviceText": "Vandkvalitet",
				            "children": []
				          }
				        ]
				      },
				      {
				        "number": "13.03",
				        "serviceText": "Varmeforsyning",
				        "children": [
				          {
				            "number": "13.03.00",
				            "serviceText": "Varmeforsyning i almindelighed",
				            "children": []
				          }
				        ]
				      }
				    ]
				  }
				]}};
			return simulateRestCall(simulateCallToGetKles);
		} // end of getKleRestService
	}
})();