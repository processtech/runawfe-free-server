(function($) {
	var methods = {
		init: function(settings) { 
			settings = $.extend({}, $.fn.treeview.defaults, settings);
			if (!settings.command) {
				$.error("'command' parameter is required and should point to valid server command");
			}
			var tree = $(this);
			tree.addClass("treeview");
			$(this).delegate(".hitarea", "click", function() {
				var node = $(this).parent();
				var childrenContainer = node.find(">.children");
				if (node.hasClass("collapsed")) {
					node.removeClass("collapsed");
					if (childrenContainer.length > 0) {
						childrenContainer.show();
						node.addClass("expanded");
					} else {
						node.addClass("loading");
						loadChildren(settings, node, childrenContainer);
					}
				} else {
					node.removeClass("expanded");
					childrenContainer.hide();
					node.addClass("collapsed");
				}
			});
			$(this).delegate(".selectable", "click", function() {
				if (settings.multipleSelection) {
					$(this).toggleClass("selected");
				} else {
					tree.find(".selected").removeClass("selected");
					$(this).addClass("selected");
				}
				settings.onSelectionChanged.call(this);
			});
		},
		refresh: function(settings) {
			settings = $.extend({}, $.fn.treeview.defaults, settings);
			$(this).addClass("loading");
			$(this).html("");
			loadChildren(settings, $(this), $(this));
		}
	};
	$.fn.treeview = function(method) {
		if (methods[method]) {
			return methods[method].apply( this, Array.prototype.slice.call(arguments, 1));
		} else if (typeof method === 'object' || !method) {
			return methods.init.apply(this, arguments);
		} else {
			$.error("Method " +  method + " not found in jQuery.treeview namespace");
		}
	};
	$.fn.treeview.defaults = {
		multipleSelection: false,
		onSelectionChanged : function() {}
	};
	function loadChildren(settings, parent, childrenContainer) {
		var url = "/wfe/ajaxcmd?command=" + settings.command;
		var parentId = "";
		if (parent.attr("id") && !parent.hasClass("root")) {
			parentId = parent.attr("id");
		}
		$.getJSON(url, {parentId: parentId}, function(response) {
			addChildren(parent, response, childrenContainer);
		});
	}
	function addChildren(parent, children, childrenContainer) {
		if (childrenContainer == null || childrenContainer.length == 0) {
			childrenContainer = $("<div class='children'></div>");
			parent.append(childrenContainer);
		}
		$.each(children, addChild, [childrenContainer]);
		parent.addClass("expanded");
		parent.removeClass("loading");
	}
	function addChild(childrenContainer) {
		var child = $("<div/>").addClass("node").attr("id", this.id || "");
		if (this.hasChildren && this.id) {
			child.addClass("collapsed");
			$("<div/>").addClass("hitarea").appendTo(child);
		}
		if (this.last) {
			child.addClass("last");
		}
		var label = $("<span />");
		if (this.selectable) {
			label.addClass("selectable");
		}
		if (this.selected) {
			label.addClass("selected");
		}
		if (this.disabled) {
			label.addClass("disabled");
		}
		label.html(this.label);
		label.appendTo(child);
		child.appendTo(childrenContainer);
		if (this.children) {
			$("<div/>").addClass("hitarea").appendTo(child);
			addChildren(child, this.children);
		}
	}
})(jQuery);
