$(function() {
	getCurrentExecutor($("#swimlaneSelect").val());
	$("#swimlaneSelect").bind("change", function() {
		getCurrentExecutor($("#swimlaneSelect").val());
	});
});

function getCurrentExecutor(value) {
	if (value != null) {
		jQuery.ajax({
			type : "POST",
			cache : false,
			url : "/wfe/ajaxcmd?command=ajaxSwimlaneCurrentExecutor",
			data : JSON.stringify({
				processId : id,
				swimlaneName : value
			}),
			dataType : "json",
			contentType : "application/json",
			processData : false,
			success : function(result) {
				var currentExecutor = $("#currentExecutor")
				currentExecutor.empty();
				currentExecutor.append("<div class=\"variableLabel\">"
						+ result.currentExecutorName + "</div>");
			}
		});
	} else {
		$(".button").css("display", "none");
	}
}
