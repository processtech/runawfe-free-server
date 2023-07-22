
var ellUNIQUENAMEVariableNames = [VARIABLE_NAMES];
var ellUNIQUENAMERowTemplate = "ROW_TEMPLATE";
var lastIndexUNIQUENAME = -1;

$(document).ready(function() {
	ellUNIQUENAMEUpdateIndexes(0);
	lastIndexUNIQUENAME = $("#ellUNIQUENAME tr[row]").length - 1;
	$('#ellUNIQUENAMEButtonAdd').click(function() {
		var rowIndex = parseInt(lastIndexUNIQUENAME) + 1;
		lastIndexUNIQUENAME = rowIndex;
		var rowElementHtml = "<tr row='" + rowIndex + "'>";
		rowElementHtml += ellUNIQUENAMERowTemplate.replace(/\[\]/g, "[" + rowIndex + "]");
		rowElementHtml += "<td><input type='button' value=' - ' onclick='ellUNIQUENAMERemoveRow(this);' /></td>";
		rowElementHtml += "</tr>";
		var rowElement = $(rowElementHtml);
		$('#ellUNIQUENAME').append(rowElement);
		ellUNIQUENAMEUpdateIndexes(1);
		initComponents(rowElement);
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
	$("#ellUNIQUENAME input[name$='indexes']").val(idsString);
}
