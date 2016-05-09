(function () {
    'use strict';
    angular.module('topicRouter').controller('ExtendedResponsibilityController', ExtendedResponsibilityController);

    ExtendedResponsibilityController.$inject = ['$log', '$scope', 'topicRouterApi'];

    function ExtendedResponsibilityController($log, $scope, topicRouterApi) {
        //$scope.employeeFilter = "";

        $scope.close = close;
        $scope.type = "cpr";
        $scope.removeFilter = removeFilter;
        $scope.add = add;
        $scope.currentTab = 'list';

        function cleanModel() {
            $scope.selectedOrgUnit = {};
            return {
            distributionRuleId: $scope.topic.id,
            type: 'cpr'
            };
        }

        $scope.model = {
            distributionRuleId: $scope.topic.id,
            type: 'cpr'
        };
        var orgUnitsMissing = true;
        var currentEmployment = $scope.user.currentRole.employment;
        //$scope.orgUnits = [];
        $scope.loadAllOrgUnits = loadAllOrgUnits;
        $scope.selectedOrgUnit = {};
        $scope.setSelectedOrgUnit = setSelectedOrgUnit;
        $scope.employmentSearch = employmentSearch;
        $scope.updateFilterd = updateFilterd;
        $scope.createFilter = createFilter;
        $scope.loadMoreEmployments = loadMoreEmployments;
        $scope.setSelectedEmp = setSelectedEmp;
        $scope.show = show;
        $scope.search = {
            municipalityId: $scope.user.municipality.id,
            offset: 0,
            pageSize: 10,
            nameTerm: '',
            initialsTerm: ''
        };
        var orgUnits = {};
        activate();

        function show(filterId){
            $log.info("ExtendedResponsibilityController::show " + filterId);
            $scope.model = cleanModel();
            $scope.currentTab='show';
            $scope.model.filterId = filterId;
            topicRouterApi.getFiltersForRule($scope.topic.id).then(function(res){
                for(var i in res){
                    if(res[i].filterId == filterId){
                        $scope.model = res[i];
                    }
                }
                for(var i in $scope.orgUnits){
                    if($scope.orgUnits[i].id == $scope.model.assignedOrgId){
                        setSelectedOrgUnit($scope.orgUnits[i]);
                    }
                }
            });
        }

        function updateFilterd(){
            $log.info("ExtendedResponsibilityController::updateFilter");
            topicRouterApi.updateFilter($scope.model).then(function(){
                _refresh();
                $scope.currentTab = 'list'
            });
        }

        function createFilter(){
            topicRouterApi.createFilter($scope.model).then(function(){
                _refresh();
                $scope.currentTab = 'list'
            });
        }

        function setSelectedEmp(emp){
            $scope.model.assignedEmployeeId = emp.id;
            $scope.model.assignedEmployeeName = emp.name;
        }

        function employmentSearch(){
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
            if(orgUnits[org.id]){
                // make sure it's overwritten so we only use one instance of each org.
                org = orgUnits[org.id];
            } else {
                orgUnits[org.id] = org; // make sure we don't work on duplicates.
                if(org.parentId && org.parent === undefined) {
                    if(orgUnits[org.parentId]){
                        org.parent = orgUnits[org.parentId];
                    } else {
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
            $log.info("ExtendedResponsibilityController::_refresh");
            topicRouterApi.getFiltersForRule(1).then(function (filters) {
                $scope.filters = filters;
            });
        }

        function add() {
            $scope.model = cleanModel();
            $scope.currentTab = "add";
        }

        function removeFilter(filterId) {
            $log.info("ExtendedResponsibilityController::removing " + filterId);

            topicRouterApi.removeFilter(1, filterId).then(function () {
                _refresh();
            });

            return true;
        }

        function close() {
            $log.info("ExtendedResponsibilityController::close");
            //$modalInstance.dismiss('cancel');
        }

    }
})();
