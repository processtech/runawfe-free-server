
if (!window.console) {
	console = {log: function() {}};
};

var SYSTEM_MENU_VISIBLE = "system_menu_visible";

$(document).ready(function() {
	// http://jqueryui.com/tooltip/	
	$(document).tooltip({ 
		track: true
	});
	initComponents($(document));
	
	$(".paging-div").width($(window).width() - $(".systemMenu").width() - 30);
	
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

function openDeletePopup(element, cookieName, message, deleteMessage, cancelButton, okButton) {
	if($.cookie(cookieName) == "true") {
		if(element.href == null) {
			var parent = element.parentNode;
			while(parent.tagName != "FORM") {
				parent = parent.parentNode;
			}
			parent.remove();
		} else {
			window.location = element.href;
		}
	} else {
		$.confirmDialog.html("<p style=\"font-size: 8pt; font-style: italic;\"><input id=\"cookieCh\" type=\"checkbox\" value=\"\"> " + deleteMessage + "</p><p>" + message + "</p>");

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
				parent.remove();
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

function restoreDefaultSettingValue(settingName, fileName) {
	jQuery.ajax({
		type: "POST",
		url: "/wfe/restore_setting.do",
		data: { settingName: settingName, fileName: fileName },
		success: function () {
			window.location.href = "/wfe/manage_settings.do";
		}
	});
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

// Filter criteria specifics

var editListFilterCriteriaDialogContent = "<div><textarea id=\"listFilterCriteriaId\" style=\"width: 95%; height: 90%;\"/></div>";

function editListFilterCriteria(inputTextId, titleText, saveButtonText, cancelButtonText) {
	$.editor = $(editListFilterCriteriaDialogContent).dialog( {
		modal: true,
		autoOpen: false,
		title: titleText,
		height: 300,
		width: 400,
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		},
	    close: function(event, ui) {
			destroyFilterCriteriaEditor();
		}
	});
	var inputText = $("#" + inputTextId).val().trim();
	try {
		var obj = JSON.parse(inputText);
		inputText = "";
		if (obj instanceof Array) {
			for (var i = 0; i < obj.length; i++) {
				var item = obj[i].trim();
				if (item.length > 0) {
					inputText += item + "\n";
				}
			}
		}
	} catch(e) {
		inputText = "";
	}
	$("#listFilterCriteriaId").val(inputText);
	var buttons = {};
	buttons[saveButtonText] = function() {
		saveListFilterCriteria(inputTextId);
	};
	buttons[cancelButtonText] = function() {
		destroyFilterCriteriaEditor();
	};
	$.editor.dialog("option", "buttons", buttons);
	$.editor.dialog("open");
}

function saveListFilterCriteria(inputTextId) {
	try {
		var items = $("#listFilterCriteriaId").val().trim().split("\n");
		for (var i = 0; i < items.length; i++) {
			if (items[i].length == 0) {
				items.splice(i--, 1);
			}
		}
		$("#" + inputTextId).val(items.length > 0 ? JSON.stringify(items) : "");
	} catch(e) {
		alert(e);
	}
	destroyFilterCriteriaEditor();
}

function destroyFilterCriteriaEditor() {
	$.editor.dialog("destroy").remove();
}

var editRangeFilterCriteriaDialogContent = "<div><input type=\"text\" id=\"rangeMinFilterCriteriaId\" style=\"width: 47%;\"/>&nbsp;-&nbsp;<input type=\"text\" id=\"rangeMaxFilterCriteriaId\" style=\"width: 47%;\"/></div>";

function editRangeFilterCriteria(inputTextId, titleText, saveButtonText, cancelButtonText) {
	$.editor = $(editRangeFilterCriteriaDialogContent).dialog( {
		modal: true,
		autoOpen: false,
		title: titleText,
		height: 140,
		width: 400,
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		},
	    close: function(event, ui) {
			destroyFilterCriteriaEditor();
		}
	});
	var inputText = $("#" + inputTextId).val().trim();
	var minValue = "";
	var maxValue = "";
	try {
		var obj = JSON.parse(inputText);
		if (obj instanceof Object) {
			if (obj.min) {
				minValue = obj.min.trim();
			}
			maxValue = obj.max.trim();
		}
	} catch(e) {
		// do nothing
	}
	$("#rangeMinFilterCriteriaId").val(minValue);
	$("#rangeMaxFilterCriteriaId").val(maxValue);
	var buttons = {};
	buttons[saveButtonText] = function() {
		saveRangeFilterCriteria(inputTextId);
	};
	buttons[cancelButtonText] = function() {
		destroyFilterCriteriaEditor();
	};
	$.editor.dialog("option", "buttons", buttons);
	$.editor.dialog("open");
}

function saveRangeFilterCriteria(inputTextId) {
	try {
		var range = {};
		var minValue = $("#rangeMinFilterCriteriaId").val().trim();
		if (minValue.length > 0) {
			range.min = minValue;
		}
		var maxValue = $("#rangeMaxFilterCriteriaId").val().trim();
		if (maxValue.length > 0) {
			range.max = maxValue;
		}
		$("#" + inputTextId).val(Object.getOwnPropertyNames(range).length > 0 ? JSON.stringify(range) : "");
	} catch(e) {
		alert(e);
	}
	destroyFilterCriteriaEditor();
}

var selectUserFilterCriteriaDialogContent = "<div><input id='selectUserFilter' /><div id='selectUserSearch'></div></div>";

function selectUser(nameInputId, labelInputId, cancelButtonText) {
	$.editor = $(selectUserFilterCriteriaDialogContent).dialog({
		modal: true,
		autoOpen: false,
		height: 400,
		width: 400,
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		},
	    close: function(event, ui) {
			destroyFilterCriteriaEditor();
		}
	});
	$("#selectUserFilter").on("change keyup", function() {
		$.ajax({
			type: "POST",
			url: "/wfe/ajaxcmd?command=ajaxActorsList",
			data: JSON.stringify({ target: "actor", hint: $(this).val(), page: 0, perPage: 10 }),
			dataType: "json",
			contentType:"application/json; charset=UTF-8",
			processData: false,
			success: function(data) {
				var $selectUserSearchDiv = $("#selectUserSearch");
				$selectUserSearchDiv.empty();
				$.each(data.data, function(k, v) {
					var $link = $("<a>", { "href": "javascript: void(0);", "style": "display: block; padding: 1px;", "text": v.fullname });
					$link.click(function() {
						$("#" + nameInputId).val(v.name);
						$("#" + labelInputId).val(v.fullname);
						destroyFilterCriteriaEditor();
					});
					$selectUserSearchDiv.append($link);
				});
			}
		});
	}).change();
	var buttons = {};
	buttons[cancelButtonText] = function() {
		destroyFilterCriteriaEditor();
	};
	$.editor.dialog("option", "buttons", buttons);
	$.editor.dialog("open");
}
