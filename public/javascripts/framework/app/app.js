'use strict';

/* App Module */

var dashBoardApp = angular.module('dashBoardApp', ['ngRoute','gridshore.c3js.chart','ServiceModule']);

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

var dataApp = angular.module('dataApp', ['ngRoute','ServiceModule','hierarchical-selector','angular.filter']);

dataApp.config(['$routeProvider',
  function($routeProvider) {
    $routeProvider.

      when('/data/caisse/:caisse/groupe/:groupe/agence/:agence/pdv/:pdv', {
        templateUrl: 'assets/javascripts/framework/partials/data.html',
        controller: 'dataCtrl'
      }).
      otherwise({
              redirectTo: '/data/caisse/all/groupe/all/agence/all/pdv/all'
            });
  }
]);


angular.module("hierarchical-selector").run(["$templateCache", function($templateCache) {$templateCache.put("hierarchical-selector.tpl.html","<div class=\"hierarchical-control\">\r\n  <div class=\"control-group\">\r\n    <button type=\"button\" ng-if=\"showButton\" class=\"pull-down\" ng-click=\"onButtonClicked($event)\"><div class=\"arrow-down\"></div></button>\r\n    <div class=\"hierarchical-input form-control\" ng-class=\"{\'with-btn\': showButton}\" ng-click=\"onControlClicked($event)\">\r\n   <span ng-if=\"selectedItems.length == 0\">Aucun filtre</span> <span ng-if=\"selectedItems.length > 0\" class=\"selected-items\">\r\n        <span ng-repeat=\"i in selectedItems\" class=\"selected-item\">{{getTagName(i)}} <span class=\"selected-item-close\" ng-click=\"deselectItem(i, $event)\"></span></span>\r\n      </span>\r\n      <!-- <input type=\"text\" class=\"blend-in\" /> -->\r\n    </div>\r\n  </div>\r\n  <div class=\"tree-view\" ng-show=\"showTree\">\r\n    <ul>\r\n      <tree-item class=\"top-level\" ng-repeat=\"item in data\" item=\"item\" select-only-leafs=\"selectOnlyLeafs\" use-can-select-item=\"useCanSelectItemCallback\" can-select-item=\"canSelectItem\" multi-select=\"multiSelect\" item-selected=\"itemSelected(item)\" on-active-item=\"onActiveItem(item)\" load-child-items=\"loadChildItems\" async=\"isAsync\" item-has-children=\"hasChildren(parent)\" async-child-cache=\"asyncChildCache\" />\r\n    </ul>\r\n  </div>\r\n</div>\r\n");

}]);