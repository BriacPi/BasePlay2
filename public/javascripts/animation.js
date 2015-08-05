// Tip: avoid this ton of code using AniJS ;)

var element = $('#square');

// when mouseover execute the animation
element.mouseover(function(){

  // the animation starts
  element.toggleClass('fadeOut animated');

  // do something when animation ends
  element.one('webkitAnimationEnd mozAnimationEnd MSAnimationEnd oanimationend animationend', function(e){

   // trick to execute the animation again
    $(e.target).removeClass('fadeOut animated');

  });

});

