$(document).ready(function () {

    var $content = $('#content');

    grabRows();


    function showRows(rows){

        if (!rows) return;
        var items = rows.map(function(row){

            return $('<div class="tile'
                    +'" data-status="' + row.status
                    +'" data-caisse="' + row.caisse
                    +'" data-groupe="' + row.groupe
                    +'" data-agence="' + row.agence
                    +'" data-pdv="' + row.pdv
                    +'" data-date="' + row.date
                    +'" data-metric="' + row.metricName
                    + '">'
                    +'<br>' + row.date
                    +'<br>' + row.metricName
                    +'<br>' + row.value

                    +
                     '</div>');

        });

        $content.append( items );
        $content.isotope({
                itemSelector: '.tile',
                layoutMode: 'fitRows',
                getSortData: {
                              caisse: '[data-caisse]',
                              groupe: '[data-groupe]',
                              agence: '[data-agence]',
                              pdv: '[data-pdv]',
                              metric: '[data-metric]',
                              date: '[data-date]',
                              status: '[data-status]',
                              occurence: function(itemElement){
                                return getOccurence(itemElement,rows);
                                },
                              isEmpty: function(itemElement){
                                if ($(itemElement).hasClass('empty-separator')){return 1;}
                                else {return 0;}
                              }
                              },
                sortAscending: {
                    caisse: true,
                    groupe: true,
                    agence: true,
                    pdv: true,
                    metric: true,
                    date: false,
                    status: true,
                    occurence: false,
                    isEmpty:false
                  }
        });

    };

    function getOccurence(item,rows){
        var dimensionToGroupBy = $('.groupby-button-group button.selected').data('groupby');
        var valueOfDimension = $(item).data(dimensionToGroupBy)
        var occurence = 0
        rows.forEach(function(row){
            if(row[dimensionToGroupBy]==valueOfDimension){occurence ++;}
        });
        return occurence;
    };


    function showGroupBy(rows){
        function getValuesToGroupBy(elems,dimensionToGroupBy){
                    var valuesToGroupBy = elems.map(function(elem){
                        return $(elem.element).data(dimensionToGroupBy);
                    });
                    return _.unique(valuesToGroupBy);
        };


        function  emptySeparatorsForGroupBy(valuesToGroupBy,dimensionToGroupBy){
            return valuesToGroupBy.map(function(value){
                return $('<div class="tile empty-separator'
                                    +'" data-'+dimensionToGroupBy+'="'+value
                                    + '">'
                                    +'</div>');
            });
        };
        function addEmptySeparators(emptySeparators){
        emptySeparators.forEach(function(sep){
            $content.append( sep )
                             .isotope( 'appended', sep );
        })

        };

        function removeEmptySeparators(){
            $content.isotope( 'remove', $('.empty-separator') );
        };




        function groupBy(dimensionToGroupBy){
                    removeEmptySeparators();
                    $content.isotope('updateSortData').isotope();
                    var elems = $content.data('isotope').filteredItems;
                    var valuesToGroupBy = getValuesToGroupBy(elems,dimensionToGroupBy);
                    var emptySeparators = emptySeparatorsForGroupBy(valuesToGroupBy,dimensionToGroupBy);
                     addEmptySeparators(emptySeparators);
                     $content.isotope({
                       sortBy: [ 'occurence',dimensionToGroupBy, 'isEmpty','date' ]
                     });
        };

        $('.groupby-button-group button').click( function() {
                    if ($(this).hasClass('selected')){
                          $(this).removeClass('selected');
                          removeEmptySeparators();
                          $content.isotope('layout');
                    } else {
                          $('.groupby-button-group button').removeClass('selected');
                          $(this).addClass('selected');
                          var dimensionToGroupBy = $('.groupby-button-group button.selected').data('groupby');
                          groupBy(dimensionToGroupBy);
                    }


        });

        $('.filter-button-group-hierarchies select').change( function() {
                    if ($('#sort-by-hierarchy-button').hasClass('selected')){
                        console.log('2a')
                          var dimensionToGroupBy = $('.groupby-button-group button.selected').data('groupby');
                          console.log('2b')
                          groupBy(dimensionToGroupBy);
                          console.log('2c')
                    }



        });

    };

    function showFilters(hierarchicalLevels){

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
                            var isEmptySeparator=$(self).hasClass('empty-separator');
                            var status = $(self).data('status');
                            var hierarchies = namesOfFilterForHierarchy.map(function(index, name){
                                return $(self).data(name);
                            });
                            var zipFilterValue = _.zip(hierarchies,filterValuesHierarchy).map(function(tuple){
                                return tuple[1]=="" || tuple[0]==tuple[1] ;
                            });

                            return isEmptySeparator || (filterValueStatus == null|| filterValueStatus==status) && ( zipFilterValue.reduce(function(acc,equalityOfHierachy){
                                return acc && equalityOfHierachy;
                            }));
                      } });
        }

        function changeGroupByHierarchyButton(newHierarchy){
            function capitalizeFirstLetter(string) {
            return string.charAt(0).toUpperCase() + string.slice(1);
             };
           $('#sort-by-hierarchy-button').data('groupby',newHierarchy);
           $('#sort-by-hierarchy-button').html(capitalizeFirstLetter(newHierarchy));

        };


        function fillFilters(hierarchicalLevels){
                 var fillFilter = function(selectId,listOfHierarchies){
                     $('#'+selectId+'-filter').html(listOfHierarchies.map(function(hierarchy){
                                 return '<option value="'+hierarchy+'"> '+hierarchy+'</option>';
                     }).join(""));
                 };
                 var filterValuesHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).val() });

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
                            changeGroupByHierarchyButton("pdv");
                            fillFilter("pdvs",pdvsNames);
                            }

                         } else{
                            changeGroupByHierarchyButton("agence");
                            fillFilter("agences",agencesNames);
                            fillFilter("pdvs",[""]);
                         }

                     }
                     else {
                        changeGroupByHierarchyButton("groupe");
                        fillFilter("groupes",groupesNames);
                        fillFilter("agences",[""]);
                        fillFilter("pdvs",[""]);
                     }

                 }else{
                    changeGroupByHierarchyButton("caisse");
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

       /* $('.filter-button-group-status button').click( function() {
            if ($(this).hasClass('selected')){
                  $(this).removeClass('selected');
            } else {
                  $('.filter-button-group-status button').removeClass('selected');
                  $(this).addClass('selected');
            }
            filter();
        }); */

        $('.filter-button-group-hierarchies select').change( function() {
            console.log('1a')
           fillFilters(hierarchicalLevels);
           console.log('1b')
           filter();
           console.log('1c')

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
         showFilters(hierarchicalLevels);
         showGroupBy(data);
         showRows(data);


        } );
    }
});