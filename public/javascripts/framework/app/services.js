var dashBoardApp = angular.module('dashBoardApp');

dashBoardApp.factory('dashboardsTransformations', ['$routeParams',
  function($routeParams){
    return {
       transformDashboards : function(dashboards){
        return dashboards.map(function(dashboard){
              var link;
        if (dashboard.level=="caisse")  {
            link= "dashboard#/dashboard/caisse/"+dashboard.title
        } else if (dashboard.level=="groupe") {
            link= "dashboard#/dashboard/caisse/"+$routeParams.caisse+"/groupe/"+dashboard.title
        }else if (dashboard.level=="agence") {
            link= "dashboard#/dashboard/caisse/"+$routeParams.caisse+"/groupe/"+$routeParams.groupe+"/agence/"+dashboard.title
        }else if (dashboard.level=="pdv") {
            link= "dashboard#/dashboard/caisse/"+$routeParams.caisse+"/groupe/"+$routeParams.roupe+"/agence/"+$routeParams.agence+"/pdv/"+dashboard.title
        }


            return {title:dashboard.title,
                numberOfRows:dashboard.numberOfRows,
                leaderboard:dashboard.leaderboard,
                natureData: [_.zipObject(dashboard.natureChart.labelsForDisplay,dashboard.natureChart.data)],
                natureColumns :
                             _.zip(dashboard.natureChart.labels,dashboard.natureChart.labelsForDisplay).map(function(labelObject){
                            if (labelObject[0]=="Solved"){var color = "green"}
                            else if (labelObject[0]=="BeingProcessed") {var color ="orange"}
                            else {var color = "red"}
                            return {id:labelObject[1], type:'donut',color:color};
                         }),
                statusData: [_.zipObject(dashboard.statusChart.labelsForDisplay,dashboard.statusChart.data)],
                statusColumns :
                             _.zip(dashboard.statusChart.labels,dashboard.statusChart.labelsForDisplay).map(function(labelObject){
                            if (labelObject[0]=="Solved"){var color = "green"}
                            else if (labelObject[0]=="BeingProcessed") {var color ="orange"}
                            else {var color = "red"}
                            return {id:labelObject[1], type:'donut',color:color};
                         }),
                link: link,
                level:dashboard.level
            }
        });
       }
    };
  }]);

dashBoardApp.factory('breadCrumbs', ['$routeParams',
 function($routeParams){
    return {
        getBreadCrumbs : function(level){
        var breadCrumbs=[];

             if (level=="all" ||level=="all_caisses" ||level=="caisse" ||level=="groupe"||level=="agence"||level=="pdv")  {
                        breadCrumbs.push({url:"dashboard#/dashboard/all",display:"Tout"})
             }
             if (level=="all_caisses" ||level=="caisse" ||level=="groupe"||level=="agence"||level=="pdv")  {
                        breadCrumbs.push({url:"dashboard#/dashboard/all_caisses",display:"Par caisse"})
             }
             if (level=="caisse" ||level=="groupe"||level=="agence"||level=="pdv")  {
                        breadCrumbs.push({url:"dashboard#/dashboard/caisse/"+$routeParams.caisse,display:$routeParams.caisse})
             }
             if (level=="groupe"||level=="agence"||level=="pdv") {
                        breadCrumbs.push({url:"dashboard#/dashboard/caisse/"+$routeParams.caisse+
                        "/groupe/"+$routeParams.groupe
                        ,display:$routeParams.groupe})
             }
             if (level=="agence"||level=="pdv") {
                        breadCrumbs.push({url:"dashboard#/dashboard/caisse/"+$routeParams.caisse+
                        "/groupe/"+$routeParams.groupe+
                        "/agence/"+$routeParams.agence
                        ,display:$routeParams.agence})
             }
             if (level=="pdv") {
                        breadCrumbs.push({url:"dashboard#/dashboard/caisse/"+$routeParams.caisse+
                        "/groupe/"+$routeParams.groupe+
                        "/agence/"+$routeParams.agence+
                        "/pdv/"+$routeParams.pdv
                        ,display:$routeParams.pdv})

         }
         return breadCrumbs;
    }
} }
 ]);