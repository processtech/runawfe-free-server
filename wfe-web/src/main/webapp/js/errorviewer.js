$(document).ready(function() {
	var buttons = {};
	buttons[buttonSupportMessage] = function() {
		showSupportFiles();
	};
	buttons[buttonCloseMessage] = function() {
		$.errorDetailsDialog.dialog("close");
	};
	$.errorDetailsDialog = $("<div style=\"padding: 10px;\" id=\"errorDetailsDiv\"></div>").dialog( {
		modal: true, 
		autoOpen: false, 
		height: 500,
		width: 700,
		buttons: buttons,  
		overlay: {
			backgroundColor: "#000", opacity: 0.5
		}  
	});
});

function showSystemError(message) {
	$.ajax({
		dataType: "json",
	    url: "/wfe/error_details.do",
	    data: {
	    	action: "getSystemError", 
	    	name: message
	    },
	    success: function(data) {
			$("#errorDetailsDiv").html("<pre>"+data.html+"</pre>");
			$.errorDetailsDialog.dialog("open");
			$(".ui-button:contains('" + buttonSupportMessage + "')").hide();
	    }
    });
}

function deleteSystemError(element, message) {
	$.ajax({
		dataType: "json",
	    url: "/wfe/error_details.do",
	    data: {
	    	action: "deleteSystemError", 
	    	name: message
	    },
	    success: function(data) {
			$(element).closest("tr").hide();
	    }
    });
}

function showTokenErrorStackTrace(tokenId, processId) {
	$.ajax({
		dataType: "json",
		url: "/wfe/error_details.do",
		data: {
			action: "getTokenErrorStackTrace",
			tokenId: tokenId,
			processId: processId
		},
		success: function(data) {
			$("#errorDetailsDiv").html("<pre>" + data.html + "</pre>");
			$.errorDetailsDialog.dialog("open");
			$(".ui-button:contains('" + buttonSupportMessage + "')").show();
		}
	});
}

function showSupportFiles() {
	var url = "/wfe/error_details.do?action=showSupportFiles&" + $("#supportForm").serialize();
	$("#errorDetailsDiv").html("<br/><br/><br/>&nbsp;&nbsp;&nbsp;<img src='/wfe/images/loading.gif' align='absmiddle' /> " + loadingMessage);
	$.errorDetailsDialog.dialog("open");
	$(".ui-button:contains('" + buttonSupportMessage + "')").hide();
    $.getJSON(
		url,
		function(data) {
			if (data.tabs.length > 1) {
				$("#errorDetailsDiv").html("<div id='tabs'><ul id='tabHeaders'></ul></div>");
	  			$.each(data.tabs, function(i, tab) {
	  				$("#tabHeaders").append("<li><a href='#tab" + tab.key + "'>" + tab.title + "</a></li>");
					$("#tabs").append("<div id='tab" + tab.key + "'></div>");
					displayFiles(tab, $("#tab" + tab.key));
				});
				$("#tabs").tabs();
			} else {
				$("#errorDetailsDiv").html("");
	  			displayFiles(data.tabs[0], $("#errorDetailsDiv"));
			}
			displayFiles(data, $("#errorDetailsDiv"));
			if (data.downloadUrl) {
				$("#errorDetailsDiv").append("<br /><br /><a href='" + data.downloadUrl + "' style='text-decoration: underline;'>" + data.downloadTitle + "</a>");
			}
			if (data.supportUrl) {
				$("#errorDetailsDiv").append("<br /><br /><a href='" + data.supportUrl + "' style='text-decoration: underline;' target='_blank'>" + data.supportTitle + "</a>");
			}
		}
	);
}

function displayFiles(tab, element) {
	if (typeof tab !== "undefined" && typeof tab.files !== "undefined") {
		$.each(tab.files, function(i, file) {
			var fileInfo = "<input type='checkbox' disabled='true'";
			if (file.included) {
				fileInfo += " checked='true'";
			}
			fileInfo += ">" + file.name + "<br />";
			element.append(fileInfo);
		});
	}
}
