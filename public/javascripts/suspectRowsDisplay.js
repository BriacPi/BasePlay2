$(document).ready(function () {

    var $content = $('#content');

    grabRows();


    function showRows(rows){

        if (!rows) return;
        var items = rows.map(function(row){

            return $('<div class="tile"><a href="/row/'+row.id+'">'+row.caisse + row.agence + row.pdv+'</a></div>');

        });

        $content.append( items );
        $content.isotope({
                itemSelector: '.tile',
                layoutMode: 'fitRows'
        });

    };

    function showFilters(rows){

        var filterFns = {
          status: function(wantedStatus,currentStatus) {
             return currentStatus== wantedStatus
          }
        };

        var filter = function(){
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
           filter();
        });

    };

    function getHierarchicalLevels(rows){
        var caisses =[""],
            groupes=[""],
            agences = [""],
            pdvs=[""];
        rows.forEach(function(row){
            caisses.push(row.caisse);
            groupes.push(row.groupe);
            agences.push(row.agence);
            pdvs.push(row.pdv);
        });
        return {caisses:_.uniq(caisses).sort(),groupes:_.uniq(groupes).sort(),agences:_.uniq(agences).sort(),pdvs:_.uniq(pdvs).sort()}
    };

    function fillFilters(rows){
        var hierarchicalLevels = getHierarchicalLevels(rows);
        var fillFilter = function(selectId,listOfHierarchies){
            $('#'+selectId+'-filter').html(listOfHierarchies.map(function(hierarchy){
                        return '<option value="'+hierarchy+'"> '+hierarchy+'</option>';
            }).join(""));
        };
        fillFilter("caisses",hierarchicalLevels.caisses);
        fillFilter("groupes",hierarchicalLevels.groupes);
        fillFilter("agences",hierarchicalLevels.agences);
        fillFilter("pdvs",hierarchicalLevels.pdvs);

    };

    function grabRows() {
        $.ajax({
            type : 'GET',
            url : '/data/detected'

        }).then( function(data){
             // initialize the isotope plugin - will run only once

         showFilters(data);
         fillFilters(data);
         showRows(data);


        } );
    }
});