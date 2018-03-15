(function($) {
	var methods = {
		init: function(settings) { 
			settings = $.extend({}, $.fn.editList.defaults, settings);
			var container = $(this);
			if (container.length == 0) {
				return;
			}

			updateIndexes(container);
			container.find(".add[name='add_" + container.attr("id") + "']").click(function(event) {
				event.stopPropagation();
				var rows = container.find("div[row]");
				var rowIndex = rows.length < 1 ? 0 : parseInt(rows.last().attr("row")) + 1;
				var copy = container.find("div[template]").first().clone();
				copy.children().each(function() {
					updateAfterTemplateCopy(this, rowIndex);
				});
				var rowElementHtml = "<div row='" + rowIndex + "' style='margin-bottom:4px;'>";
				rowElementHtml += copy.html();
				rowElementHtml += "</div>";
				var rowElement = $(rowElementHtml);
				$(this).before(rowElement);
				updateIndexes(container);
				initComponents(rowElement);
				container.trigger("onRowAdded", [rowIndex]);
				settings.onRowAdded.call(this, [rowIndex]);
			});

			container.delegate(".remove[name='remove_" + container.attr("id") + "']", "click", function(event) {
				event.stopPropagation();
				var rowElement = $(this).closest("div[row]");
				var rowIndex = parseInt(rowElement.attr("row"));
				container.trigger("onBeforeRowRemoved", [rowIndex]);
				settings.onBeforeRowRemoved.call(this, [rowIndex]);
				rowElement.find(".inputFileDelete").each(function() {
					$(this).click();
				});
				rowElement.remove();
				updateIndexes(container);
				container.trigger("onRowRemoved", [rowIndex]);
				settings.onRowRemoved.call(this, [rowIndex]);
			});

		}
	};
	$.fn.editList = function(method) {
		if (methods[method]) {
			return methods[method].apply( this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error("Method " +  method + " not found in jQuery.editList namespace");
		}
	};
	$.fn.editList.defaults = {
		onRowAdded : function() {},
		onBeforeRowRemoved : function() {},
		onRowRemoved : function() {}
	};
	
	function updateAfterTemplateCopy(element, rowIndex) {
		$(element).children().each(function() {
			updateAfterTemplateCopy(this, rowIndex);
		});
		$.each(element.attributes, function() {
			if (this.specified) {
				this.value = this.value.replace(/\{\}/, "[" + rowIndex + "]");
			}
		});
	}
	
	function updateIndexes(container) {
		var ids = [];
		var divs = container;
		while (divs && divs.length && ids.length <= 0) {
			divs.each(function() {
				$(this).children("div[row]").filter(filterTemplatesElements).each(function() {
					ids.push($(this).attr("row"));
				});
			});
			if (ids.length <= 0) {
				divs = divs.children("div");
			}
		}
		var indexesInput = container.find("input[name$='.indexes']").filter(filterTemplatesElements).first();
		indexesInput.val(ids);
	}

})(jQuery);
