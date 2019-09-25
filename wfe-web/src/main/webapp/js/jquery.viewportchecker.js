
(function($){
	$.fn.viewportChecker = function(useroptions){
			// Define options and extend with user
			var options = {
					classToAdd: 'visible',
					classToAddForFullView : 'full-visible',
					removeClassAfterAnimation: false,
					offset: 100,
					repeat: false,
					invertBottomOffset: true,
					callbackFunction: function(elem, action){},
					scrollHorizontal: false,
					scrollBox: window
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
							viewportStart = Math.max(
									$('html').scrollTop(),
									$('body').scrollTop(),
									$(window).scrollTop()
							);
							viewportEnd = (viewportStart + boxSize.height);
					}
					else{
							viewportStart = Math.max(
									$('html').scrollLeft(),
									$('body').scrollLeft(),
									$(window).scrollLeft()
							);
							viewportEnd = (viewportStart + boxSize.width);
					}

					// Loop through all given dom elements
					$elem.each(function(){
							var $obj = $(this),
									objOptions = {},
									attrOptions = {};

							//  Get any individual attribution data
							if ($obj.data('vp-add-class'))
									attrOptions.classToAdd = $obj.data('vp-add-class');
							if ($obj.data('vp-remove-class'))
									attrOptions.classToRemove = $obj.data('vp-remove-class');
							if ($obj.data('vp-add-class-full-view'))
									attrOptions.classToAddForFullView = $obj.data('vp-add-class-full-view');
							if ($obj.data('vp-keep-add-class'))
									attrOptions.removeClassAfterAnimation = $obj.data('vp-remove-after-animation');
							if ($obj.data('vp-offset'))
									attrOptions.offset = $obj.data('vp-offset');
							if ($obj.data('vp-repeat'))
									attrOptions.repeat = $obj.data('vp-repeat');
							if ($obj.data('vp-scrollHorizontal'))
									attrOptions.scrollHorizontal = $obj.data('vp-scrollHorizontal');
							if ($obj.data('vp-invertBottomOffset'))
									attrOptions.scrollHorizontal = $obj.data('vp-invertBottomOffset');

							// Extend objOptions with data attributes and default options
							$.extend(objOptions, options);
							$.extend(objOptions, attrOptions);

							// If class already exists; quit
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

									// Check if full element is in view
									if (rawEnd <= viewportEnd && rawStart >= viewportStart)
											$obj.addClass(objOptions.classToAddForFullView);
								

									// Set element as already animated
								

							// Remove class if not in viewport and repeat is true
							} else if ($obj.hasClass(objOptions.classToAdd) && (objOptions.repeat)){
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