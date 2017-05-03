
$(document).ready(function() {
	initUNIQUENAME(null);
});

function initUNIQUENAME(addButtonElement) {
	var addButton;
	if (addButtonElement !== null) {
		addButton = $(addButtonElement).closest("div").find("#btnAddUNIQUENAME").filter(filterTemplatesElements).last();
	} else {
		addButton = $("#btnAddUNIQUENAME");
	}
	updateIndexesUNIQUENAME(addButton.closest("div").parent().closest("div"));
	addButton.each(function() {
		$(this).click(function() {
			var div = $(this).closest("div").parent().closest("div");
			var rows = div.find("div[row][current][name='VARIABLE']");
			var rowIndex = rows.length < 1 ? 0 : parseInt(rows.last().attr("row")) + 1;
			var copy = div.find("div[template]").first().clone();
			copy.children().each(function() {
				updateTemplateOnAddUNIQUENAME(this, rowIndex);
			});
			var e = "<div current row='" + rowIndex + "' name='VARIABLE' style='margin-bottom:4px;'>";
			e += copy.html();
			e += "</div>";
			addButton.before(e);
			updateIndexesUNIQUENAME(div);
			COMPONENT_JS_HANDLER
			$("#UNIQUENAME").trigger("onRowAdded", [rowIndex]);
		});
	});
}

function updateTemplateOnAddUNIQUENAME(element, rowIndex) {
	$(element).children().each(function() {
		updateTemplateOnAddUNIQUENAME(this, rowIndex);
	});
	$.each(element.attributes, function() {
		if(this.specified) {
			this.value = this.value.replace(/\{\}/, "[" + rowIndex + "]");
		}
	});
}

function removeUNIQUENAME(button) {
	var div = $(button).closest("div");
	var listDiv = div.parent().closest("div").parent().closest("div");
	var rowIndex = parseInt(div.attr("row"));
	$("#UNIQUENAME").trigger("onBeforeRowRemoved", [rowIndex]);
	div.find(".inputFileDelete").each(function() {
		$(this).click();
	});
	div.remove();
	updateIndexesUNIQUENAME(listDiv);
	$("#UNIQUENAME").trigger("onRowRemoved", [rowIndex]);
}

function removeAllUNIQUENAME() {
	$("#UNIQUENAME div[row]").each(function() {
		$(this).find(".inputFileDelete").each(function() {
			$(this).click();
		});
		$(this).remove();
	});
	$("input[name='VARIABLE.indexes']").val("");
	$("#UNIQUENAME").trigger("onAllRowsRemoved");
}

function updateIndexesUNIQUENAME(div) {
	var ids = [];
	div.find("div[row][current][name='VARIABLE']").filter(filterTemplatesElements).each(function() {
		ids.push($(this).attr("row")); 
	});
	var indexesInput = div.find("input[name$='.indexes']").filter(filterTemplatesElements).first();
	indexesInput.val(ids);
}
