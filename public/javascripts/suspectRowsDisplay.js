$(document).ready(function () {

    var $content = $('#content');
    var params = document.body.getElementsByTagName('script');
    query = params[0].classList;
    var whereToGetData = query[0];
    grabRows();


    function showRows(rows){

        if (!rows) return;
        var items = rows.map(function(row){

            return $('<div class="tile not-separator tile--width-1 tile--height-1'
                    +'" data-status="' + row.status
                    +'" data-caisse="' + row.caisse
                    +'" data-groupe="' + row.groupe
                    +'" data-agence="' + row.agence
                    +'" data-pdv="' + row.pdv
                    +'" data-date="' + row.date
                    +'" data-metric="' + row.metricName
                    + '">'

                    +'<div class="tile__content">'
                    +   ' <a class="black" style="background-color :white; padding :20px" href="/row/'+row.id+'">'
                    +       '<div class="tile__content__title">'
                    +           '<span class="tile__content__title__icon"><i class="fa fa-calendar"></i></span>'
                    +           '<h5 class="tile__content__title__text" style="color :#00C5A2" >'+row.date+'</h5> '
                    +        '</div>'
                    +        '<div class="tile__content__data tile__content__data--numeric">'
                    +           '<div class="tile__content__data__metrics">'
                    +               '<h3 class="tile__content__data__metrics__value" style ="font-size :24px; color: #676767">'+row.value+'</h3>'
                    +               '<div class="tile__content__data__metrics__comparison">'
                    +                   '<div class="tile__content__data__metrics__comparison__icon" style ="font-size:14px; color:#006699">'+row.metricName+'</div>'
                    +               '</div>'
                    +            '</div>'
                    +        '</div>'
                    +   '</a>'
                    +'</div>'
                    +'</div>'
                     );


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
                              isEmpty: '.empty-separator',
                              isNamed: function(elem){
                                if($(elem).hasClass('named-separator')){return 1;} else {return 0;}
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
                    isEmpty:true,
                    isNamed:false
                  }
        });
        $content.isotope({sortBy: [ 'date' ]
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

        function  namedSeparatorsForGroupBy(valuesToGroupBy,dimensionToGroupBy){
            return valuesToGroupBy.map(function(value){
                return $('<div class="tile named-separator'
                                    +'" data-'+dimensionToGroupBy+'="'+value
                                    + '">'
                                    +'<h2>'+value+'</h2>'
                                    +'</div>');
            });
        };


        function addSeparators(emptySeparators,namedSeparators){
            emptySeparators.forEach(function(sep){
                $content.append( sep )
                                 .isotope( 'appended', sep );
            })
            namedSeparators.forEach(function(sep){
                $content.append( sep )
                                 .isotope( 'appended', sep );
            })

        };

        function removeEmptySeparators(){
            $content.isotope( 'remove', $('.empty-separator') );
            $content.isotope( 'remove', $('.named-separator') );
        };




        function groupBy(dimensionToGroupBy){
                    removeEmptySeparators();
                    $content.isotope('updateSortData').isotope();
                    var elems = $content.data('isotope').filteredItems;
                    var valuesToGroupBy = getValuesToGroupBy(elems,dimensionToGroupBy);
                    var emptySeparators = emptySeparatorsForGroupBy(valuesToGroupBy,dimensionToGroupBy);
                    var namedSeparators = namedSeparatorsForGroupBy(valuesToGroupBy,dimensionToGroupBy);
                     addSeparators(emptySeparators,namedSeparators);
                     $content.isotope({
                       sortBy: [ 'occurence',dimensionToGroupBy, 'isEmpty','isNamed','date' ]
                     });
        };

        $('.groupby-button-group button').click( function() {
                    if ($(this).hasClass('selected')){
                          $(this).removeClass('selected').removeClass('active');
                          removeEmptySeparators();
                          $content.isotope('layout');
                    } else {
                          $('.groupby-button-group button').removeClass('selected').removeClass('active');
                          $(this).addClass('selected').addClass('active');
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
                    if ($('#sort-by-status-button').hasClass('selected')){
                        var dimensionToGroupBy = $('.groupby-button-group button.selected').data('groupby');
                        groupBy(dimensionToGroupBy);
                    }
        });

        $('.filter-button-group-metric select').change( function() {
                    if ($('#sort-by-hierarchy-button').hasClass('selected')){
                        console.log('2a')
                          var dimensionToGroupBy = $('.groupby-button-group button.selected').data('groupby');
                          console.log('2b')
                          groupBy(dimensionToGroupBy);
                          console.log('2c')
                    }
                    if ($('#sort-by-status-button').hasClass('selected')){
                        var dimensionToGroupBy = $('.groupby-button-group button.selected').data('groupby');
                        groupBy(dimensionToGroupBy);
                    }
        });
        $('.selectpicker').selectpicker('refresh');
    };

    function showFilters(hierarchicalLevels,rows){

        var filterFns = {
          status: function(wantedStatus,currentStatus) {
             return currentStatus== wantedStatus
          }
        };

        function filter(){
            var filterValueMetric = $('.filter-button-group-metric select').val();
            var filterValuesHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).val() })

            var namesOfFilterForHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).data('filter') });
            $content.isotope({ filter: function(){
                            var self = this;
                            var isSeparator= ($(self).hasClass('empty-separator') || $(self).hasClass('named-separator') )
                            var metric = $(self).data('metric');
                            var hierarchies = namesOfFilterForHierarchy.map(function(index, name){
                                return $(self).data(name);
                            });
                            var zipFilterValue = _.zip(hierarchies,filterValuesHierarchy).map(function(tuple){
                                return tuple[1]=="" || tuple[0]==tuple[1] ;
                            });

                            return isSeparator || (filterValueMetric == ""|| filterValueMetric==metric) && ( zipFilterValue.reduce(function(acc,equalityOfHierachy){
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
           $('.selectpicker').selectpicker('refresh');

        };
        function fillMetricFilters(rows){
            var metrics=[];
            rows.forEach(function(row){
                metrics.push(row.metricName);
            });
            var listOfMetrics= _.unique(metrics).sort();

            var filters=[];
            filters.push('<option value=""> Tout </option>')
            listOfMetrics.forEach(function(metric){
                filters.push('<option value="'+metric+'"> '+metric+'</option>');
            });

            $('#metric-filter').html(filters)
            $('.selectpicker').selectpicker('refresh');
        }

        function fillHierarchyFilters(hierarchicalLevels){
                 var fillFilter = function(selectId,listOfHierarchies){
                     var filters=[];
                     filters.push('<option value=""> Tout </option>')
                     listOfHierarchies.forEach(function(hierarchy){
                            filters.push('<option value="'+hierarchy+'"> '+hierarchy+'</option>');
                     });
                     $('#'+selectId+'-filter').html(filters);
                 };
                 var filterValuesHierarchy = $('.filter-button-group-hierarchies select').map(function(){ return $(this).val() });

                 var caissesNames=objectToNames(hierarchicalLevels);

                 var indexOfCaisse = _.indexOf(caissesNames,filterValuesHierarchy[0]);
                 if(indexOfCaisse>=0){
                     var groupesNames=objectToNames(hierarchicalLevels[indexOfCaisse].content);
                     var indexOfGroupe = _.indexOf(groupesNames,filterValuesHierarchy[1]);
                     if (indexOfGroupe>=0){
                         var agencesNames=objectToNames(hierarchicalLevels[indexOfCaisse].content[indexOfGroupe].content);
                         var indexOfAgence = _.indexOf(agencesNames,filterValuesHierarchy[2]);
                         if (indexOfAgence>=0){
                            var pdvsNames=objectToNames(hierarchicalLevels[indexOfCaisse].content[indexOfGroupe].content[indexOfAgence].content);
                            var indexOfPdv = _.indexOf(pdvsNames,filterValuesHierarchy[3]);
                            if (indexOfPdv>=0){
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
               $('.selectpicker').selectpicker('refresh');
        };
        function initialFillHierarchyFilters(hierarchicalLevels){
             var fillFilter = function(selectId,listOfHierarchies){
                                 var filters=[];
                                 filters.push('<option value=""> Tout </option>')
                                 listOfHierarchies.forEach(function(hierarchy){
                                        filters.push('<option value="'+hierarchy+'"> '+hierarchy+'</option>');
                                 });
                                 $('#'+selectId+'-filter').html(filters);
                             };
            var caissesNames=objectToNames(hierarchicalLevels);
            fillFilter("caisses",caissesNames);
            fillFilter("groupes",[""]);
            fillFilter("agences",[""]);
            fillFilter("pdvs",[""]);
            $('.selectpicker').selectpicker('refresh');
        };

       fillMetricFilters(rows);


       $('.filter-button-group-metric select').change( function() {

            filter();
        });

        $('.filter-button-group-hierarchies select').change( function() {
            console.log('1a')
           fillHierarchyFilters(hierarchicalLevels);
           console.log('1b')
           filter();
           console.log('1c')

        });
        initialFillHierarchyFilters(hierarchicalLevels);

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
            return {name:row.agence,content:[rowToPdv(row)]};
        };
        function rowToGroupe(row){
            return {name:row.groupe,content:[rowToAgence(row)]};
        };
        function rowToCaisse(row){
            return {name:row.caisse,content:[rowToGroupe(row)]};
        };


        var caisses =[];
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
            url : '/data/'+whereToGetData

        }).then( function(data){
             // initialize the isotope plugin - will run only once
         var hierarchicalLevels = getHierarchicalLevels(data)
         showFilters(hierarchicalLevels,data);
         showGroupBy(data);
         showRows(data);


        } );
    }
});