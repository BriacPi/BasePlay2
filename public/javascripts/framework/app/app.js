'use strict';

/* App Module */

var dashBoardApp = angular.module('dashBoardApp', ['ngRoute','gridshore.c3js.chart']);

dashBoardApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.

      when('/dashboard/all', {
        templateUrl: 'assets/javascripts/framework/partials/one.html',
        controller: 'dashBoardAllCtrl'
      }).
      when('/dashboard/all_caisses', {
        templateUrl: 'assets/javascripts/framework/partials/many.html',
        controller: 'dashBoardAllCaissesCtrl'
      }).
      when('/dashboard/caisse/:caisse', {
        templateUrl: 'assets/javascripts/framework/partials/many.html',
        controller: 'dashBoardCaisseCtrl'
      }).
      when('/dashboard/caisse/:caisse/groupe/:groupe', {
        templateUrl: 'assets/javascripts/framework/partials/many.html',
        controller: 'dashBoardGroupeCtrl'
      }).
      when('/dashboard/caisse/:caisse/groupe/:groupe/agence/:agence', {
        templateUrl: 'assets/javascripts/framework/partials/many.html',
        controller: 'dashBoardAgenceCtrl'
      }).
      when('/tiles/caisse/:caisse/groupe/:groupe/agence/:agence/pdv/:pdv', {
        templateUrl: 'assets/javascripts/framework/partials/tiles.html',
        controller: 'tilesCtrl'
      }).
      otherwise({
        redirectTo: '/dashboard/all'
      });
  }]);
