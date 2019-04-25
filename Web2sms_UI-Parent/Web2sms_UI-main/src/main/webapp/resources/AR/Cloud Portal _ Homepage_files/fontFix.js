window.onload = function(){
	if (!$.support.leadingWhitespace) {	// equal to false in IE 6-8).
		Cufon.replace("h1");
		Cufon.now();
	}
};