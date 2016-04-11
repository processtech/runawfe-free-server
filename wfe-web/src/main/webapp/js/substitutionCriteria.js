
jQuery().ready(function($) {
	$("#type").change(changeParameters);
	$.editDialog = $('<div></div>').dialog({
		width: 300, modal:true, autoOpen: false, 
		overlay: {backgroundColor: '#000', opacity: 0.5}
	});
	$.editDialog.html("<input id='dialogFilter' style='width: 100%;'><div id='dialogContent' style='height: 300px; overflow-y: scroll;'></div>");
	$("#dialogFilter").change(function() {
		filterItems();
	});
	$("#dialogFilter").keyup(function() {
		filterItems();
	});
});

function changeParameters() {
	$("tr[paramIndex]").each(function() {
		$(this).remove();
	});
	var fd = $("div[id='"+$("#type").val()+"']");
	fd.children("div").each(function(index, value) {
		var tr = "<tr paramIndex='"+index+"'>";
		$(this).children("span").each(function(index, value) {
			tr += "<td class='list'>"+$(this).html()+"</td>";
		});
		tr += "</tr>";
		$("#paramsTable").append(tr);
	});
	$("#paramsTable").find("input[paramIndex]").each(function() {
		$(this).removeAttr("disabled");
	});
}

function filterItems(filterText) {
	var filterText = $("#dialogFilter").val().toLowerCase();
	$("#dialogContent").children("div").each(function() {
		if ($(this).text().toLowerCase().indexOf(filterText) == 0) {
			$(this).show();
		} else {
			$(this).hide();
		}
	});
}

var selectInputId;
function editParameter(inputId, rendererClassName) {
	selectInputId = inputId;
	$("#dialogContent").html("");
    $.getJSON(
		"/wfe/ajaxcmd?command=getParamDialogData&renderer="+rendererClassName,
		function(data) {
			$.each(data, function(i, item) {
				$("#dialogContent").append("<div><a href='javascript:select(\""+item.value+"\");'>"+item.text+"</a></div>");
			});
		}
	);
	$.editDialog.dialog('open');
}

function select(value) {
	$("input[paramIndex='"+selectInputId+"']").val(value);
	$.editDialog.dialog("close");
}
