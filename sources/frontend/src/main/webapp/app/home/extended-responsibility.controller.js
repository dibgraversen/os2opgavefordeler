(function () {
    'use strict';
    angular.module('topicRouter').controller('ExtendedResponsibilityController', ExtendedResponsibilityController);

    ExtendedResponsibilityController.$inject = ['$log', '$scope', '$modalStack', 'topicRouterApi'];

    function ExtendedResponsibilityController($log, $scope, $modalStack, topicRouterApi) {
	    var defaultFilterType = 'cpr';

        $scope.close = close;
        $scope.type = defaultFilterType;
        $scope.removeFilter = removeFilter;
        $scope.add = add;
        $scope.currentTab = 'list';
	    $scope.ruleAlerts = [];
	    $scope.initialType = '';

        function cleanModel() {
	        $log.info('ExtendedResponsibilityController::cleanModel (type: ' + $scope.type + ')');

            $scope.selectedOrgUnit = {};
	        $scope.selectedEmp = {};

	        $scope.searchResult = {};
	        $scope.employments = {};

	        $scope.type = defaultFilterType;

	        $scope.ruleAlerts = [];

	        activateTab($scope.type); // select the correct tab based on filter type

            return {
                distributionRuleId: $scope.topic.id,
                type: $scope.type
            };
        }

        $scope.model = {
            distributionRuleId: $scope.topic.id,
            type: $scope.type,
	        name: '',
	        days: '',
	        months: '',
	        text: ''
        };

        var orgUnitsMissing = true;
        var currentEmployment = $scope.user.currentRole.employment;

	    $scope.loadAllOrgUnits = loadAllOrgUnits;
        $scope.selectedOrgUnit = {};
        $scope.setSelectedOrgUnit = setSelectedOrgUnit;
        $scope.employmentSearch = employmentSearch;
        $scope.updateFilter = updateFilter;
        $scope.createFilter = createFilter;
        $scope.loadMoreEmployments = loadMoreEmployments;
        $scope.setSelectedEmp = setSelectedEmp;
        $scope.show = show;
	    $scope.closeAlert = closeAlert;

        $scope.search = {
            municipalityId: $scope.user.municipality.id,
            offset: 0,
            pageSize: 10,
            nameTerm: '',
            initialsTerm: ''
        };

	    // activates the specified tab
	    function activateTab(tab) {
		    $scope.active = {}; //reset
		    $scope.active[tab] = true;
	    };

	    // set default active tab to CPR
	    $scope.active = {
		    cpr: true
	    };

	    var orgUnits = {};

	    activate();

        function show(filterId, filterType){
            $log.info("ExtendedResponsibilityController::show filter " + filterId + " (type: " + filterType + ")");

	        $scope.ruleAlerts = [];

	        $scope.model = cleanModel();
            $scope.currentTab = 'show';
            $scope.model.filterId = filterId;
	        $scope.model.type = filterType;
	        $scope.type = filterType;
	        $scope.initialType = filterType;

	        activateTab(filterType); // select the correct tab based on filter type

	        topicRouterApi.getFiltersForRule($scope.topic.id).then(function(res){
                for (var i in res) {
                    if (res[i].filterId == filterId) {
                        $scope.model = res[i];
                    }
                }

                for (var i in $scope.orgUnits) {
                    if ($scope.orgUnits[i].id == $scope.model.assignedOrgId) {
                        setSelectedOrgUnit($scope.orgUnits[i]);
                    }
                }
            });
        }

        function updateFilter(){
            $log.info("ExtendedResponsibilityController::updateFilter");

	        setFilterFromModel();
        }

        function createFilter(){
	        $log.info("ExtendedResponsibilityController::createFilter (initial type was '" + $scope.initialType + "', new type is: '" + $scope.model.type + "')");

			if ($scope.model.name) { // the name must be supplied
				if ($scope.selectedOrgUnit.id) { // the user must select an organisational unit
					if ($scope.initialType === '' || $scope.initialType === $scope.model.type) {
						if ($scope.model.type === 'cpr') { // make sure the user has provided days and/or months, if needed
							if ($scope.model.days || $scope.model.months) {
								// check that the numbers specified for days and/or months are valid
								var validDays = true;
								var validMonths = true;

								if ($scope.model.days) {
									validDays = validateDays($scope.model.days);
								}

								if ($scope.model.months) {
									validMonths = validateMonths($scope.model.months);
								}

								if (validDays && validMonths) {
									setFilterFromModel();
								}
								else {
									if (!validDays) {
										addAlert({
											type: 'warning',
											msg: 'Der er fejl i de angivne dage.'
										});
									}

									if (!validMonths) {
										addAlert({
											type: 'warning',
											msg: 'Der er fejl i de angivne måneder.'
										});
									}
								}
							}
							else {
								addAlert({
									type: 'warning',
									msg: 'Du skal angive dage og/eller måneder.'
								});
							}
						}
						else if ($scope.type = 'text') {
							if ($scope.model.text) {
								setFilterFromModel();
							}
							else {
								addAlert({
									type: 'warning',
									msg: 'Du skal angive en tekst.'
								});
							}
						}
						else {
							$log.error('Unknown filter type (valid values: cpr, text)');
						}
					}
					else {
						addAlert({
							type: 'warning',
							msg: 'Det er ikke muligt at ændre typen for en eksisterende regel.'
						});
					}
				}
				else {
					addAlert({
						type: 'warning',
						msg: 'Du skal som minimum vælge en organisatorisk enhed.'
					});
				}
			}
	        else {
				addAlert({
					type: 'warning',
					msg: 'Du skal angive et navn for den udvidede fordelingsregel.'
				});
			}
        }

	    function setFilterFromModel() {
		    $log.info('ExtendedResponsibilityController::setFilterFromModel (type: ' + $scope.model.type + ')');

		    topicRouterApi.createFilter($scope.model).then(function() {
			    _refresh();
			    $scope.currentTab = 'list'
		    });
	    }

        function setSelectedEmp(emp){
	        $scope.selectedEmp = emp;
            $scope.model.assignedEmployeeId = emp.id;
            $scope.model.assignedEmployeeName = emp.name;
        }

        function employmentSearch() {
            $scope.searchNotification = false;
            $scope.search.offset = 0;

            topicRouterApi.employmentSearch($scope.search).then(function(result){
                $scope.searchResult = result;
                $scope.employments = result.results;
            });
        }

        function loadMoreEmployments(){
            $scope.search.offset = $scope.search.offset + $scope.search.pageSize;
            topicRouterApi.employmentSearch($scope.search).then(function(result){
                $scope.searchResult = result;
                $scope.employments = $scope.employments.concat(result.results);
            });
        }

        function setSelectedOrgUnit(orgUnit){
            $scope.selectedOrgUnit = orgUnit;
            $scope.model.assignedOrgId = orgUnit.id;
            $scope.model.assignedOrgName = orgUnit.name;
        }

        function loadAllOrgUnits(){
            orgUnits = {};
            if(orgUnitsMissing){
                topicRouterApi.getOrgUnitsForResponsibility($scope.user.municipality.id, currentEmployment, false).then(function(orgUnits){
                    _.each(orgUnits, function(org){ loadParent(org); });
                    $scope.orgUnits = orgUnits;
                    orgUnitsMissing = false;
                });
            }
        }

        function loadParent(org){
            if (orgUnits[org.id]){
                // make sure it's overwritten so we only use one instance of each org.
                org = orgUnits[org.id];
            }
            else {
                orgUnits[org.id] = org; // make sure we don't work on duplicates.

                if (org.parentId && org.parent === undefined) {
                    if (orgUnits[org.parentId]) {
                        org.parent = orgUnits[org.parentId];
                    }
                    else {
                        topicRouterApi.getOrgUnit(org.parentId).then(function (parent) {
                            org.parent = parent;

                            if (parent.parentId) {
                                loadParent(parent);
                            }
                        });
                    }
                }
            }
        }

        function activate() {
            $log.info("ExtendedResponsibilityController::activate");

            _refresh();
        }

        function _refresh() {
            $log.info("ExtendedResponsibilityController::_refresh (rule: " + $scope.topic.id + ")");

	        topicRouterApi.getFiltersForRule($scope.topic.id).then(function (filters) {
                $scope.filters = filters;
            });
        }

        function add() {
            $scope.model = cleanModel();

	        $scope.initialType = '';

            $scope.currentTab = "add";
        }

        function removeFilter(filterId) {
            $log.info("ExtendedResponsibilityController::removing filter " + filterId + ' for rule ' + $scope.topic.id);

            topicRouterApi.removeFilter($scope.topic.id, filterId).then(function () {
                _refresh();
            });

            return true;
        }

        function close() {
            $log.info("ExtendedResponsibilityController::close");

	        $modalStack.dismissAll('cancel');
        }

	    function addAlert(alert) {
		    $scope.ruleAlerts.push(alert);
	    }

	    function closeAlert(index) {
		    $scope.ruleAlerts.splice(index, 1);
	    }

	    function isInt(value) {
		    return !isNaN(value) &&
				    parseInt(Number(value)) == value &&
				    !isNaN(parseInt(value, 10));
	    }

	    function isValidDay(value) {
		    return isInt(value) && parseInt(Number(value)) > 0 && parseInt(Number(value)) < 32;
	    }

	    function isValidMonth(value) {
		    return isInt(value) && parseInt(Number(value)) > 0 && parseInt(Number(value)) < 13;
	    }

	    function validateDays(input) {
		    var dayStrings = input.split(',');

		    for (var i = 0; i < dayStrings.length; i++) {
			    if (dayStrings[i].indexOf('-') > -1) { // interval
					var dayArr = dayStrings[i].split('-');

				    if (dayArr.length == 2) {
						if (!isValidDay(dayArr[0]) || !isValidDay(dayArr[1])) {
							return false;
						}
				    }
				    else {
					    return false;
				    }
			    }
			    else { // single day
				    if (!isValidDay(dayStrings[i])) { // one or more days are invalid
					    return false;
				    }
			    }
		    }

		    return true;
	    }

	    function validateMonths(input) {
		    var monthsStrings = input.split(',');

		    for (var i = 0; i < monthsStrings.length; i++) {
			    if (monthsStrings[i].indexOf('-') > -1) { // interval
				    var monthArr = monthsStrings[i].split('-');

				    if (monthArr.length == 2) {
					    if (!isValidMonth(monthArr[0]) || !isValidMonth(monthArr[1])) {
						    return false;
					    }
				    }
				    else {
					    return false;
				    }
			    }
			    else { // single day
				    if (!isValidMonth(monthsStrings[i])) { // one or more days are invalid
					    return false;
				    }
			    }
		    }

		    return true;
	    }
    }
})();
