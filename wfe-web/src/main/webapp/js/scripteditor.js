var editDialogContent = "<div><input id=\"fileName\" style=\"width: 100%\" class=\"required\"><br><textarea id=\"documentArea\" style=\"width: 100%; height: 90%; display: none; border: 1px solid black !important;\"></textarea></div>";
var uploadDialogContent = "<div><form id=\"uploadForm\" method=\"post\" action='/wfe/admin_scripts.do' enctype='multipart/form-data'><input type=\"hidden\" name=\"action\"><input name=\"fileName\" style=\"width: 100%\"><br><input name=\"uploadFile\" type=\"file\" style=\"width: 100%\"></form></div>";
var xmleditor = null;

$(document).ready(function() {
	$.uploader = $(uploadDialogContent).dialog( {
		modal: true, 
		autoOpen: false, 
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		}  
	});
});

function editScript(fileName, saveButtonText, executeButtonText, cancelButtonText) {
	$.editor = $(editDialogContent).dialog( {
		modal: true, 
		autoOpen: false, 
		height: 500,
		width: 700,
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		},
	    close: function(event, ui) {
	    	destroyEditor();
		}  
	});
	$("#fileName").val(fileName);
	var buttons = {};
	buttons[saveButtonText] = function() {
		saveScript(); 
	};
	buttons[executeButtonText] = function() {
		executeScript(); 
	};
	buttons[cancelButtonText] = function() {
		destroyEditor();
	};
	$.editor.dialog("option", "buttons", buttons);
	$.editor.dialog("open");
	if (fileName.length > 0) {
		jQuery.ajax({
		    type: "POST",
		    url: "/wfe/admin_scripts.do",
		    data: {
		    	ajax: "true", 
		    	action: "get", 
		    	fileName: fileName
		    },
		    dataType: "html",
		    success: function(msg) {
		    	initEditor(msg); 
		    }
	    });
    } else {
    	initEditor(""); 
    }
}

function initEditor(msg) {
   	$("#documentArea").val(msg);
	xmleditor = CodeMirror.fromTextArea("documentArea", {
		content: msg,
	    parserfile: "parsexml.js",
	    stylesheet: "css/xmleditor.css",
	    path: "js/xmleditor/",
	    continuousScanning: 500,
	    autoMatchParens: true,
	    reindentOnLoad: true,
	    lineNumbers: false
	});
}

function destroyEditor() {
	$.editor.dialog("destroy").remove();
	xmleditor = null;
}

function saveScript() {
	jQuery.ajax( {
		type: "POST",
		url: "/wfe/admin_scripts.do",
		data: {
	    	ajax: "true", 
			action: "save", 
			fileName: $("#fileName").val(), 
			script: xmleditor.getCode() 
		},
		dataType: "html",
		success: function(msg) {
			if (msg == "") {
				setStatusMessage(saveSuccessMessage);
			} else {
				setStatusMessage(msg);
			}
			destroyEditor();
		}
	});
}

function setStatusMessage(html) {
	setTimeout(function() {
		$("#ajaxErrorsDiv").html(html);
	}, 700);
}

function executeScript() {
	jQuery.ajax( {
		type: "POST",
		url: "/wfe/admin_scripts.do",
		data: {
	    	ajax: "true", 
			action: "execute", 
			script: xmleditor.getCode() 
		},
		dataType: "html",
		success: function(msg) {
			if (msg == "0") {
				setStatusMessage(executionSuccessMessage);
			} else {
				setStatusMessage(executionFailedMessage);
			}
		}
	});
	destroyEditor();
}

function uploadScript(saveButtonText, executeButtonText, cancelButtonText) {
	$("#fileName").val("");
	$.uploader.dialog("option", "height", 200);
	$.uploader.dialog("option", "width", 400);
	var buttons = {};
	buttons[saveButtonText] = function() {
		$("input[name='action']").val("save");
		$(this).dialog("close");
		$("#uploadForm").submit();
	};
	buttons[executeButtonText] = function() {
		$("input[name='action']").val("execute");
		$(this).dialog("close");
		$("#uploadForm").submit();
	};
	buttons[cancelButtonText] = function() {
		$(this).dialog("close");
	};
	$.uploader.dialog("option", "buttons", buttons);
	$.uploader.dialog("open");
}
