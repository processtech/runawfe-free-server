
var ellUNIQUENAMEVariableNames = [VARIABLE_NAMES];
var ellUNIQUENAMERowTemplate = "ROW_TEMPLATE";
var lastIndexUNIQUENAME = -1;

$(document).ready(function() {
	ellUNIQUENAMEUpdateIndexes(0);
	lastIndexUNIQUENAME = $("#ellUNIQUENAME tr[row]").length - 1;
	$('#ellUNIQUENAMEButtonAdd').click(function() {
		var rowIndex = parseInt(lastIndexUNIQUENAME) + 1;
		lastIndexUNIQUENAME = rowIndex;
		var e = "<tr row='" + rowIndex + "'>";
		e += ellUNIQUENAMERowTemplate.replace(/\[\]/g, "[" + rowIndex + "]");
		e += "<td><input type='button' value=' - ' onclick='ellUNIQUENAMERemoveRow(this);' /></td>";
		e += "</tr>";
		$('#ellUNIQUENAME').append(e);
		ellUNIQUENAMEUpdateIndexes(1);
		JS_HANDLERS
		$("#ellUNIQUENAME").trigger("onRowAdded", [rowIndex]);
	});
});

function ellUNIQUENAMERemoveRow(button) {
	var tr = $(button).closest("tr");
	var rowIndex = parseInt(tr.attr("row"));
	$("#ellUNIQUENAME").trigger("onBeforeRowRemoved", [rowIndex]);
	tr.find(".inputFileDelete").each(function() {
		$(this).click();
	});
	tr.remove();
	ellUNIQUENAMEUpdateIndexes(-1);
	$("#ellUNIQUENAME").trigger("onRowRemoved", [rowIndex]);
}

function ellUNIQUENAMEUpdateIndexes(delta) {
	var ids = [];
	$("#ellUNIQUENAME tr[row]").each(function() {
		ids.push($(this).attr("row")); 
	});
	var idsString = ids.join(",");
	$("input[name$='indexes']").val(idsString);
}
