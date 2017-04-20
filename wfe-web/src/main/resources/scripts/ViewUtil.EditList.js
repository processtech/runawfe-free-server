
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
			console.log("Adding row " + rowIndex);
			var copy = div.find("div[template]").first().clone();
			copy.find("[name]").each(function(){
			    $(this).attr("name", $(this).attr("name").replace(/\{\}/, "[" + rowIndex + "]"));
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
	
function getSizeUNIQUENAME() {
	if ($("input[name='VARIABLE.indexes']").length > 0) {
		return parseInt($("input[name='VARIABLE.indexes']").val().split(',').length);
	} else {
		return 0;
	}
}

function removeUNIQUENAME(button) {
	var div = $(button).closest("div");
	var listDiv = div.parent().closest("div").parent().closest("div");
	var rowIndex = parseInt(div.attr("row"));
	console.log("Removing row ", rowIndex);
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
	console.log("Removed all rows");
}

function updateIndexesUNIQUENAME(div) {
	var ids = "";
	div.find("div[row][current][name='VARIABLE']").filter(filterTemplatesElements).each(function() {
		ids == "" ? ids = $(this).attr('row') : ids += "," + $(this).attr('row') ; 
	});
	var indexesInput = div.find("input[name$='.indexes']").filter(filterTemplatesElements).first();
	indexesInput.val(ids);
	console.log("List size = " + getSizeUNIQUENAME());
	
	div.find(".inputFormattedText").filter(filterTemplatesElements).trumbowyg({
	    lang: currentBrowserLanguage,
	    svgPath : 'css/trumbowyg.svg'
	})
	
}