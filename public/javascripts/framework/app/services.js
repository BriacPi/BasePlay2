var dashBoardApp = angular.module('dashBoardApp');

dashBoardApp.factory('dashboardsTransformations', [
  function(){
    return {
       transformDashboards : function(dashboards){
        return dashboards.map(function(dashboard){
            return {title:dashboard.title,
                numberOfRows:dashboard.numberOfRows,
                leaderboard:dashboard.leaderboard,
                natureData: [_.zipObject(dashboard.natureChart.labels,dashboard.natureChart.data)],
                natureColumns : dashboard.natureChart.labels.map(function(label){
                            return {id:label, type:'pie'};
                         }),
                statusData: [_.zipObject(dashboard.statusChart.labels,dashboard.statusChart.data)],
                statusColumns : dashboard.statusChart.labels.map(function(label){
                            return {id:label, type:'pie'};
                         })
            }
        });
       }
    };
  }]);