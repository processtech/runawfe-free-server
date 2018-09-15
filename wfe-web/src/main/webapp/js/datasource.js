var editDialogContent = "<div><input type=\"hidden\" id=\"dataSourceId\"/><input type=\"password\" id=\"dataSourcePassword\" style=\"width: 95%;\" class=\"required\"/></div>";

function newDataSourcePassword(dataSourceId, titleText, saveButtonText, cancelButtonText) {
	$.editor = $(editDialogContent).dialog( {
		modal: true, 
		autoOpen: false, 
		title: titleText,
		height: 140,
		width: 250,
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		},
	    close: function(event, ui) {
	    	destroyPasswordEditor();
		}  
	});
	$("#dataSourceId").val(dataSourceId);
	var buttons = {};
	buttons[saveButtonText] = function() {
		savePassword(); 
	};
	buttons[cancelButtonText] = function() {
		destroyPasswordEditor();
	};
	$.editor.dialog("option", "buttons", buttons);
	$.editor.dialog("open");
}

function savePassword() {
	jQuery.ajax( {
		type: "POST",
		url: "/wfe/change_data_source_password.do",
		data: {
	    	ajax: "true", 
			action: "save", 
			dataSourceId: $("#dataSourceId").val(),
			dataSourcePassword: $("#dataSourcePassword").val()
		},
		dataType: "html",
		success: function(msg) {
			if (msg == "") {
				setStatusMessage(savePasswordSuccessMessage);
			} else {
				setStatusMessage(msg);
			}
			destroyPasswordEditor();
		}
	});
}

function destroyPasswordEditor() {
	$.editor.dialog("destroy").remove();
}

function setStatusMessage(html) {
	setTimeout(function() {
		$("#ajaxErrorsDiv").html(html);
	}, 700);
}

var serverVersionDialogContent = "<div id=\"serverVersionDiv\"></div>";

function getServerVersion(dataSourceId, titleText) {
	jQuery.ajax( {
		type: "GET",
		url: "/wfe/data_source_server_version.do",
		data: {
			ajax: "true",
			dataSourceId: dataSourceId
		},
		dataType: "html",
		success: function(msg) {
			$.editor = $(serverVersionDialogContent).dialog( {
				modal: true,
				autoOpen: false,
				title: titleText,
				height: 180,
				width: 300,
				overlay: {
					backgroundColor: "#000", opacity: 0.5
				},
				close: function(event, ui) {
					destroyPasswordEditor();
				}
			});
			$("#serverVersionDiv").html(msg);
			var buttons = {};
			buttons["Ok"] = function() {
				destroyPasswordEditor();
			};
			$.editor.dialog("option", "buttons", buttons);
			$.editor.dialog("open");
		}
	});
}
