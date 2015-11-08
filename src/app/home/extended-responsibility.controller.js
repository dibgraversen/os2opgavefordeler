(function () {
    'use strict';
    angular.module('topicRouter').controller('ExtendedResponsibilityController', ExtendedResponsibilityController);

    ExtendedResponsibilityController.$inject = ['$log', '$scope', 'topicRouterApi'];

    function ExtendedResponsibilityController($log, $scope, topicRouterApi) {
        //$scope.employeeFilter = "";

        $scope.close = close;
        $scope.removeFilter = removeFilter;
        $scope.add = add;
        $scope.currentTab = 'list';
        $scope.model = {distributionRuleId: $scope.topic.id};
        //$scope.orgUnits = [];
        $scope.loadAllOrgUnits = loadAllOrgUnits;
        $scope.selectedOrgUnit = {};
        $scope.setSelectedOrgUnit = setSelectedOrgUnit;
        $scope.employmentSearch = employmentSearch;
        $scope.createFilter = createFilter;
        $scope.loadMoreEmployments = loadMoreEmployments;
        $scope.setSelectedEmp = setSelectedEmp;
        $scope.show = show;
        $scope.updateFilter = updateFilter;
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

        function updateFilter(){
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
                topicRouterApi.getOrgUnitsForResponsibility(municipality.id, currentEmployment, false).then(function(orgUnits){
                    _.each(orgUnits, function(org){ loadParent(org); });
                    $scope.orgUnits = orgUnits;
                    orgUnitsMissing = false;
                });
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
