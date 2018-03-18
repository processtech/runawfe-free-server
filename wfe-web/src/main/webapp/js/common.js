
if (!window.console) {
	console = {log: function() {}};
};

const SYSTEM_MENU_VISIBLE = "system_menu_visible";

$(document).ready(function() {
	// http://jqueryui.com/tooltip/	
	$(document).tooltip({ 
		track: true
	});
	initComponents($(document));
	// confirmation dialog
	$.confirmDialog = $("<div></div>").dialog({
		minWidth: 400, minHeight: 200, modal: true, autoOpen: false
	});
	$("#hierarchyTypeSelect").change(function(){
		if ($(this).val() == "_default_type_") {
			$("#newHierarchyTypeName").removeAttr("disabled");
			$("#newHierarchyTypeName").focus();
		} else {
			$("#newHierarchyTypeName").attr("disabled", "true");
		}
	});
	$(".selectionStatusPropagator").change(propagateSelectionStatus);
	setSystemMenuVisible(getLocalStorageValue(SYSTEM_MENU_VISIBLE, $(window).width() > 1000), false);
	$("#showSystemMenu").click(function() {
		setSystemMenuVisible(true, true);
	});
	$("#hideSystemMenu").click(function() {
		setSystemMenuVisible(false, true);
	});
});

function initComponents(container) {
	// http://trentrichardson.com/examples/timepicker/
	container.find(".inputTime").filter(filterTemplatesElements).timepicker({ ampm: false, seconds: false });
	// http://docs.jquery.com/UI/Datepicker
	container.find(".inputDate").filter(filterTemplatesElements).datepicker({ dateFormat: "dd.mm.yy", buttonImage: "/wfe/images/calendar.gif" });
	container.find(".inputDateTime").filter(filterTemplatesElements).datetimepicker({ dateFormat: "dd.mm.yy" });
	container.find(".editList").filter(filterTemplatesElements).each(function () {
		$(this).editList();
	});
}

function filterTemplatesElements() {
	return $(this).parents('[template]').length < 1;
}

// add timestamp to ajax queries
function unify(url) {
	if (url.indexOf("?") != -1) {
		return url + "&t="+(new Date().getTime());
	}
	return url + "?t="+(new Date().getTime());
}

function escapeQuotesForHtmlContext(s) {
	s = s.replace('"', '\\&quot;');
	s = s.replace("'", '\\&quot;');
	return s;
}

function openConfirmPopup(element, cookieName, message, confirmMessage, cancelButton, okButton) {
	if($.cookie(cookieName) == "true") {
		if(element.href == null) {
			var parent = element.parentNode;
			while(parent.tagName != "FORM") {
				parent = parent.parentNode; 
			}
			parent.submit();
		} else { 
			window.location = element.href; 
		}
	} else {
		$.confirmDialog.html("<p style=\"font-size: 8pt; font-style: italic;\"><input id=\"cookieCh\" type=\"checkbox\" value=\"\"> " + confirmMessage + "</p><p>" + message + "</p>"); 
		
		var buttons = {};
		buttons[okButton] = function() {
			if($("#cookieCh").is(":checked")) { 
				$.cookie(cookieName, "true");
			}
			var parent = element.parentNode;
			while(parent.tagName != "FORM") {
				parent = parent.parentNode; 
			}
			if (element.href == null) { 
				parent.submit(); 
			} else { 
				window.location = element.href; 
			}
		}
		buttons[cancelButton] = function() {
			$(this).dialog("close");
		};
		$.confirmDialog.dialog("option", "buttons", buttons);
		$.confirmDialog.dialog("option", "position", "center");
		$.confirmDialog.dialog("open");
	}
}

function openSubstitutionCriteriasConfirmPopup(message, allMethod, allButton, onlyMethod, onlyButton, cancelButton) {
	$.confirmDialog.html("<p>" + message + "</p>");
	var form = $("#substitutionCriteriasForm");
	var buttons = {};
	buttons[onlyButton] = function() {
		$("input[name='removeMethod']").val(onlyMethod);
		form.submit();
	};
	buttons[allButton] = function() {
		$("input[name='removeMethod']").val(allMethod);
		form.submit();
	};
	buttons[cancelButton] = function() {
		$(this).dialog("close");
	};
	$.confirmDialog.dialog("option", "buttons", buttons);
	$.confirmDialog.dialog("open");
}

function viewBlock(blockId) {
	var controlId = blockId + "Controls";
	$("#"+controlId).toggle();
	var visabilityState = $("#"+controlId).is(':visible') ? "visible" : "hidden";
	$("#"+controlId+"Img").attr("src", "/wfe/images/view_setup_" + visabilityState +  ".gif");
	jQuery.ajax({
	    type: "POST",
	    url: "/wfe/hideableBlock.do",
	    data: { name: blockId }
    });
}

function showFiltersHelp() {
	$("#filtersHelpDialog").dialog({
		dialogClass: "no-close",
		buttons: [{
			text: buttonCloseMessage,
			click: function() {
				$(this).dialog("close");
			}
		}]
	});
}

function propagateSelectionStatus() {
	var table = $(this).closest("table");
	var checked = $(this).prop("checked");
	table.find("tr td:first-child").find("input[type='checkbox']:enabled").each(function() {
		$(this).prop("checked", checked);
	});
}

function getLocalStorageValue(key, defaultValue) {
	try {
		var value = localStorage.getItem(key);
		if (value != null) {
			return JSON.parse(value);
		}
		return defaultValue;
	} catch(e) {
		console.log("Unable to get value from localStorage by key " + key + ": " + e);
	}
}

function setLocalStorageValue(key, value) {
	try {
		localStorage.setItem(key, JSON.stringify(value));
	} catch(e) {
		console.log("Unable to set [" + key + ", " + value + "] to localStorage: " + e);
	}
}

function setSystemMenuVisible(visible, update) {
	if (visible) {
		$(".systemMenu").show();
		$("#showSystemMenu").hide();
	} else {
		$(".systemMenu").hide();
		$("#showSystemMenu").show();
	}
	if (update) {
		setLocalStorageValue(SYSTEM_MENU_VISIBLE, visible);
	}
}
