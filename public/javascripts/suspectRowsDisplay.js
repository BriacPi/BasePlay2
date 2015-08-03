$(document).ready(function () {

    var $content = $('#content');

    grabRows();


    function showRows(rows){

        if (!rows) return;
        var items = rows.map(function(row){

            return $('<div class="tile '+row.caisse
                    +'" data-status="' + row.status
                    +'" data-caisse="' + row.caisse
                    +'" data-groupe="' + row.groupe
                    +'" data-agence="' + row.agence
                    +'" data-pdv="' + row.pdv
                    + '">'
                    +'<br>' + row.caisse
                    +'<br>' + row.groupe
                    +'<br>' + row.agence
                    +'<br>' + row.pdv
                    +
                     '</div>');

        });

        $content.append( items );
        $content.isotope({
                itemSelector: '.tile',
                layoutMode: 'fitRows'
        });

    };

    function showFilters(rows,hierarchicalLevels){

        var filterFns = {
          status: function(wantedStatus,currentStatus) {
             return currentStatus== wantedStatus
          }
        };

        function filter(){
            var filterValueStatus = $('.filter-button-group-status button.selected').data('filter');
            var filterValuesHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).val() })

            var namesOfFilterForHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).data('filter') })
            $content.isotope({ filter: function(){
                            var self = this;
                            var status = $(self).data('status');
                            var hierarchies = namesOfFilterForHierarchy.map(function(index, name){
                                return $(self).data(name);
                            });
                            var zipFilterValue = _.zip(hierarchies,filterValuesHierarchy).map(function(tuple){
                                return tuple[1]=="" || tuple[0]==tuple[1];
                            });

                            return (filterValueStatus == null || filterValueStatus==status) && ( zipFilterValue.reduce(function(acc,equalityOfHierachy){
                                return acc && equalityOfHierachy;
                            }));
                      } });
        }

        function fillFilters(hierarchicalLevels){
                 var fillFilter = function(selectId,listOfHierarchies){
                     $('#'+selectId+'-filter').html(listOfHierarchies.map(function(hierarchy){
                                 return '<option value="'+hierarchy+'"> '+hierarchy+'</option>';
                     }).join(""));
                 };
                 var filterValuesHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).val() });
                console.log(filterValuesHierarchy[0],filterValuesHierarchy[1],filterValuesHierarchy[2],filterValuesHierarchy[3])
                 var caissesNames=objectToNames(hierarchicalLevels);

                 var indexOfCaisse = _.indexOf(caissesNames,filterValuesHierarchy[0]);
                 if(indexOfCaisse>0){
                     var groupesNames=objectToNames(hierarchicalLevels[indexOfCaisse].content);
                     var indexOfGroupe = _.indexOf(groupesNames,filterValuesHierarchy[1]);
                     if (indexOfGroupe>0){
                         var agencesNames=objectToNames(hierarchicalLevels[indexOfCaisse].content[indexOfGroupe].content);
                         var indexOfAgence = _.indexOf(agencesNames,filterValuesHierarchy[2]);
                         if (indexOfAgence>0){
                            var pdvsNames=objectToNames(hierarchicalLevels[indexOfCaisse].content[indexOfGroupe].content[indexOfAgence].content);
                            var indexOfPdv = _.indexOf(pdvsNames,filterValuesHierarchy[3]);
                            if (indexOfPdv>0){
                            }else{
                            fillFilter("pdvs",pdvsNames);
                            }

                         } else{
                            fillFilter("agences",agencesNames);
                            fillFilter("pdvs",[""]);
                         }

                     }
                     else {
                        fillFilter("groupes",groupesNames);
                        fillFilter("agences",[""]);
                        fillFilter("pdvs",[""]);
                     }

                 }else{
                    fillFilter("groupes",[""]);
                    fillFilter("agences",[""]);
                    fillFilter("pdvs",[""]);
                 }
               
        };
        function initialFillFilters(hierarchicalLevels){
            var fillFilter = function(selectId,listOfHierarchies){
                                 $('#'+selectId+'-filter').html(listOfHierarchies.map(function(hierarchy){
                                             return '<option value="'+hierarchy+'"> '+hierarchy+'</option>';
                                 }).join(""));
                             };
            var caissesNames=objectToNames(hierarchicalLevels);
            fillFilter("caisses",caissesNames);
            fillFilter("groupes",[""]);
            fillFilter("agences",[""]);
            fillFilter("pdvs",[""]);

        };

        $('.filter-button-group-status button').click( function() {
            if ($(this).hasClass('selected')){
                  $(this).removeClass('selected');
            } else {
                  $('.filter-button-group-status button').removeClass('selected');
                  $(this).addClass('selected');
            }
            filter();
        });

        $('.filter-button-group-hierarchies select').change( function() {

           fillFilters(hierarchicalLevels);
           filter();

        });
        initialFillFilters(hierarchicalLevels);

    };


    function objectToNames(listOfHierarchies){
        return listOfHierarchies.map(function(hierarchy){
             return hierarchy.name;
        });
    };

    function getHierarchicalLevels(rows){

        function rowToPdv(row){
            return {name:row.pdv,content:null};
        };
        function rowToAgence(row){
            return {name:row.agence,content:[{name:"",content:null},rowToPdv(row)]};
        };
        function rowToGroupe(row){
            return {name:row.groupe,content:[{name:"",content:null},rowToAgence(row)]};
        };
        function rowToCaisse(row){
            return {name:row.caisse,content:[{name:"",content:null},rowToGroupe(row)]};
        };


        var caisses =[{name:"",content:null}];
        rows.forEach(function(row){
            var caissesNames = objectToNames(caisses);
            var indexOfCaisse = _.indexOf(caissesNames,row.caisse);
            if (indexOfCaisse>=0){
                var groupes = caisses[indexOfCaisse].content;
                var groupesNames = objectToNames(groupes);
                            var indexOfGroupe = _.indexOf(groupesNames,row.groupe);
                            if (indexOfGroupe>=0){
                                var agences = caisses[indexOfCaisse].content[indexOfGroupe].content;
                                var agencesNames = objectToNames(agences);
                                                            var indexOfAgence = _.indexOf(agencesNames,row.agence);
                                                            if (indexOfAgence>=0){
                                                                     var pdvs = caisses[indexOfCaisse].content[indexOfGroupe].content[indexOfAgence].content;
                                                                     var pdvsNames = objectToNames(pdvs);
                                                                                                 var indexOfPdv = _.indexOf(pdvsNames,row.pdv);
                                                                                                 if (indexOfPdv>=0){

                                                                                                 } else {
                                                                                                     caisses[indexOfCaisse].content[indexOfGroupe].content[indexOfAgence].content.push(rowToAgence(row))
                                                                                                 }
                                                            } else {
                                                                caisses[indexOfCaisse].content[indexOfGroupe].content.push(rowToAgence(row))
                                                            }
                            } else {
                                caisses[indexOfCaisse].content.push(rowToGroupe(row))
                            }
            } else {
                caisses.push(rowToCaisse(row))
            }

        });
        return caisses
    };



    function grabRows() {
        $.ajax({
            type : 'GET',
            url : '/data/detected'

        }).then( function(data){
             // initialize the isotope plugin - will run only once
         var hierarchicalLevels = getHierarchicalLevels(data)
         showFilters(data,hierarchicalLevels);
         showRows(data);


        } );
    }
});