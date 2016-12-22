var ie6compatibility = $.browser.msie && $.browser.version < 8;

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
				
				var currentValue = "";
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
				$("#variableCurrentInfo").find('*').prop('disabled', true);
				
				
				$("#variableInput").append(data.input);
				
				$('#nullValueCheckbox').change(function() {
			        if($(this).is(':checked')) {
			        	$("[name=" + $("#variableSelect").val() + "]").attr('disabled', 'true');
			        } else {
			        	$("[name=" + $("#variableSelect").val() + "]").attr('disabled', null);
			        }
			    });
				
				$(".inputTime").filter(filterTemplatesElements).timepicker({ ampm: false, seconds: false });
				$(".inputDate").filter(filterTemplatesElements).datepicker({ dateFormat: "dd.mm.yy", buttonImage: "/wfe/images/calendar.gif" });
				$(".inputDateTime").filter(filterTemplatesElements).datetimepicker({ dateFormat: "dd.mm.yy" });
				
				$('.dropzone').filter(filterTemplatesElements).each(function () {
					initFileInput($(this));
				});
				$(".dropzone").filter(filterTemplatesElements).bind("dragleave dragend drop", function (e) {
					$(this).removeClass("dropzonehover");
				});
				$(".dropzone").filter(filterTemplatesElements).bind('dragover', function (e) {
					$(this).addClass("dropzonehover");
				});
				if (ie6compatibility) {
					$(".inputFileContainer").css("width", "400px");
					$(".inputFileAjax").css({"width": "77px", "height": "26px", "cursor": "pointer", "filter": "alpha(opacity=50)"});
					$(".inputFileAttachButtonDiv").css("width", "170px");
					$(".inputFileAttach").css("cursor", "default");
				}
			}
		});	
	} else {
		$(".button").css("display", "none");
	}
}
