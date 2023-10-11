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
		$row.find("td:eq(1)").append('<select name="payloadType(' + index + ')"><option value="string" selected>string</option><option value="boolean">boolean</option><option value="time">time</option><option value="datetime">datetime</option><option value="date">date</option><option value="executor">executor</option><option value="double">double</option><option value="integer">integer</option><option value="bigdecimal">bigdecimal</option></select>');
	}
	$table.append($row);
	return index;
}

function removeRow(tableId, index) {
	$table = $("#" + tableId);
	$table.find("tr[row=" + index + "]").remove();
}

async function pasteRows() {
	try {
		$("#ajaxErrorsDiv").html("");
		$("#routing, #payload").find("tr[row]").remove();
		var text;
		if ($("#manualPasteArea").length) {
			text = $("#manualPasteArea").val();
		} else {
			text = await navigator.clipboard.readText();
		}
		var json = JSON.parse(text);
		Object.keys(json).forEach(function(tableId) {
			json[tableId].forEach(function(rowData) {
				var index = addRow(tableId);
				$("[name='" + tableId + "Param(" + index + ")']").val(rowData["name"]);
				if (tableId !== "payload") {
					$("[name='" + tableId + "Value(" + index + ")']").val(rowData["value"]);
				}
				if (rowData["type"]) {
					$("[name='payloadType(" + index + ")']").val(rowData["type"]);
				}
			});
		});
	} catch (e) {
		$("#ajaxErrorsDiv").html(pasteErrorMessage + ": " + e.message);
	}
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
						$("#ajaxErrorsDiv").html(signalIsSentMessage);
					}
				}
			});
			return false;
		}
	);
	if (!navigator.clipboard || !navigator.clipboard.readText) {
		var $button = $("#pasteButton");
		$button.before($("<textarea />", {"id": "manualPasteArea", "style": "width: 99%; height: 200px;", "placeholder": $button.text()}));
		$button.text(applyButtonName);
	}
});