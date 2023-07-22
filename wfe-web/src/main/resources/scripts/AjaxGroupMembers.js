
$(document).ready(function(){
	$("#groupSelectorId").change(function(event){
		var s = $("#groupSelectorId").val();
		if (s == '') {
			return;
		}
		$("#userSelectorId option").remove();
		$("#userSelectorId").attr("disabled", "disabled");
		$("#userSelectorId").append('<option> ... </option>');
		$.getJSON(
			"JSON_URL", 
			{ groupId: s },
			function(data) {
				$("#userSelectorId option").remove();
				$.each (data, function(i, item) { 
				$("#userSelectorId").append("<option value='"+item.id+"'>"+item.name+"</option>");
			});
			$("#userSelectorId").removeAttr("disabled");
		});
	});
});