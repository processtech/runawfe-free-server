var xmleditor = null;

function openDocumentEditor(url, saveUrl, saveButton, cancelButton) {
	$.editor = $('<div><textarea id="documentArea" style="width:100%; height:98%; display: none; "></textarea></div>').dialog( {
		modal:true, autoOpen: false, draggable: false, 
		overlay: {backgroundColor: '#000', opacity: 0.5},
	    close: function(event, ui) {
	    	destroyEditor();
		}  
	} );
		 
	$.editor.dialog("option", "height", 500);
	$.editor.dialog("option", "width", 700);
	var buttons = {};
	buttons[saveButton] = function() {
		saveDocument(saveUrl); 
		destroyEditor();
	};
	buttons[cancelButton] = function() {
		destroyEditor(); 
	};
	$.editor.dialog('option', 'buttons', buttons);
	$.editor.dialog('open');
	jQuery.ajax({
	    type: 'POST',
	    url: url,
	    data: '',
		dataType: 'html',
	    success: function(msg) { 
	    	$('#documentArea').val(msg); initEditor(); 
	    }
    }); 
}

function initEditor() {
	if(xmleditor == null) {
		xmleditor = CodeMirror.fromTextArea('documentArea', {
			content: $("#documentArea").val(),
		    parserfile: "parsexml.js",
		    stylesheet: "css/xmleditor.css",
		    path: "js/xmleditor/",
		    continuousScanning: 500,
		    autoMatchParens: true,
		    reindentOnLoad: true,
		    lineNumbers: false
		  });
	}
}

function saveDocument(saveUrl) {
	jQuery.ajax({
		type: 'POST',
		url: saveUrl,
		dataType: 'html',
		data: {conf : xmleditor.getCode() }
	});
}

function destroyEditor() {
	$.editor.dialog('close'); 
	$.editor.dialog('destroy').remove();
	xmleditor = null;
}