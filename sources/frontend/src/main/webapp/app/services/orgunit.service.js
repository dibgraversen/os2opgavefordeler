(function () {
	'use strict';

	angular.module('topicRouter').factory('orgUnitService', orgUnitService);

	orgUnitService.$inject = ['$http', '$q', '$timeout', '$cacheFactory', 'serverUrl', 'appSpinner', '$log'];

	function orgUnitService($http, $q, $timeout, $cacheFactory, serverUrl, appSpinner, $log) {		
		var service = {
			getOrgUnits: getOrgUnits,
			getOrgUnitsAsTree : getOrgUnitsAsTree,
			getOrgUnit: getOrgUnit,
			addKle : addKle,
			removeKle : removeKle,
			getKles : getKles
		};

		var baseUrl = serverUrl;

		var requestConfig = {
			headers: {
				'Content-Type': 'application/json',
				'Cache-Control': 'no-cache',
				'Pragma': 'no-cache',
				'Expires': '-1'
			}
		};
		return service;

		function getOrgUnits() {
			return httpGet('/ou/list');
		}

		function getOrgUnitsAsTree() {
			return httpGet('/ou');
		}

		function getOrgUnit(id) {
			return httpGet('/ou/' + id);
		}

		function getKles(){		
		/*return $timeout( function(){
				var result = JSON.parse('[{"number":"04","serviceText":"Parker, fritids-/idrætsanlæg og landskabspleje mv.","children":[{"number":"04.00","serviceText":"Parker, fritids-/idrætsanlæg og landskabspleje","children":[{"number":"04.00.00","serviceText":"Parker, fritids-/idrætsanlæg og landskabspleje i almindelighed","children":[]}]},{"number":"04.01","serviceText":"Parker, anlæg og grønne områder","children":[{"number":"04.01.00","serviceText":"Parker, anlæg og grønne områder i almindelighed","children":[]},{"number":"04.01.09","serviceText":"Renovering af parker og anlæg","children":[]},{"number":"04.01.10","serviceText":"Nyanlæg af parker og anlæg","children":[]},{"number":"04.01.11","serviceText":"Pleje af parker og anlæg","children":[]},{"number":"04.01.14","serviceText":"Udlån/benyttelse af parker og anlæg","children":[]},{"number":"04.01.15","serviceText":"Uretmæssig benyttelse af parker og anlæg","children":[]},{"number":"04.01.16","serviceText":"Vildtpleje","children":[]},{"number":"04.01.20","serviceText":"Hærværk i parker og anlæg","children":[]}]},{"number":"04.02","serviceText":"Botaniske haver [udgået]","children":[{"number":"04.02.00","serviceText":"Botaniske haver i almindelighed [udgået]","children":[]}]},{"number":"04.03","serviceText":"Offentlige legepladser","children":[{"number":"04.03.00","serviceText":"Offentlige legepladser i almindelighed","children":[]}]},{"number":"04.04","serviceText":"Haller/indendørsanlæg","children":[{"number":"04.04.00","serviceText":"Haller/indendørsanlæg i almindelighed","children":[]}]},{"number":"04.05","serviceText":"Hundetoiletter","children":[{"number":"04.05.00","serviceText":"Hundetoiletter i almindelighed","children":[]}]},{"number":"04.06","serviceText":"Skøjtebaner [udgået]","children":[{"number":"04.06.00","serviceText":"Skøjtebaner i almindelighed [udgået]","children":[]}]},{"number":"04.07","serviceText":"Svømmeanlæg [udgået]","children":[{"number":"04.07.00","serviceText":"Svømmeanlæg i almindelighed [udgået]","children":[]}]},{"number":"04.08","serviceText":"Udendørsanlæg","children":[{"number":"04.08.00","serviceText":"Udendørsanlæg i almindelighed","children":[]},{"number":"04.08.09","serviceText":"Renovering af udendørsanlæg","children":[]},{"number":"04.08.10","serviceText":"Nyanlæg af udendørsanlæg","children":[]},{"number":"04.08.11","serviceText":"Pleje af udendørsanlæg","children":[]},{"number":"04.08.12","serviceText":"Tekniske installationer i udendørsanlæg","children":[]},{"number":"04.08.14","serviceText":"Udlån/benyttelse af udendørsanlæg","children":[]},{"number":"04.08.20","serviceText":"Hærværk på udendørsanlæg","children":[]}]},{"number":"04.09","serviceText":"Private skove [udgået]","children":[{"number":"04.09.00","serviceText":"Private skove [udgået]","children":[]}]},{"number":"04.10","serviceText":"Kommunale skove","children":[{"number":"04.10.00","serviceText":"Kommunale skove i almindelighed","children":[]},{"number":"04.10.01","serviceText":"Udlån/benyttelse af skove","children":[]},{"number":"04.10.02","serviceText":"Fiskeri, jagt og vildt i skove","children":[]},{"number":"04.10.03","serviceText":"Pleje/hugst af skove","children":[]},{"number":"04.10.10","serviceText":"Nyanlæg og tilplantninger i skove","children":[]},{"number":"04.10.20","serviceText":"Hærværk i skove","children":[]}]},{"number":"04.11","serviceText":"Kommunale strande","children":[{"number":"04.11.00","serviceText":"Kommunale strande i almindelighed","children":[]},{"number":"04.11.01","serviceText":"Udlån/benyttelse af strande","children":[]},{"number":"04.11.02","serviceText":"Jagt og vildtpleje","children":[]},{"number":"04.11.03","serviceText":"Nyanlæg af strande","children":[]},{"number":"04.11.04","serviceText":"Renholdelse/strandrensning/vedligeholdelse af strande","children":[]},{"number":"04.11.09","serviceText":"Renovering af strande","children":[]},{"number":"04.11.10","serviceText":"Livredning og redningsposter på strande","children":[]},{"number":"04.11.16","serviceText":"Beplantning af strande","children":[]},{"number":"04.11.20","serviceText":"Hærværk på strande","children":[]},{"number":"04.11.25","serviceText":"Stormflod","children":[]}]},{"number":"04.12","serviceText":"Vandrerhjem","children":[{"number":"04.12.00","serviceText":"Vandrerhjem i almindelighed","children":[]}]},{"number":"04.13","serviceText":"Det åbne land [udgået]","children":[{"number":"04.13.00","serviceText":"Det åbne land i almindelighed [udgået]","children":[]}]},{"number":"04.14","serviceText":"Campingpladser og lejrpladser","children":[{"number":"04.14.00","serviceText":"Campingpladser og lejrpladser i almindelighed","children":[]},{"number":"04.14.09","serviceText":"Renovering af camping- og lejrpladser","children":[]},{"number":"04.14.10","serviceText":"Nyanlæg af camping- og lejrpladser","children":[]},{"number":"04.14.11","serviceText":"Pleje og vedligeholdelse af camping- og lejrpladser","children":[]},{"number":"04.14.15","serviceText":"Udlejningstilladelse efter campingreglementet","children":[]},{"number":"04.14.18","serviceText":"Benyttelse af campinghytter","children":[]},{"number":"04.14.20","serviceText":"Brandværnsforanstaltninger på campingpladser","children":[]},{"number":"04.14.22","serviceText":"Hygiejniske forhold mv. på campingpladser","children":[]}]},{"number":"04.15","serviceText":"Planteskole/gartneri [udgået]","children":[{"number":"04.15.00","serviceText":"Planteskole/gartneri i almindelighed [udgået]","children":[]}]},{"number":"04.16","serviceText":"Plantesygdomme","children":[{"number":"04.16.00","serviceText":"Plantesygdomme i almindelighed","children":[]}]},{"number":"04.18","serviceText":"Kystbeskyttelse","children":[{"number":"04.18.00","serviceText":"Kystbeskyttelse i almindelighed","children":[]},{"number":"04.18.06","serviceText":"Risikostyringsplanlægning, oversvømmelser","children":[]},{"number":"04.18.10","serviceText":"Badebroer og bådebroer","children":[]}]},{"number":"04.20","serviceText":"Læplantninger","children":[{"number":"04.20.00","serviceText":"Læplantninger i almindelighed","children":[]}]},{"number":"04.21","serviceText":"Udvikling af landdistrikterne","children":[{"number":"04.21.00","serviceText":"Udvikling af landdistrikterne i almindelighed","children":[]},{"number":"04.21.01","serviceText":"Særligt følsomme landbrugsområder (SFL-områder)","children":[]},{"number":"04.21.05","serviceText":"Fremme af udviklingen i særlige landdistrikter","children":[]},{"number":"04.21.10","serviceText":"Miljøvenlige jordbrugsforanstaltninger","children":[]}]},{"number":"04.30","serviceText":"Kommunale begravelser","children":[{"number":"04.30.00","serviceText":"Kommunale begravelser i almindelighed","children":[]},{"number":"04.30.01","serviceText":"Begravelser og urnenedsættelser","children":[]},{"number":"04.30.02","serviceText":"Flytning af urner","children":[]},{"number":"04.30.05","serviceText":"Kremering","children":[]},{"number":"04.30.10","serviceText":"Kommunale begravelsespladser","children":[]}]},{"number":"04.31","serviceText":"Kommunale kirkegårde","children":[{"number":"04.31.00","serviceText":"Kommunale kirkegårde i almindelighed","children":[]},{"number":"04.31.01","serviceText":"Gravminder","children":[]},{"number":"04.31.09","serviceText":"Renovering af kirkegårde","children":[]},{"number":"04.31.10","serviceText":"Nyanlæg af kirkegårde","children":[]},{"number":"04.31.11","serviceText":"Vedligeholdelse og pleje af kommunale kirkegårde","children":[]},{"number":"04.31.15","serviceText":"Gravsteder","children":[]},{"number":"04.31.20","serviceText":"Hærværk på kirkegårde","children":[]}]}]},{"number":"05","serviceText":"Veje og trafik","children":[{"number":"05.00","serviceText":"Veje og trafik","children":[{"number":"05.00.00","serviceText":"Veje og trafik i almindelighed","children":[]},{"number":"05.00.05","serviceText":"Tilgængelighed, veje og trafik","children":[]}]},{"number":"05.01","serviceText":"Offentlige veje","children":[{"number":"05.01.00","serviceText":"Offentlige veje i almindelighed","children":[]},{"number":"05.01.01","serviceText":"Vejnavne/husnumre","children":[]},{"number":"05.01.02","serviceText":"Anlæg af nye kommuneveje","children":[]},{"number":"05.01.07","serviceText":"Hovedreparationer [udgået]","children":[]},{"number":"05.01.08","serviceText":"Vejvedligeholdelse og vejrenovering","children":[]},{"number":"05.01.09","serviceText":"Kørebanebelægninger, materialer [udgået]","children":[]},{"number":"05.01.10","serviceText":"Adgangsforhold til offentlige veje","children":[]},{"number":"05.01.11","serviceText":"Sikring af vejanlæg","children":[]},{"number":"05.01.12","serviceText":"Belysning, veje","children":[]},{"number":"05.01.13","serviceText":"Afvanding og dræning af veje","children":[]},{"number":"05.01.14","serviceText":"Vejvanding [udgået]","children":[]},{"number":"05.01.15","serviceText":"Vejrabatter og beplantning","children":[]},{"number":"05.01.16","serviceText":"Hegn mod vej","children":[]},{"number":"05.01.17","serviceText":"Færdselsret på private veje [udgået]","children":[]},{"number":"05.01.18","serviceText":"Arbejder udført for private veje m.v. [udgået]","children":[]},{"number":"05.01.19","serviceText":"Overtagelse af private veje [udgået]","children":[]},{"number":"05.01.20","serviceText":"Statslige veje [udgået]","children":[]},{"number":"05.01.21","serviceText":"Amtskommunale veje [udgået]","children":[]},{"number":"05.01.22","serviceText":"Nedlæggelse af kommuneveje","children":[]},{"number":"05.01.25","serviceText":"Jernbaneinfrastruktur","children":[]},{"number":"05.01.30","serviceText":"Almindelige bestemmelser om vejenes administration","children":[]},{"number":"05.01.35","serviceText":"Vejplanlægning","children":[]},{"number":"05.01.40","serviceText":"Trafikstyring","children":[]},{"number":"05.01.45","serviceText":"Statsveje","children":[]}]},{"number":"05.02","serviceText":"Private fællesveje/stier","children":[{"number":"05.02.00","serviceText":"Private fællesveje/stier i almindelighed","children":[]},{"number":"05.02.01","serviceText":"Vejudlæg/anlæg","children":[]},{"number":"05.02.02","serviceText":"Istandsættelse og vedligeholdelse","children":[]},{"number":"05.02.03","serviceText":"Forandringer, færdselsregulering mv.","children":[]},{"number":"05.02.04","serviceText":"Nedlæggelse af private fællesveje/stier","children":[]},{"number":"05.02.05","serviceText":"Vejnavn/husnumre på private fællesveje","children":[]},{"number":"05.02.10","serviceText":"Overkørsler og overgange","children":[]},{"number":"05.02.12","serviceText":"Belysning, private fællesveje/stier","children":[]},{"number":"05.02.13","serviceText":"Bygningsfremspring, vejskilte, hegn mod vej mv.","children":[]},{"number":"05.02.15","serviceText":"Særlig brug af vejareal","children":[]},{"number":"05.02.16","serviceText":"Beplantning, private fællesveje/stier","children":[]},{"number":"05.02.20","serviceText":"Overtagelse af private veje","children":[]}]},{"number":"05.03","serviceText":"Broer og tunneler mv.","children":[{"number":"05.03.00","serviceText":"Broer og tunneler mv. i almindelighed","children":[]},{"number":"05.03.05","serviceText":"Nyanlæg, broer og tunneler","children":[]},{"number":"05.03.06","serviceText":"Vedligeholdelse/renovering","children":[]}]},{"number":"05.04","serviceText":"Stier","children":[{"number":"05.04.00","serviceText":"Stier i almindelighed","children":[]},{"number":"05.04.04","serviceText":"Fortove [udgået]","children":[]},{"number":"05.04.05","serviceText":"Stinumre/stifortegnelse","children":[]},{"number":"05.04.06","serviceText":"Nyanlæg, stier","children":[]},{"number":"05.04.08","serviceText":"Vedligeholdelse af stier","children":[]},{"number":"05.04.09","serviceText":"Belægning, stier","children":[]},{"number":"05.04.11","serviceText":"Vejvisning på cykel-, ride- og vandreruter","children":[]},{"number":"05.04.12","serviceText":"Belysning, stier","children":[]},{"number":"05.04.16","serviceText":"Beplantning, stier","children":[]},{"number":"05.04.20","serviceText":"Overtagelse af almene stier og private fællesstier","children":[]},{"number":"05.04.21","serviceText":"Afgivelse af kommunal sti til privat [udgået]","children":[]},{"number":"05.04.22","serviceText":"Nedlæggelse af offentlige/almene stier","children":[]},{"number":"05.04.23","serviceText":"Klassificering af stier","children":[]}]},{"number":"05.05","serviceText":"Pladser, torve, holdepladser mv.","children":[{"number":"05.05.00","serviceText":"Pladser, torve, holdepladser mv. i almindelighed","children":[]},{"number":"05.05.01","serviceText":"Parkeringspladser [udgået]","children":[]},{"number":"05.05.02","serviceText":"Parkeringsrestriktioner [udgået]","children":[]},{"number":"05.05.04","serviceText":"holdepladser (taxi, busser) [udgået]","children":[]},{"number":"05.05.05","serviceText":"Nyanlæg, pladser, torve og holdepladser","children":[]},{"number":"05.05.06","serviceText":"Vedligeholdelse/renovering, pladser, torve og holdepladser","children":[]},{"number":"05.05.12","serviceText":"Belysning, pladser, torve og holdepladser","children":[]},{"number":"05.05.15","serviceText":"Parkeringskontrol [udgået]","children":[]}]},{"number":"05.06","serviceText":"Pladser og anlæg [udgået]","children":[{"number":"05.06.00","serviceText":"I almindelighed [udgået]","children":[]},{"number":"05.06.01","serviceText":"Torve [udgået]","children":[]},{"number":"05.06.02","serviceText":"Rastepladser [udgået]","children":[]}]},{"number":"05.07","serviceText":"Renholdelse og vintertjeneste","children":[{"number":"05.07.00","serviceText":"Renholdelse og vintertjeneste i almindelighed","children":[]},{"number":"05.07.01","serviceText":"Renholdelse","children":[]},{"number":"05.07.02","serviceText":"Snerydning og glatførebekæmpelse","children":[]},{"number":"05.07.04","serviceText":"Ukrudtsbekæmpelse og renholdelse (klipning) af rabatter og fortorve [udgået]","children":[]},{"number":"05.07.10","serviceText":"Grundejeres forpligtelser, private fællesveje og fællesstier","children":[]}]},{"number":"05.08","serviceText":"Oplagspladser m.v. [udgået]","children":[{"number":"05.08.00","serviceText":"I almindelighed [udgået]","children":[]},{"number":"05.08.01","serviceText":"Materielplads [udgået]","children":[]},{"number":"05.08.02","serviceText":"Arbejdsskure og arbejdsvogne [udgået]","children":[]},{"number":"05.08.03","serviceText":"Stenpladser [udgået]","children":[]},{"number":"05.08.04","serviceText":"Gruspladser og grusgrave [udgået]","children":[]},{"number":"05.08.05","serviceText":"Saltoplag [udgået]","children":[]}]},{"number":"05.09","serviceText":"Parkering","children":[{"number":"05.09.00","serviceText":"Parkering i almindelighed","children":[]},{"number":"05.09.02","serviceText":"Betalingsparkering: Parkeringslicenser og betalingsafgifter","children":[]},{"number":"05.09.04","serviceText":"Parkeringskontrol","children":[]},{"number":"05.09.06","serviceText":"Parkeringsanlæg","children":[]},{"number":"05.09.08","serviceText":"Parkeringsafmærkning","children":[]},{"number":"05.09.10","serviceText":"Parkeringsautomater","children":[]},{"number":"05.09.12","serviceText":"Parkeringsdispensationer","children":[]}]},{"number":"05.10","serviceText":"Trafikforhold [udgået]","children":[{"number":"05.10.00","serviceText":"I almindelighed [udgået]","children":[]},{"number":"05.10.01","serviceText":"Gadegennembrud [udgået]","children":[]},{"number":"05.10.02","serviceText":"Gågader [udgået]","children":[]},{"number":"05.10.03","serviceText":"Benzinstationers placering [udgået]","children":[]},{"number":"05.10.04","serviceText":"Jernbaners og stationers indretning og udnyttelse [udgået]","children":[]},{"number":"05.10.07","serviceText":"Rækværkers placering [udgået]","children":[]}]},{"number":"05.11","serviceText":"Trafikrestriktioner [udgået]","children":[{"number":"05.11.00","serviceText":"I almindelighed [udgået]","children":[]},{"number":"05.11.01","serviceText":"Tung trafik [udgået]","children":[]},{"number":"05.11.02","serviceText":"Tvangsruter, ikke-brandmæssige [udgået]","children":[]},{"number":"05.11.03","serviceText":"Foranstaltninger til begrænsning af køretøjers brændstofforbrug [udgået]","children":[]}]},{"number":"05.12","serviceText":"Opgravning, ledningsarbejder mv.","children":[{"number":"05.12.00","serviceText":"Opgravning, ledningsarbejder mv. i almindelighed","children":[]},{"number":"05.12.10","serviceText":"Koordinering af gravearbejder og ledningsarbejder","children":[]}]},{"number":"05.13","serviceText":"Færdselsregulering/trafiksikkerhed","children":[{"number":"05.13.00","serviceText":"Færdselsregulering/trafiksikkerhed i almindelighed","children":[]},{"number":"05.13.01","serviceText":"Færdsels- og vejafmærkninger","children":[]},{"number":"05.13.02","serviceText":"Heller [udgået]","children":[]},{"number":"05.13.03","serviceText":"Vejvisere og skilte [udgået]","children":[]},{"number":"05.13.04","serviceText":"Færdselstavler [udgået]","children":[]},{"number":"05.13.05","serviceText":"Færdselsstriber [udgået]","children":[]},{"number":"05.13.06","serviceText":"Vejafmærkninger [udgået]","children":[]},{"number":"05.13.09","serviceText":"Autoværn, rækværker","children":[]},{"number":"05.13.10","serviceText":"Lokale trafiksaneringer","children":[]},{"number":"05.13.11","serviceText":"Færdselsregulering ved pludselig skade [udgået]","children":[]},{"number":"05.13.12","serviceText":"Lokale hastighedsbegrænsninger [udgået]","children":[]},{"number":"05.13.13","serviceText":"Ubetinget vigepligt [udgået]","children":[]},{"number":"05.13.14","serviceText":"Ensrettet færdsel [udgået]","children":[]},{"number":"05.13.15","serviceText":"Særtransport (blokvogne, vogntog, mobilkraner mv.)","children":[]},{"number":"05.13.16","serviceText":"Forbud mod visse færdselsarter [udgået]","children":[]},{"number":"05.13.17","serviceText":"Gågader [udgået]","children":[]},{"number":"05.13.18","serviceText":"Kørselsdispensationer","children":[]},{"number":"05.13.20","serviceText":"Kørselsafgifter/trafikbegrænsning","children":[]}]},{"number":"05.14","serviceText":"Genstande mv. på vejarealet","children":[{"number":"05.14.00","serviceText":"Genstande mv. på vejarealet i almindelighed","children":[]},{"number":"05.14.01","serviceText":"Bænke - råden over vejareal","children":[]},{"number":"05.14.02","serviceText":"Cykelstativer - råden over vejareal","children":[]},{"number":"05.14.03","serviceText":"Kiosker - råden over vejareal","children":[]},{"number":"05.14.04","serviceText":"Anbringelse af beholdere og lignende genstande på vej","children":[]},{"number":"05.14.05","serviceText":"Stadepladser - råden over vejareal","children":[]},{"number":"05.14.06","serviceText":"Postkasser - råden over vejareal","children":[]},{"number":"05.14.07","serviceText":"Skilte, plakatsøjler og transparenter - råden over vejareal","children":[]},{"number":"05.14.08","serviceText":"Benyttelse af gade- og fortovsarealer - råden over vejareal","children":[]},{"number":"05.14.09","serviceText":"Anbringelse af køretøjer - råden over vejareal","children":[]},{"number":"05.14.10","serviceText":"Kunst - råden over vejareal","children":[]},{"number":"05.14.11","serviceText":"Skure/skurvogne - råden over vejareal","children":[]},{"number":"05.14.12","serviceText":"Automater - råden over vejareal","children":[]},{"number":"05.14.13","serviceText":"Faste genstande over vejareal","children":[]},{"number":"05.14.14","serviceText":"Genstande, der medfører snesamling - råden over vejareal [udgår 2015]","children":[]},{"number":"05.14.15","serviceText":"Spildevand og tilledning af vand - råden over vejareal","children":[]},{"number":"05.14.17","serviceText":"Valgplakater - råden over vejareal","children":[]},{"number":"05.14.20","serviceText":"Træer, beplantning mv. - råden over vejareal","children":[]},{"number":"05.14.21","serviceText":"Tankanlæg - råden over vejareal","children":[]},{"number":"05.14.22","serviceText":"Sporanlæg - råden over vejareal [udgår 2015]","children":[]}]},{"number":"05.18","serviceText":"Vejbidrag","children":[{"number":"05.18.00","serviceText":"Vejbidrag i almindelighed","children":[]}]},{"number":"05.23","serviceText":"Tryghedsfremmende foranstaltninger","children":[{"number":"05.23.00","serviceText":"Tryghedsfremmende foranstaltninger i almindelighed","children":[]},{"number":"05.23.04","serviceText":"Tv-overvågning af offentligt område","children":[]}]},{"number":"05.26","serviceText":"Ekspropriation til offentlige vej- og stianlæg","children":[{"number":"05.26.00","serviceText":"Ekspropriation til offentlige vej- og stianlæg i almindelighed","children":[]},{"number":"05.26.05","serviceText":"Åstedsforretning, veje","children":[]},{"number":"05.26.10","serviceText":"Ekspropriationsbeslutning, veje","children":[]},{"number":"05.26.15","serviceText":"Ekspropriationserstatning, veje","children":[]}]}]}]');
				return result;
			},500);
			*/
			return httpGet('/kle/tree'); 
		}

		function addKle(kle,orgunit,assignment){
			return httpPost("/ou/" + orgunit.id + "/" + assignment + "/" + kle.number, null);
		}

		function removeKle(kle,orgunit,assignment){
			return httpDelete("/ou/" + orgunit.id + "/" + assignment + "/" + kle.number, null);
		}

		function simulateRestCall(functionToSimulate){ 
			return $timeout(functionToSimulate,10);
		}

		function httpGet(url, params) {
			var options = {
				//cache: cache
			};
			if (params) {
				//options.params = encodeURIComponent( JSON.stringify(params) );
				options.params = params;
			}
			return httpExecute(url, 'GET', options);
		}

		function httpPost(url, data) {
			return httpExecute(url, 'POST', {data: data});
		}

		function httpDelete(url, data) {
			return httpExecute(url, 'DELETE', {data: data});
		}

		function httpExecute(requestUrl, method, options) {
			var defaults = {
				url: baseUrl + requestUrl,
				method: method,
				withCredentials: true,
				headers: requestConfig.headers
			};

			$log.log(defaults.url);
			
			angular.extend(options, defaults); // merge defaults into options.
			appSpinner.showSpinner();
			return $http(options).then(
					function (response) {
						appSpinner.hideSpinner();
						return response.data;
					},
					function (reason) {
						appSpinner.hideSpinner();
						return $q.reject(reason);
					});
		}
	}
})();