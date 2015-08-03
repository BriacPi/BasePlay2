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
                    + '">'+
                         '<h3>' + row.agence + '</h3><br>' + row.date +
                     '</div>');
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

        $('.filter-button-group-status button').click( function() {
          var filterValue = $(this).data('filter');
          $content.isotope({ filter: function(){
                var status = $(this).data('status');
                return filterValue==status;
          } });
        });

        $('.filter-button-group-hierarchies select').change( function() {
            var wantedLevel = $(this).val();
            var nameOfFilter = $(this).data('filter');
            $content.isotope({ filter: function(){
               var level = $(this).data(nameOfFilter);
               return level==wantedLevel;
            } });
        });

    };

    function getHierarchicalLevels(rows){
        var caisses =[],
            groupes=[],
            agences = [],
            pdvs=[];
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