// Fermeture
(function(){

	/**
	 * Fonction pour arrondir un nombre.
	 *
	 * @param	{String}	type	Le type d'arrondi.
	 * @param	{Number}	value	Le nombre à arrondir.
	 * @param	{Integer}	exp		L'exposant (le logarithme en base 10 de la base pour l'arrondi).
	 * @returns	{Number}			La valeur arrondie.
	 */
	function decimalAdjust(type, value, exp) {
		// Si l'exposant vaut undefined ou zero...
		if (typeof exp === 'undefined' || +exp === 0) {
			return Math[type](value);
		}
		value = +value;
		exp = +exp;
		// Si value n'est pas un nombre
        // ou si l'exposant n'est pas entier
		if (isNaN(value) || !(typeof exp === 'number' && exp % 1 === 0)) {
			return NaN;
		}
		// Décalage
		value = value.toString().split('e');
		value = Math[type](+(value[0] + 'e' + (value[1] ? (+value[1] - exp) : -exp)));
		// Re "calage"
		value = value.toString().split('e');
		return +(value[0] + 'e' + (value[1] ? (+value[1] + exp) : exp));
	}

	// Arrondi décimal
	if (!Math.round10) {
		Math.round10 = function(value, exp) {
			return decimalAdjust('round', value, exp);
		};
	}
	// Arrondi décimal inférieur
	if (!Math.floor10) {
		Math.floor10 = function(value, exp) {
			return decimalAdjust('floor', value, exp);
		};
	}
	// Arrondi décimal supérieur
	if (!Math.ceil10) {
		Math.ceil10 = function(value, exp) {
			return decimalAdjust('ceil', value, exp);
		};
	}

})();
