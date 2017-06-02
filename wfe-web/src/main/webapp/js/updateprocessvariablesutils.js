
$(function() {
	getVariableInfo($("#variableSelect").val());
	$("#variableSelect").bind("change", function() {
		getVariableInfo($("#variableSelect").val());		
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
				
				$("#variableScriptingInfo").empty();
				$("#variableCurrentInfo").empty();
				$("#variableInput").empty();
				
				var scriptingValue = "<div class=\"variableLabel\">" + data.scriptingName + "</div>";
				$("#variableScriptingInfo").append(scriptingValue);
				
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
				$("#variableCurrentInfo").append(currentValue); 
				$("#variableCurrentInfo").find("*").prop("disabled", true);
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
