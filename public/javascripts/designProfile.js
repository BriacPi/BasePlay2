(function(){
            var menu_trigger = $("[data-card-menu]");
            var back_trigger = $("[data-card-back]");

            menu_trigger.click(function(){
                $(".card, body").toggleClass("show-menu");
            });

            back_trigger.click(function(){
                $(".card, body").toggleClass("show-menu");
            });
        })();
        $(window).resize(function(){

/*
        	$('.center').css({
        		position:'absolute',
        		left: ($(window).width() - $('.center').outerWidth())/2,
        		top: ($(window).height() - $('.center').outerHeight())/10
        	});
*/
        });

        // To initially run the function:
        $(window).resize();