var ie6compatibility = $.browser.msie && $.browser.version < 8;

$(function () {
	$(document).bind("drop dragover", function (e) {
		e.preventDefault();
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
	$(document).delegate(".inputFileDelete", "click", function () {
		deleteFile($(this).attr("inputId"));
	});
});

var commonInitComponents = initComponents;

initComponents = function(container) {
	container.find(".dropzone").filter(filterTemplatesElements).each(function () {
		initFileInput($(this));
	});
	container.find(".tabs").tabs();
	if ($.fn.trumbowyg) {
		container.find(".inputFormattedText").filter(filterTemplatesElements).trumbowyg({
			lang: currentBrowserLanguage,
			svgPath: "css/trumbowyg.svg"
		});
	}
	$(".js-select-executor").focus(function() {
		var $element = $(this);
		$(this).autocomplete({
			delay: 300,
			minLength: 0,
			source: function(request, response) {
				$.ajax({
					type: "GET",
					cache: false,
					url: "/wfe/ajaxcmd?command=ajaxExecutorsList",
					data: {
						type: $element.attr("js-executor-type"),
						includingTemporaryGroups: $element.attr("js-including-temporary-groups"),
						hint: request.term
					},
					dataType: "json",
					success: function (result) {
						response(result.data);
					}
				});
			},
			select: function(event, ui) {
				$element.val(ui.item.label);
				$("[name='" + $element.attr("js-label-for") + "']").val(ui.item.value);
				return false;
			}
		});
		$(this).autocomplete("search", $(this).val());
	});
	$(".js-select-executor").change(function() {
		if ($(this).val() === "") {
			$("[name='" + $(this).attr("js-label-for") + "']").val("");
		}
	});
	return commonInitComponents(container);
}

function initFileInput(dropzone) {
	var container = dropzone.parent();
	var progressBar = container.find(".progressbar");
	var progressBarLine = progressBar.find(".line");
	var fileInput = dropzone.find(".inputFile");
	var inputId = fileInput.attr("name");
	var inputVariable = container.closest(".inputVariable");
	inputVariable = inputVariable.length == 0 ? container.closest("#variableInput") : inputVariable;

	dropzone.fileupload({
		dataType: "json",
		url: "/wfe/upload?id=" + id + "&key=" + getMaxKey(inputVariable) + "&inputId=" + inputId,
		fileInput: fileInput,
		singleFileUploads: false,
		done: function (e, data) {
			addInputRows(e, data.result, inputVariable);
		},
		progressall: function (e, data) {
			var progress = parseInt(data.loaded / data.total * 100, 10);
			progressBarLine.css("width", progress + "%");
		},
		dropZone: dropzone
	}).bind("fileuploadsubmit", function (e, data) {
		data.formData = {
			inputId: inputId,
		};
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

function deleteFile(inputId) {
	var dropzone = $("input[name='" + inputId + "']").parent().parent();
	dropzone.show();
	jQuery.ajax({
		type: "GET",
		url: "/wfe/upload",
		data: {
			action: "delete",
			id: id,
			inputId: inputId,
			timestamp: new Date().getTime()
		},
		dataType: "html",
		success: function (msg) {
			dropzone.parent().parent().find(".remove").click();
			dropzone.parent().find(".progressbar").hide();
		}
	});
}

function setFocusOnInvalidInputIfAny() {
	var invalidInputs = $("[invalid='true']");
	if (invalidInputs.length > 0) {
		var firstInvalidInput = invalidInputs.get(0);
		var parentTabPanels = $(firstInvalidInput).parents("div[role='tabpanel']");
		if (parentTabPanels.length > 0) {
			$("a[href='#" + parentTabPanels.get(0).id + "']").click();
		}
		firstInvalidInput.focus();
	}
}

function addInputRows(msg, data, inputVariable) {
	var addElement = inputVariable.find('.add');

	for (var i = 0; i < data.length; i++) {
		if (i != 0) {
			addElement.click();
		}
		var label = data[i].name + "<span style='color: #888'> - " + data[i].size + "</span>";
		var row = inputVariable.find('div[row=\"' + data[i].key + '\"]');
		var progressBar;
		if(row.length == 0){
			progressBar =  inputVariable.find(".progressbar");
			inputVariable.find(".dropzone").hide();
		} else {
			progressBar = row.find(".progressbar");
			row.find(".dropzone").hide();
		}
		var statusText = progressBar.find(".statusText");
		var statusImg = progressBar.find("img");
		var progressBarLine = progressBar.find(".line");
		progressBarLine.attr("style", "height:26px");
		progressBar.show();
		statusImg.attr("key", data[i].key);
		statusImg.attr("src", "/wfe/images/delete.png");
		statusImg.addClass("inputFileDelete");
		statusText.html("<a href='/wfe/upload?action=view&inputId=" + statusImg.attr("inputId") + "&id=" + id + "'>" + label + "</a>");
	}
}

function getMaxKey(inputVariable) {
	var divs = inputVariable.find('div[row]');
	var maxKey = 0;
	divs.each(function() {
		var div = $(this);
		var key = div.attr("row");
		if(parseInt(maxKey) < parseInt(key)) {
			maxKey = parseInt(key);
		}
	});
	return maxKey;
}
