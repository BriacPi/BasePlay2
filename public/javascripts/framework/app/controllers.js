'use strict';

/* Controllers */

var dashBoardApp = angular.module('dashBoardApp');

dashBoardApp.controller('dashBoardAllCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations','breadCrumbs',
  function($scope, $routeParams, $http,dashboardsTransformations,breadCrumbs) {
  $scope.dashboard = {};

  $scope.dashboard.natureData = [];
  $scope.dashboard.natureColumns = [{type:'donut'}];
  $scope.dashboard.statusData = [];
  $scope.dashboard.statusColumns = [{type:'donut'}];
      $http.get('dashboard_data/all').success(function(data) {
        $scope.dashboard = dashboardsTransformations.transformDashboards([data])[0]
      });
  $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("all")
    }]);

dashBoardApp.controller('dashBoardAllCaissesCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations','breadCrumbs',
  function($scope, $routeParams, $http,dashboardsTransformations,breadCrumbs) {
      $scope.dashboard = {};

      $scope.dashboard.natureData = [];
      $scope.dashboard.natureColumns = [{type:'donut'}];
      $scope.dashboard.statusData = [];
      $scope.dashboard.statusColumns = [{type:'donut'}];
      $http.get('dashboard_data/all_caisses').success(function(data) {
      console.log(dashboardsTransformations.transformDashboards(data))
        $scope.dashboards = dashboardsTransformations.transformDashboards(data);

      });
      $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("all_caisses")
    }]);

dashBoardApp.controller('dashBoardCaisseCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations','breadCrumbs',
  function($scope, $routeParams, $http,dashboardsTransformations,breadCrumbs) {
    $scope.dashboard = {};

    $scope.dashboard.natureData = [];
    $scope.dashboard.natureColumns = [{type:'donut'}];
    $scope.dashboard.statusData = [];
    $scope.dashboard.statusColumns = [{type:'donut'}];
    $http.get('dashboard_data/caisse/'+$routeParams.caisse).success(function(data) {
        $scope.dashboards = dashboardsTransformations.transformDashboards(data);
      });
      $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("caisse")
    }]);

dashBoardApp.controller('dashBoardGroupeCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations','breadCrumbs',
  function($scope, $routeParams, $http,dashboardsTransformations,breadCrumbs) {
    $scope.dashboard = {};

    $scope.dashboard.natureData = [];
    $scope.dashboard.natureColumns = [{type:'donut'}];
    $scope.dashboard.statusData = [];
    $scope.dashboard.statusColumns = [{type:'donut'}];
    $http.get('dashboard_data/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe).success(function(data) {
        $scope.dashboards = dashboardsTransformations.transformDashboards(data);
      });
      $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("groupe");
    }]);

dashBoardApp.controller('dashBoardAgenceCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations','breadCrumbs',
  function($scope, $routeParams, $http,dashboardsTransformations,breadCrumbs) {
    $scope.dashboard = {};

    $scope.dashboard.natureData = [];
    $scope.dashboard.natureColumns = [{type:'donut'}];
    $scope.dashboard.statusData = [];
    $scope.dashboard.statusColumns = [{type:'donut'}];
    $http.get('dashboard_data/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence).success(function(data) {
        $scope.dashboards = dashboardsTransformations.transformDashboards(data);
      });
      $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("agence")
    }]);

dashBoardApp.controller('dashBoardPdvCtrl', ['$scope', '$routeParams','$http','dashboardsTransformations','breadCrumbs',
  function($scope, $routeParams, $http,dashboardsTransformations,breadCrumbs) {
    $scope.dashboard = {};

    $scope.dashboard.natureData = [];
    $scope.dashboard.natureColumns = [{type:'donut'}];
    $scope.dashboard.statusData = [];
    $scope.dashboard.statusColumns = [{type:'donut'}];
    $http.get('dashboard_data/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence+'/pdv/'+$routeParams.pdv).success(function(data) {
        $scope.dashboards = dashboardsTransformations.transformDashboards(data);
      });
      $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("pdv")
    }]);

dashBoardApp.controller('tilesCtrl', ['$scope', '$routeParams','$http',
  function($scope, $routeParams, $http) {
      $http.get('tiles/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence+'/pdv/'+$routeParams.pdv).success(function(data) {
        $scope.tiles = data;
      });
    }]);



