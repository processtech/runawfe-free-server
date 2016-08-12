
if (!window.console) {
	console = {log: function() {}};
};

$(document).ready(function() {
	// http://jqueryui.com/tooltip/	
	$(document).tooltip({ 
		track: true
	});
	
	// http://trentrichardson.com/examples/timepicker/
	$(".inputTime").timepicker({ ampm: false, seconds: false });
	// http://docs.jquery.com/UI/Datepicker
	$(".inputDate").datepicker({ dateFormat: "dd.mm.yy", buttonImage: "/wfe/images/calendar.gif" });
	$(".inputDateTime").datetimepicker({ dateFormat: "dd.mm.yy" });
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
});

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
		$.confirmDialog.html("<p>" + message + "</p><p><input id=\"cookieCh\" type=\"checkbox\" value=\"\"> " + confirmMessage + "</p>"); 
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

function createListElementRegExp(s) {
	s = s.replace(/\[/g, '\\[');
	s = s.replace(/\]/g, '\\]');
	return new RegExp(s + '\\[\\]','g');		
}

function createDoubleSubLineRegExp() {
	return new RegExp('__','g');		
}
