$(document).ready(function() {
	initTooltipOnArea();
	$(document).delegate(".multiInstanceBox", "mouseover", function() {
		var attr = $(this).attr("bgcolor");
		if (typeof attr === "undefined" || attr !== false) {
			$(this).attr("bgcolor", $(this).css("background-color"));
		}
		$(this).css("background-color", "gray");
	});
	$(document).delegate(".multiInstanceBox", "mouseout", function() {
		$(this).css("background-color", $(this).attr("bgcolor"));
	});
	$("#showAllProcessDefinitionChanges").click(function(e) {
		$(this).hide();
		e.preventDefault();
		$("#processDefinitionChanges").find("tr:gt(0)").remove();
		$.ajax({
			type: "GET",
			url: $(this).attr("href"),
			dataType: "html",
			success: function (data) {
				$("#processDefinitionChanges").append(data);
			}
		});
	});
});

function initTooltipOnArea() {
	$("area").tooltip({ 
		track: true,
		content: function () {
			var re = /<error>([^]*?)<\/error>/g;
			var str = $(this).prop("title");
			var escaped = str.replace(re, function(match, p1) {
				return "<b class='fontRed'>" + $("<div />").text(p1).html() + "</b>";
			});
			return escaped;
		}
	});
}

function showEmbeddedSubprocessDefinition(definitionId, subprocessId, width, height) {
	var jsId = getJsessionidValue();
	var src;
	if (jsId) {
		src = "/wfe/definition_graph_component.do;jsessionid=" + jsId + "?id=" + definitionId + "&subprocessId=" + subprocessId;
	} else {
		src = "/wfe/definition_graph_component.do?id=" + definitionId + "&subprocessId=" + subprocessId;
	}
	showImageDialog(src, width, height);
}

function showEmbeddedSubprocess(processId, subprocessId, width, height) {
	var jsId = getJsessionidValue();
	var src;
	if (jsId) {
		src = "/wfe/process_graph_component.do;jsessionid=" + jsId + "?id=" + processId + "&subprocessId=" + subprocessId;
	} else {
		src = "/wfe/process_graph_component.do?id=" + processId + "&subprocessId=" + subprocessId;
	}
	showImageDialog(src, width, height);
}

function showEmbeddedSubprocessGraphHistory(processId, subprocessId, width, height) {
	var jsId = getJsessionidValue();
	var src;
	if (jsId) {
		src = "/wfe/process_graph_component_history.do;jsessionid=" + jsId + "?id=" + processId + "&subprocessId=" + subprocessId;
	} else {
		src = "/wfe/process_graph_component_history.do?id=" + processId + "&subprocessId=" + subprocessId;
	}
	showImageDialog(src, width, height);
}

var graphDialogCounter = 0;
function showImageDialog(src, w, h) {
	// TODO auto-update with history
	graphDialogCounter++;
	var graphDialogDivId = "graphDialog" + graphDialogCounter;
	$.graphDialog = $("<div id='" + graphDialogDivId + "'></div>").dialog({
    	autoOpen: false,
    	modal: true,
    	position: [100, 100],  
    	width: w + 40,
    	height: h + 70,
    	close: function(event, ui) {
	    	$("#" + graphDialogDivId).html("");
		}
    });
   	$("#" + graphDialogDivId).html("<br/><br/>&nbsp;&nbsp;&nbsp;<img src='/wfe/images/loading.gif' align='absmiddle' />");
	$.graphDialog.dialog("open");
	$("#" + graphDialogDivId).load(unify(src), function() {
		initTooltipOnArea();
	});
}

function getJsessionidValue() {
	var jsession = document.location.href.match(/jsessionid=(.*?)\?id/i);
	if (jsession) {
		return jsession[1];
	}
	return null;
}
