var ie6compatibility = $.browser.msie && $.browser.version < 8;
var lastFileNumber = -1;
var filesCount = 0;

$(function() {	
	$(document).bind('drop dragover', function (e) {
		e.preventDefault();
	});
	$('.dropzone').each(function () {
		initFileInput($(this));
	});
	$(".dropzone").bind("dragleave dragend drop", function (e) {
		$(this).removeClass("dropzonehover");
	});
	$(".dropzone").bind('dragover', function (e) {
		$(this).addClass("dropzonehover");
	});
	if (ie6compatibility) {
		$(".inputFileContainer").css("width", "400px");
		$(".inputFileAjax").css({"width": "77px", "height": "26px", "cursor": "pointer", "filter": "alpha(opacity=50)"});
		$(".inputFileAttachButtonDiv").css("width", "170px");
		$(".inputFileAttach").css("cursor", "default");
	}
	$(document).delegate(".inputFileDelete", "click", function() {
		deleteFile($(this).attr("inputId"), $(this).attr("key"));
	});
});

function initFileInput(dropzone) {
	$(".inputFileContainer").attr("style", "height: 32px");
	var progressBar = dropzone.parent().find(".progressbar");
	var progressBarLine = progressBar.find(".line");
	var fileInput = dropzone.find(".inputFile");
	var inputId = fileInput.attr("name");
	
	jQuery.ajax({
		type: "GET",
		url: "/wfe/bulkUpload",
		data: {
			action: "view", 
	    	timestamp: new Date().getTime()
		},
		dataType: "html",
		success: function (e, msg, data) { 
			addInputRows(msg, jQuery.parseJSON(data.responseText), progressBar, progressBarLine, inputId);
		}
	});	
	
	dropzone.fileupload({
		dataType: "json",
		multiple: true,
		url: "/wfe/bulkUpload?fileKey=" + lastFileNumber,
		fileInput: fileInput,
		singleFileUploads: false,
		done: function (e, data) { 
			addInputRows(e, data.result, progressBar, progressBarLine, inputId);
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			progressBarLine.css("width", progress + "%");
		},
		dropZone: dropzone
	}).bind("fileuploadsubmit", function (e, data) {
		data.formData = {
			inputId: inputId
		};
		dropzone.hide();
		progressBar.show();
		$(".inputFileContainer").focus();
	}).bind('fileuploadfail', function (e, data) {
		var statusText = progressBar.find(".statusText");
		var statusImg = progressBar.find("img");
		statusImg.attr("src", "/wfe/images/error.gif");
		statusImg.addClass("inputFileDelete");
		statusText.html(data.textStatus);
		$(".inputFileContainer").focus();
	});
}

function deleteFile(inputId, key) {	
	jQuery.ajax({
		type: "GET",
		url: "/wfe/bulkUpload",
		data: {
			action: "delete", 
			key: key,
	    	timestamp: new Date().getTime()
		},
		dataType: "html",
		success: function(msg) {
			var dropzone = $("input[name='" + inputId + "']").parent().parent();
			var progressBar = dropzone.parent().find(".progressbar");
			var progressBarLine = progressBar.find(".line");
			progressBar.hide();			
			
			filesCount--;			
						
			if(filesCount == 0) {				
				dropzone.show();
				$("img[key='" + key + "']").parent().remove();
				progressBar.attr("style","height:26px; display: none;");
				progressBarLine.attr("style","width: 0%; height:0px;");
				$(".inputFileContainer").attr("style", "height: 32px");
			} else {
				var inputHeight = 26 * filesCount + 2;
				var progressBarHeight = 26 * filesCount;
				progressBar.attr("style","height:" + progressBarHeight + "px");
				progressBarLine.attr("style","height:" + progressBarHeight + "px");
				$(".inputFileContainer").attr("style", "height:" + inputHeight + "px");
				var offset = parseInt($("img[key='" + key + "']").parent().attr("offset"));
				$("img[key='" + key + "']").parent().remove();
				for (var i = 0; i <= lastFileNumber; i++) {
				    if($("img[key='" + i + "']").size() > 0 && parseInt($("img[key='" + i + "']").parent().attr("offset")) > offset) {
				    	var iOffset = parseInt($("img[key='" + i + "']").parent().attr("offset")) - 26;
				    	$("img[key='" + i + "']").parent().attr("style","position: absolute; top:" + iOffset + "px");
				    	$("img[key='" + i + "']").parent().attr("offset", iOffset);
				    }	
				}
			}
		}
	});
}

function addInputRows(msg, data, progressBar, progressBarLine, inputId) {	
	var statusText = progressBar.find(".statusText");
	var statusImg = progressBar.find("img");
	var maxKey = lastFileNumber;
	
	for(var i = 0; i < data.length; i++) {
		lastFileNumber++;
		var label = data[i].name + "<span style='color: #888'> - " + data[i].size + "</span>";
		
		if(lastFileNumber == 0) {
			$(".dropzone").hide();
			progressBar.show();
			progressBarLine.show();
			progressBarLine.attr("style","height:26px");
			progressBar.find(".status").attr("offset", 26 * filesCount);
			progressBar.append("<div class=\"firstUploadFile\"></div>");
			statusImg.attr("key", data[i].key);
			statusImg.attr("src", "/wfe/images/delete.png");
			statusImg.addClass("inputFileDelete");
			statusText.html(label);
		} else {
			var inputHeight = 26 * filesCount + 28;
			var progressBarHeight = 26 * filesCount + 26;
			$(".inputFileContainer").attr("style", "height:" + inputHeight + "px");
			progressBar.attr("style","height:" + progressBarHeight + "px");
			progressBarLine.attr("style","height:" + progressBarHeight + "px");
			var text = "<div style=\"position: absolute; top: " + 26 * filesCount + "px;\" class=\"status\" offset = " + 26 * filesCount + " >" +
					"<img src=\"" + "/wfe/images/delete.png" + "\" class=\"inputFileDelete\" inputId=\"" + inputId + "\" key=" + data[i].key + ">" +
					"<span class=\"statusText\">" + label + "</span></div>";
			statusText = progressBar.find(".firstUploadFile");
			statusText.append(text);
		}	
		filesCount++;	
		if(parseInt(data[i].key) > parseInt(maxKey)) {
			maxKey = parseInt(data[i].key);
		}
	}	
	if(parseInt(maxKey) > parseInt(lastFileNumber)) {
		lastFileNumber = parseInt(maxKey);
	}
}
