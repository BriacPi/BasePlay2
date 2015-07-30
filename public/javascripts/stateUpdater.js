$(function(){
    setInterval(function(){
        $.getJSON("/analysis_state").then(function(data){
            if(false) {$( '#state' ).html( "<a >"+ data.niceMessage + "</a>");
            }
            else {$( '#state' ).html( "<a class=errorRed>"+ data.niceMessage + "</a>");
            }
                                                        })
                             }, 10000);
})
