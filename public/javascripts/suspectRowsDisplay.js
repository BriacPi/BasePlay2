$(document).ready(function () {

    var $content = $('#content');

    grabRows();


    function showRows(rows){

        if (!rows) return;
        var items = rows.map(function(row){
            return $('<div class="tile '+row.caisse+'" data-agence="' + row.agence + '">'+
                         '<p class="status">'+row.status+'</p>'+
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
        $content.append('<div class="button-group filter-button-group">'+
                          '<button data-filter="*">Tous les etats</button>'+
                          '<button data-filter=".detectedOnly">Non affecte</button>'+
                          '<button data-filter=".solved">Resolu</button>'+
                          '<button data-filter=".beingProcessed">En cours</button>'+
                        '</div>');
        var filterFns = {
          // show if number is greater than 50
          detectedOnly: function() {
            var state = $(this).find('.status').text();
            return name.match( detectedOnly );
          },
          solved: function() {
            var state = $(this).find('.status').text();
            return name.match( solved );
          },
          beingProcessed: function() {
            var state = $(this).find('.status').text();
            return name.match( beingProcessed );
          }
        };

        $('.filter-button-group').on( 'click', 'button', function() {
          var filterValue = $(this).attr('data-filter');
          // use filter function if value matches
          filterValue = filterFns[ filterValue ] || filterValue;
          $content.isotope({ filter: filterValue });
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