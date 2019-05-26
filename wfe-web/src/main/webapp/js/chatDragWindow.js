$(document).ready(function() {
	 //var sliderElem = document.getElementById('slider');
	 //var thumbElem = sliderElem.children[0];
	  var windowChat = document.getElementsByClassName('modal-content');
	  var footer = document.getElementById('modal-header-dragg');
	  //var ball = document.getElementById('ball');

	  footer.onmousedown = function(e) {

	    var coords = getCoords(windowChat);
	    var shiftX = e.pageX - coords.left;
	    var shiftY = e.pageY - coords.top;

	    windowChat[0].style.position = "absolute";
	    //document.body.appendChild(windowChat);
	    moveAt(e);

	    windowChat[0].style.zIndex = 1000; // над другими элементами

	    function moveAt(e) {
	    	windowChat[0].style.left = e.pageX - shiftX + 'px';
	    	windowChat[0].style.top = e.pageY - shiftY + 'px';
	    }

	    document.onmousemove = function(e) {
	      moveAt(e);
	    };

	    footer.onmouseup = function() {
	      document.onmousemove = null;
	      footer.onmouseup = null;
	    };
	    document.onmouseup = function() {
		      document.onmousemove = null;
		      footer.onmouseup = null;
		    };

	  }

	  footer.ondragstart = function() {
	    return false;
	  };

	  function getCoords(elem) {   // кроме IE8-
	    var box = elem[0].getBoundingClientRect();
	    return {
	      top: box.top + pageYOffset,
	      left: box.left + pageXOffset
	    };
	  }
	
})