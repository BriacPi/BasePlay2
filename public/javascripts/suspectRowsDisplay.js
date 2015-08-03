$(document).ready(function () {

    var $content = $('#content');

    grabRows();


    function showRows(rows){

        if (!rows) return;
        var items = rows.map(function(row){
            return $('<div class="tile '+row.caisse+'" data-status="' + row.status + '">'+
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

    };

    function grabRows() {
        $.ajax({
            type : 'GET',
            url : '/data/detected'

        }).then( function(data){
             // initialize the isotope plugin - will run only once

         showFilters(data);
         showRows(data);


        } );
    }
});