$.processUpgradeDialog = undefined;
function selectProcessUpgrageVersionDialog(link) {
	var definitionName = $(link).attr("data-definitionName");
	var definitionVersion = parseInt($(link).attr("data-definitionVersion"));
	if ($.processUpgradeDialog == undefined) {
		var dialog = $("<div>", {"id" : "delegateDialog"});
		$("<table class='list definitions'></table>").appendTo(dialog);
		$.processUpgradeDialog = dialog.dialog({
			width: 800,
			height: 500,
			buttons: [
				{
					text: "Ok",
					click: function() {
						submitProcessUpgrade();
					}
				},
				{
					text: buttonCancelMessage,
					click: function() {
						$.processUpgradeDialog.dialog("close");
					}
				}
			]
		});
	}

	var d = $.processUpgradeDialog;
	var definitionsTable = d.find("table.definitions");

	function request() {
		$.ajax({
			type: "POST",
			url: "/wfe/ajaxcmd?command=ajaxDefinitionVersions&definitionName=" + definitionName,
			dataType: "json",
			contentType: "application/json; charset=UTF-8",
			processData: false,
			success: function(data) {
				processDefinitions(data);
			}
		});
	}

	function processDefinitions(data) {
		definitionsTable.empty();
		var headers = $("<tr class='list'>");
		$("<th class='list'></td>").appendTo(headers);
		$("<th class='list'>" + $.processUpgradeDialogStatic.version + "</td>").appendTo(headers);
		$("<th class='list'>" + $.processUpgradeDialogStatic.dateUpload + "</td>").appendTo(headers);
		$("<th class='list'>" + $.processUpgradeDialogStatic.authorUpload + "</td>").appendTo(headers);
		$("<th class='list'>" + $.processUpgradeDialogStatic.dateUpdate + "</td>").appendTo(headers);
		$("<th class='list'>" + $.processUpgradeDialogStatic.authorUpdate + "</td>").appendTo(headers);
		$("<th class='list'>" + $.processUpgradeDialogStatic.description + "</td>").appendTo(headers);
		headers.appendTo(definitionsTable);
		$.each(data.data, function(k, v) {
			var row = $("<tr class='list'>");
			var radio = $("<input type='radio' name='version' value='" + v.version + "'></input>");
			if (v.version == definitionVersion) {
				radio.attr("checked", "checked");
				row.attr("style", "background-color: #ccc;");
			}
			$("<td class='list'></td>").appendTo(row).append(radio);
			$("<td class='list'>" + v.version + "</td>").appendTo(row);
			$("<td class='list'>" + v.createDate + "</td>").appendTo(row);
			$("<td class='list'>" + v.createUserName + "</td>").appendTo(row);
			$("<td class='list'>" + v.updateDate + "</td>").appendTo(row);
			$("<td class='list'>" + v.updateUserName + "</td>").appendTo(row);
			$("<td class='list'>" + v.description + "</td>").appendTo(row);
			row.appendTo(definitionsTable);
		});
	}

	function submitProcessUpgrade() {
		var version = $("input[type='radio'][name='version']:checked").val();
		if (version != definitionVersion) {
			window.location = $(link).attr("href") + "&version=" + version;
		}
	}

	request();
	$.processUpgradeDialog.dialog("open");
}
