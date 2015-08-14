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


dashBoardApp.controller('tilesCtrl', ['$scope', '$routeParams','$http','breadCrumbs','tilesTransformations','urlGenerator',
  function($scope, $routeParams, $http,breadCrumbs,tilesTransformations,urlGenerator) {
      $http.get('tiles/caisse/'+$routeParams.caisse+'/groupe/'+$routeParams.groupe+'/agence/'+$routeParams.agence+'/pdv/'+$routeParams.pdv).success(function(data) {
         $scope.tiles = tilesTransformations.transformTiles(data);
      });

       $scope.breadCrumbs= breadCrumbs.getBreadCrumbs("pdv");
    }]);


var dataApp = angular.module('dataApp');

dataApp.controller('dataCtrl', ['$scope', '$routeParams','$http','tilesTransformations','hierarchy',
  function($scope, $routeParams, $http,tilesTransformations,hierarchy) {
      var tiles = [];
       $scope.hierarchytree = []
       $scope.metricstree=[];
      $http.get('data/all').success(function(data) {
         tiles = tilesTransformations.transformTiles(data);
         $scope.tiles=tiles;
         $scope.metricstree = _.unique(tiles.map(function(tile){
            return tile.metricName;
         })).sort().map(function(metricName){
            return {name:metricName,children:[]}
         });
         $scope.hierarchytree = hierarchy.getHierarchy(tiles);
         console.log($scope.metricstree,$scope.hierarchytree);
      });

      $scope.caisse="";
      $scope.groupe="";
      $scope.agence="";
      $scope.pdv="";
      $scope.metrics=[];
      $scope.groupByVar='statusName';
      $scope.underCurrentHierarchy='caisse';

      $scope.filteredTiles=tiles
      $scope.filter = {};

      $scope.filterTiles= function(){
        $scope.tiles=tiles.filter(function(tile){

            return ($scope.metrics.length==0 || (_.indexOf($scope.metrics,tile.metricName)>=0)) &&
                ($scope.caisse=="" || tile.caisse==$scope.caisse)&&
                ($scope.groupe=="" || tile.groupe==$scope.groupe)&&
                ($scope.agence=="" || tile.agence==$scope.agence)&&
                ($scope.pdv=="" || tile.pdv==$scope.pdv);
        });
      };
      $scope.setHierarchy= function(items){
          if(items){
            var item=items[0];
            if (item.type == 'caisse') {
                $scope.caisse=item.name;
                $scope.groupe="";
                $scope.agence="";
                $scope.pdv="";
                $scope.underCurrentHierarchy='groupe'
            }
            if (item.type == 'groupe') {
                $scope.caisse=item.caisse;
                $scope.groupe=item.name;
                $scope.agence="";
                $scope.pdv="";
                $scope.underCurrentHierarchy='agence'
            }
            if (item.type == 'agence') {
                $scope.caisse=item.caisse;
                $scope.groupe=item.groupe;
                $scope.agence=item.name;
                $scope.pdv="";
                $scope.underCurrentHierarchy='pdv';
            }
            if (item.type == 'pdv') {
                $scope.caisse=item.caisse;
                $scope.groupe=item.groupe;
                $scope.agence=item.agence;
                $scope.pdv=item.name;
                $scope.underCurrentHierarchy='pdv';
            }

          } else {
                  $scope.caisse="";
                  $scope.groupe="";
                  $scope.agence="";
                  $scope.pdv="";
          }
          $scope.filterTiles();
          if ( $scope.groupByVar!='statusName'){$scope.byStatus('hierarchy');}

      };


      $scope.setMetrics= function(items){
          if(items){
             $scope.metrics=items.map(function(item){
                return item.name;
             });

          } else {
             $scope.metrics=[];
          }
          $scope.filterTiles();
      };


      $scope.byStatus= function(string){
        if (string=='status'){
         $scope.groupByVar='statusName';
        }
        else {
         $scope.groupByVar=$scope.underCurrentHierarchy;
        }

      };
  }
]);

var mytasksApp = angular.module('mytasksApp');

mytasksApp.controller('dataCtrl', ['$scope', '$routeParams','$http','tilesTransformations','hierarchy',
  function($scope, $routeParams, $http,tilesTransformations,hierarchy) {
      var tiles = [];
       $scope.hierarchytree = []
       $scope.metricstree=[];
      $http.get('data/mytasks').success(function(data) {
         tiles = tilesTransformations.transformTiles(data);
         $scope.tiles=tiles;
         $scope.metricstree = _.unique(tiles.map(function(tile){
            return tile.metricName;
         })).sort().map(function(metricName){
            return {name:metricName,children:[]}
         });
         $scope.hierarchytree = hierarchy.getHierarchy(tiles);
         console.log($scope.metricstree,$scope.hierarchytree);
      });

      $scope.caisse="";
      $scope.groupe="";
      $scope.agence="";
      $scope.pdv="";
      $scope.metrics=[];
      $scope.groupByVar='statusName';
      $scope.underCurrentHierarchy='caisse';

      $scope.filteredTiles=tiles
      $scope.filter = {};

      $scope.filterTiles= function(){
        $scope.tiles=tiles.filter(function(tile){

            return ($scope.metrics.length==0 || (_.indexOf($scope.metrics,tile.metricName)>=0)) &&
                ($scope.caisse=="" || tile.caisse==$scope.caisse)&&
                ($scope.groupe=="" || tile.groupe==$scope.groupe)&&
                ($scope.agence=="" || tile.agence==$scope.agence)&&
                ($scope.pdv=="" || tile.pdv==$scope.pdv);
        });
      };
      $scope.setHierarchy= function(items){
          if(items){
            var item=items[0];
            if (item.type == 'caisse') {
                $scope.caisse=item.name;
                $scope.groupe="";
                $scope.agence="";
                $scope.pdv="";
                $scope.underCurrentHierarchy='groupe'
            }
            if (item.type == 'groupe') {
                $scope.caisse=item.caisse;
                $scope.groupe=item.name;
                $scope.agence="";
                $scope.pdv="";
                $scope.underCurrentHierarchy='agence'
            }
            if (item.type == 'agence') {
                $scope.caisse=item.caisse;
                $scope.groupe=item.groupe;
                $scope.agence=item.name;
                $scope.pdv="";
                $scope.underCurrentHierarchy='pdv';
            }
            if (item.type == 'pdv') {
                $scope.caisse=item.caisse;
                $scope.groupe=item.groupe;
                $scope.agence=item.agence;
                $scope.pdv=item.name;
                $scope.underCurrentHierarchy='pdv';
            }

          } else {
                  $scope.caisse="";
                  $scope.groupe="";
                  $scope.agence="";
                  $scope.pdv="";
          }
          $scope.filterTiles();
          if ( $scope.groupByVar!='statusName'){$scope.byStatus('hierarchy');}

      };


      $scope.setMetrics= function(items){
          if(items){
             $scope.metrics=items.map(function(item){
                return item.name;
             });

          } else {
             $scope.metrics=[];
          }
          $scope.filterTiles();
      };


      $scope.byStatus= function(string){
        if (string=='status'){
         $scope.groupByVar='statusName';
        }
        else {
         $scope.groupByVar=$scope.underCurrentHierarchy;
        }

      };
  }
]);



