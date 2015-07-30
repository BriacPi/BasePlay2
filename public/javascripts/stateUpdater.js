$(function(){
    var setState =function(){
                  $.getJSON("/analysis_state").then(function(data){
                      if(data.color.indexOf("red")>-1) {
                          $( '#state' ).html( "<FONT COLOR=\"red\" >" + data.niceMessage + "</FONT>");
                      }
                      else {
                          $( '#state' ).html( data.niceMessage );
                      }
                  })
              }
    setState()
    setInterval(setState, 10000);
})
