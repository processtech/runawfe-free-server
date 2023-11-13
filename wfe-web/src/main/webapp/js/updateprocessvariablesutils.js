$(document).ready(function() {
	var $submitButton = $("input[name='submitButton']");
	var $variableName = $("input[name='variableName']");
	$submitButton.attr("disabled", true);
	$variableName.autocomplete({
		delay: 300,
		minLength: 0,
		source: function(request, response) {
			$(".errors").html("");
		    $.ajax({
		   	    type: "GET",
			    cache: false,
			    url: getVariablesUrl,
			    data: {
                    processId : id,
                    hint : request.term
                },
			    dataType: "json",
			    success:
			    function (result) {
				    response(result.data);
			    }
		    })
        },
		select: function(event, ui) {
			$variableName.val(ui.item.label).change();
			return false;
		}
	});
    $variableName.focus(function() {
		$(this).autocomplete("search", $(this).val());
	});
	
	function getVariableInfo() {
		$submitButton.attr("disabled", true);
		var variableName = $variableName.val();
		if (!variableName) {
			return;
		}
		jQuery.ajax({
			type: "GET",
			cache: false,
			url: "/wfe/getVariable",
			data: {
				id: id,
				variableName: variableName,
				displayInChat: displayInChat
			},
			dataType: "html",
			success: function (e, msg, result) {
				var data = jQuery.parseJSON(result.responseText);
				$("#variableInput").empty();
				var currentValue;
				if (data.variableIsNull == "true") {
					currentValue = "<div class=\"variableLabel\"><input type='checkbox' disabled='true' checked>NULL</div>";
					$("#nullValueCheckbox").css("display", "none");
					$("#nullValueLabel").css("display", "none");
				} else {
					currentValue = "<div class=\"variableScript\">" + data.variableValue + "</div>";
					$("#nullValueCheckbox").css("display", "inline");
					$("#nullValueLabel").css("display", "inline");
				}
				$("#variableInput").append(data.input);
				$("#nullValueCheckbox").change(function() {
			        if ($(this).is(':checked')) {
			        	$("[name=" + $("#variableSelect").val() + "]").attr("disabled", "true");
			        } else {
			        	$("[name=" + $("#variableSelect").val() + "]").attr("disabled", null);
			        }
			    });
				initComponents($("#variableInput"));
				$submitButton.attr("disabled", !data.scriptingName);
			}
		});
	}	

	$variableName.change(getVariableInfo);
	$variableName.change();
});