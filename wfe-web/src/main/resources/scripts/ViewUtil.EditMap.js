
var componentInputUNIQUENAMEKey = "COMPONENT_INPUT_KEY";
var componentInputUNIQUENAMEValue = "COMPONENT_INPUT_VALUE";
var lastIndexUNIQUENAME = -1;

$(document).ready(function() {
	updateIndexesUNIQUENAME();
	lastIndexUNIQUENAME = $("#UNIQUENAME div[row][current]").length - 1;
	$("#btnAddMapUNIQUENAME").click(function() {
		var rowIndex = parseInt(lastIndexUNIQUENAME) + 1;
		lastIndexUNIQUENAME = rowIndex;
		console.log("Adding row " + rowIndex);
		var e = "<div current row='" + rowIndex + "' name='VARIABLE' style='margin-bottom:4px;'>";
		e += componentInputUNIQUENAMEKey.replace(/\[\]/g, "[" + rowIndex + "]");
		e += componentInputUNIQUENAMEValue.replace(/\[\]/g, "[" + rowIndex + "]");
		e += "<input type='button' value=' - ' onclick='removeUNIQUENAME(this);' style='width: 30px; margin-top: 6px;' />";
		e += "</div>";
		$("#btnAddMapUNIQUENAME").before(e);
		updateIndexesUNIQUENAME();
		COMPONENT_JS_HANDLER
		$("#UNIQUENAME").trigger("onRowAdded", [rowIndex]);
	});
});

function getSizeUNIQUENAME() {
	return parseInt($("input[name='VARIABLE.indexes']").val().split(',').length);
}

function removeUNIQUENAME(button) {
	var div = $(button).closest("div");
	var rowIndex = parseInt(div.attr("row"));
	console.log("Removing row ", rowIndex);
	div.find(".inputFileDelete").each(function() {
		$(this).click();
	});
	div.remove();
	updateIndexesUNIQUENAME();
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

function updateIndexesUNIQUENAME() {		
	var ids = "";
	$("#UNIQUENAME div[row][current]").each(function() {
		ids == "" ? ids = $(this).attr('row') : ids += "," + $(this).attr('row') ; 
	});
	var indexesInput = $("input[name='VARIABLE.indexes']");
	indexesInput.val(ids);
	console.log("List size = " + getSizeUNIQUENAME());
}