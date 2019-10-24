
(function($){
	$.fn.viewportChecker = function(useroptions){
			// Define options and extend with user
			var options = {
					classToAdd: 'visible',
					classToAddForFullView : 'full-visible',
					removeClassAfterAnimation: false,
					offset: 0,//100,
					repeat: false,
					invertBottomOffset: true,
					callbackFunction: function(elem, action){},
					scrollHorizontal: false,
					scrollBox: "#modal-body"
			};
			$.extend(options, useroptions);
			// Cache the given element and height of the browser
			var $elem = this,
					boxSize = {height: $(options.scrollBox).height(), width: $(options.scrollBox).width()};
			/*
			 * Main method that checks the elements and adds or removes the class(es)
			 */
			this.checkElements = function(){
					var viewportStart, viewportEnd;
					// Set some vars to check with
					if (!options.scrollHorizontal){
						viewportStart =$('.modal-body').scrollTop();
						viewportEnd = (viewportStart + boxSize.height);
					}
					// Loop through all given dom elements
					$elem.each(function(){
							var $obj = $(this),
									objOptions = {};
							// Extend objOptions with data attributes and default options
							//??
							$.extend(objOptions, options);
							// If class already exists; quit
							//???
							if ($obj.data('vp-animated') && !objOptions.repeat){
									return;
							}
							
							// Check if the offset is percentage based
							if (String(objOptions.offset).indexOf("%") > 0)
									objOptions.offset = (parseInt(objOptions.offset) / 100) * boxSize.height;
							// Get the raw start and end positions
							var rawStart = (!objOptions.scrollHorizontal) ? $obj.offset().top : $obj.offset().left,
									rawEnd = (!objOptions.scrollHorizontal) ? rawStart + $obj.height() : rawStart + $obj.width();
							// Add the defined offset
							var elemStart = Math.round( rawStart ) + objOptions.offset,
									elemEnd = (!objOptions.scrollHorizontal) ? elemStart + $obj.height() : elemStart + $obj.width();
							if (objOptions.invertBottomOffset)
									elemEnd -= (objOptions.offset * 2);
							// Add class if in viewport
							if ((elemStart < viewportEnd) && (elemEnd > viewportStart)){
									// Do the callback function. Callback wil send the jQuery object as parameter
									objOptions.callbackFunction($obj, "add");
									$(options.scrollBox).unbind("load scroll", this.checkElements);
									if( 'ontouchstart' in window || 'onmsgesturechange' in window ){
										// Device with touchscreen
										$(document).unbind("touchmove MSPointerMove pointermove", this.checkElements);
									}
									// Check if full element is in view
									if (rawEnd <= viewportEnd && rawStart >= viewportStart)
											$obj.addClass(objOptions.classToAddForFullView);
							// Remove class if not in viewport and repeat is true
							}
					});

			};

			// Select the correct events
			if( 'ontouchstart' in window || 'onmsgesturechange' in window ){
					// Device with touchscreen
					$(document).bind("touchmove MSPointerMove pointermove", this.checkElements);
			}
			// Always load on window load
			$(options.scrollBox).bind("load scroll", this.checkElements);
			// On resize change the height var
			$(window).resize(function(e){
					boxSize = {height: $(options.scrollBox).height(), width: $(options.scrollBox).width()};
					$elem.checkElements();
			});
			// trigger inital check if elements already visible
			this.checkElements();
			// Default jquery plugin behaviour
			return this;
	};
})(jQuery);