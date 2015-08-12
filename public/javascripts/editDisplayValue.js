            $(document).ready(function() {
                if (navigator.language == 'fr'){var language ='fr'} else {var language ='en'}
	            var value =  ($('#value').text()).split(',');
	            console.log(value);
	            var formattedValue = sprintf(language)(value[0],value[1]);
	            $('#value').html(formattedValue);


            } );