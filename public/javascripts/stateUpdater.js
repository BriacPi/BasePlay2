$(function(){
    setInterval(function(){
        $.getJSON("/analysis_state").then(function(data){$( '#state' ).html(data.niceMessage);})
    }, 10000);
})
