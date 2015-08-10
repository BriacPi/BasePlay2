'use strict';

/* Controllers */

var dashBoardApp = angular.module('dashBoardApp');

dashBoardApp.controller('dashBoardAllCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
  $scope.natureData = [];
  $scope.natureColumns = [];
  $scope.statusData = [];
  $scope.statusColumns = [];
      $http.get('dashboard_data/all').success(function(data) {
        $scope.dashboard = data;
         $scope.natureData = [_.zipObject(data.natureChart.labels,data.natureChart.data)]
         $scope.natureColumns = data.natureChart.labels.map(function(label){
            return {id:label, type:'pie'}
         });
         $scope.statusData = [_.zipObject(data.statusChart.labels,data.statusChart.data)]
         $scope.statusColumns = data.statusChart.labels.map(function(label){
            return {id:label, type:'pie'}
         });
      });
    }]);

dashBoardApp.controller('dashBoardAllCaissesCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations',
  function($scope, $routeParams, $http,dashboardsTransformations) {
    $scope.dashboards =[]
      $http.get('dashboard_data/all_caisses').success(function(data) {
      console.log(dashboardsTransformations.transformDashboards(data))
        $scope.dashboards = dashboardsTransformations.transformDashboards(data);

      });
    }]);

dashBoardApp.controller('dashBoardCaisseCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
      $http.get('dashboard_data/caisse/'+$routeParams.caisse).success(function(data) {
        $scope.dashboards = data;
      });
    }]);

dashBoardApp.controller('dashBoardGroupeCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
      $http.get('dashboard_data/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe).success(function(data) {
        $scope.dashboards = data;
      });
    }]);

dashBoardApp.controller('dashBoardAgenceCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
      $http.get('dashboard_data/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence).success(function(data) {
        $scope.dashboards = data;
      });
    }]);

dashBoardApp.controller('dashBoardPdvCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
      $http.get('dashboard_data/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence+'/pdv/'+$routeParams.pdv).success(function(data) {
        $scope.dashboards = data;
      });
    }]);

dashBoardApp.controller('tilesCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
      $http.get('tiles/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence+'/pdv/'+$routeParams.pdv).success(function(data) {
        $scope.tiles = data;
      });
    }]);

