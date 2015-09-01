var serviceModule = angular.module('ServiceModule', []);

serviceModule.factory('dashboardsTransformations', ['$routeParams',
  function($routeParams){
    return {
       transformDashboards : function(dashboards){
        return dashboards.map(function(dashboard){
              var linkTitle;
        if (dashboard.level=="caisse")  {
            linkTitle= "dashboard#/dashboard/caisse/"+dashboard.title
        } else if (dashboard.level=="groupe") {
            linkTitle= "dashboard#/dashboard/caisse/"+$routeParams.caisse+"/groupe/"+dashboard.title
        }else if (dashboard.level=="agence") {
            linkTitle= "dashboard#/dashboard/caisse/"+$routeParams.caisse+"/groupe/"+$routeParams.groupe+"/agence/"+dashboard.title
        }else if (dashboard.level=="pdv") {
            linkTitle= "dashboard#/tiles/caisse/"+$routeParams.caisse+"/groupe/"+$routeParams.groupe+"/agence/"+$routeParams.agence+"/pdv/"+dashboard.title
        }


            return {title:dashboard.title,
                    numberOfRows:dashboard.numberOfRows,
                    leaderboard:dashboard.leaderboard.map(function(line){
                        var link;
                        if (dashboard.level=="all" )  {
                            link= "dashboard#/dashboard/caisse/"+line.name;
                        }else if (dashboard.level=="caisse")  {
                            link= "dashboard#/dashboard/caisse/"+dashboard.title+"/groupe/"+line.name;
                        }else if (dashboard.level=="groupe") {
                            link= "dashboard#/dashboard/caisse/"+$routeParams.caisse+"/groupe/"+dashboard.title+"/agence/"+line.name;
                        }else if (dashboard.level=="agence") {
                            link= "dashboard#/tiles/caisse/"+$routeParams.caisse+"/groupe/"+$routeParams.groupe+"/agence/"+dashboard.title+"/pdv/"+line.name;
                        }
                        return {name:line.name,numberOfUnsolvedAnomalies:line.numberOfUnsolvedAnomalies,link:link};
                    }),
                natureData: [_.zipObject(dashboard.natureChart.labelsForDisplay,dashboard.natureChart.data)],
                natureColumns :
                             _.zip(dashboard.natureChart.labels,dashboard.natureChart.labelsForDisplay).map(function(labelObject){
                            if (labelObject[0]=="Abnormality"){var color = "orange"}
                            else if (labelObject[0]=="NotAbnormality") {var color ="orange"}
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
                linkTitle: linkTitle,
                level:dashboard.level
            }
        });
       }
    };
  }]);

serviceModule.factory('breadCrumbs', ['$routeParams',
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
                        breadCrumbs.push({url:"dashboard#/tiles/caisse/"+$routeParams.caisse+
                        "/groupe/"+$routeParams.groupe+
                        "/agence/"+$routeParams.agence+
                        "/pdv/"+$routeParams.pdv
                        ,display:$routeParams.pdv})

         }
         return breadCrumbs;
    }
} }
 ]);

 serviceModule.factory('tilesTransformations', ['$routeParams',
   function($routeParams){
     return {
        transformTiles : function(tiles){
            if (navigator.language == 'fr'){var language ='fr'} else {var language ='en'}

            return tiles.map(function(tile){
             var value = sprintf(language)(tile.format, tile.value);
             return {
                metricName:tile.metricName,
                date:tile.date,
                value:value,
                id:tile.id,
                status:tile.statusCode,
                statusName:tile.status,
                caisse:tile.caisse,
                groupe:tile.groupe,
                agence:tile.agence,
                pdv:tile.pdv
             }
         });
        }
     };
   }]);
   
 serviceModule.factory('hierarchy', [
    function($routeParams){
       return {
          getHierarchy : function(tiles){
                function tileToPdv(tile){
                    return {caisse:tile.caisse,groupe:tile.groupe,agence:tile.agence,type:'pdv',name:tile.pdv,children:[]};
                };
                function tileToAgence(tile){
                    return {caisse:tile.caisse,groupe:tile.groupe,agence:"",type:'agence',name:tile.agence,children:[tileToPdv(tile)]};
                };
                function tileToGroupe(tile){
                    return {caisse:tile.caisse,groupe:"",agence:"",type:'groupe',name:tile.groupe,children:[tileToAgence(tile)]};
                };
                function tileToCaisse(tile){
                    return {caisse:"",groupe:"",agence:"",type:'caisse',name:tile.caisse,children:[tileToGroupe(tile)]};
                };

                var caisses =[];
                tiles.forEach(function(tile){
                var caissesNames = _.unique(caisses.map(function(caisse){return caisse.name;}));
                    var indexOfCaisse = _.indexOf(caissesNames,tile.caisse);
                    if (indexOfCaisse>=0){
                        var groupes = caisses[indexOfCaisse].children;
                        var groupesNames = _.unique(groupes.map(function(groupe){return groupe.name;}));
                                    var indexOfGroupe = _.indexOf(groupesNames,tile.groupe);
                                    if (indexOfGroupe>=0){
                                        var agences = caisses[indexOfCaisse].children[indexOfGroupe].children;
                                        var agencesNames = _.unique(agences.map(function(agence){return agence.name;}));
                                                                    var indexOfAgence = _.indexOf(agencesNames,tile.agence);
                                                                    if (indexOfAgence>=0){
                                                                             var pdvs = caisses[indexOfCaisse].children[indexOfGroupe].children[indexOfAgence].children;
                                                                             var pdvsNames = _.unique(pdvs.map(function(pdv){return pdv.name;}));
                                                                                                         var indexOfPdv = _.indexOf(pdvsNames,tile.pdv);
                                                                                                         if (indexOfPdv>=0){
        
                                                                                                         } else {
                                                                                                             caisses[indexOfCaisse].children[indexOfGroupe].children[indexOfAgence].children.push(tileToPdv(tile))
                                                                                                         }
                                                                    } else {
                                                                        caisses[indexOfCaisse].children[indexOfGroupe].children.push(tileToAgence(tile))
                                                                    }
                                    } else {
                                        caisses[indexOfCaisse].children.push(tileToGroupe(tile))
                                    }
                    } else {
                        caisses.push(tileToCaisse(tile))
                    }
        
                });
                var sortRec = function(levels){
                    return levels.map(function(level){
                        return {caisse:level.caisse,groupe:level.groupe,agence:level.agence,type:level.type,name:level.name,children:sortRec(level.children)}
                    }).sort(function (a, b) {
                         if (a.name > b.name) {
                           return 1;
                         }
                         if (a.name < b.name) {
                           return -1;
                         }
                         // a must be equal to b
                         return 0;
                       });
                };
                return sortRec(caisses);
                  }
       };
    }
 ]);



