$(document).ready(function() {
	$("input[name='searchVariable']").autocomplete({
		delay: 300,
		minLength: 0,
		source: function(request, response) {
		    $.ajax({
		   	    type: "GET",
			    cache: false,
			    url: "/wfe/ajaxcmd?command=ajaxGetProcessVariablesList",
			    data:
			    {
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
			$("input[name='searchVariable']").val(ui.item.label);
			getVariableInfo(ui.item.label);
			return false;
		}
	});

    $("input[name='searchVariable']").focus(function() {
		$(this).autocomplete("search", $(this).val());
	});
});

function getVariableInfo(value) {
	if(value != null) {
		jQuery.ajax({
			type: "GET",
			cache: false,
			url: "/wfe/getComponentInput",
			data: {
				id: id,
				variableName: value
			},
			dataType: "html",
			success: function (e, msg, result) {
				var data = jQuery.parseJSON(result.responseText);
				$("#variableInput").empty();

				var currentValue;
				if(data.variableIsNull == "true") {
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
			        if($(this).is(':checked')) {
			        	$("[name=" + $("#variableSelect").val() + "]").attr("disabled", "true");
			        } else {
			        	$("[name=" + $("#variableSelect").val() + "]").attr("disabled", null);
			        }
			    });
				initComponents($("#variableInput"));
			}
		});
	} else {
		$(".button").css("display", "none");
	}
}