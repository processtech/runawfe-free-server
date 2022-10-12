function addRow(tableId) {
	var $table = $("#" + tableId);
	var $rows = $table.find("tr[row]");
	var index = $rows.length ? parseInt($rows.last().attr("row")) + 1 : 0;
	var $row = $("<tr>", { "row": index })
		.append($("<td>", { "class": "list" })
			.append($("<input>", { "name": tableId + "Param(" + index + ")" }))
			.append($("<button>", { "type": "button", "onclick": "removeRow('" + tableId + "', " + index + ")", "text": "-" }))
		)
		.append($("<td>", { "class": "list" })
			.append($("<input>", { "name": tableId + "Value(" + index + ")" }))
		);
	if (tableId === "payload") {
		$row.find("td:eq(1)").append('<select name="payloadType(' + index + ')"><option value="string" selected>string</option><option value="boolean">boolean</option><option value="text">text</option><option value="time">time</option><option value="datetime">datetime</option><option value="date">date</option><option value="executor">executor</option><option value="double">double</option><option value="processref">processref</option><option value="hidden">hidden</option><option value="executor">executor</option><option value="integer">integer</option><option value="bigdecimal">bigdecimal</option><option value="formattedText">formattedText</option></select>');
	}
	$table.append($row);
}

function removeRow(tableId, index) {
	$table = $("#" + tableId);
	$table.find("tr[row=" + index + "]").remove();
}

$(document).ready(function() {
	$("form").submit(
		function() {
			$.ajax({
				url: $(this).attr("action"),
				type: "POST",
				dataType: "html",
				global: false,
				data: $(this).serialize(),
				success: function(response) {
					if (response) {
						$("#ajaxErrorsDiv").html(response);
					} else {
						$("#ajaxErrorsDiv").html(signalSentMessage);
					}
				}
			});
			return false;
		}
	);
});