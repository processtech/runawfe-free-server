<input type="hidden" name="${model.variable.definition.name}.indexes" value="" autocomplete="off" />

<table class="userTypeList list" id="eutl${model.uniqueName}">
	<thead>
		<tr>
			<#list model.attributes as attribute>
			<th class="list">${attribute.name}</th>
			</#list>
			<#if model.allowToAddElements || model.allowToDeleteElements>
			<th class="list" style="width: 30px;">
				<#if model.allowToAddElements>
				<input type="button" class="add" value=" + " />
				</#if>
			</th>
			</#if>
		</tr>
	</thead>
	<tbody>
		<#list model.variableValue as row>
		<tr row="${row?index}">
			<#list model.attributes as attribute>
			<td class="list">${model.getValue(row, attribute, row?index)}</td>
			</#list>
			<#if model.allowToAddElements || model.allowToDeleteElements>
			<th class="list">
				<#if model.allowToDeleteElements>
				<input type="button" class="remove" value=" - " />
				</#if>
			</th>
			</#if>
		</tr>
		</#list>
	</tbody>
</table>

<script>
var lastIndex${model.uniqueName} = -1;

$(document).ready(function() {
	eutl${model.uniqueName}UpdateIndexes(0);
	lastIndex${model.uniqueName} = $("#eutl${model.uniqueName} tr[row]").length - 1;
	$("#eutl${model.uniqueName} .add").click(function() {
		var rowIndex = parseInt(lastIndex${model.uniqueName}) + 1;
		lastIndex${model.uniqueName} = rowIndex;
		var template = "";
		template += "<tr row='" + rowIndex + "'>";
		<#list model.attributes as attribute>
		template += "<td class='list'>${model.getTemplateValue(attribute)}</td>";
		</#list>
		<#if model.allowToAddElements || model.allowToDeleteElements>
		template += "<th class='list'>";
		<#if model.allowToDeleteElements>
		template += "<input type='button' class='remove' value=' - ' />";
		</#if>
		template += "</th>";
		</#if>
		template += "</tr>";
		template = template.replace(/\[\]/g, "[" + rowIndex + "]");
		$("#eutl${model.uniqueName}").append(template);
		eutl${model.uniqueName}UpdateIndexes(1);
		<#list model.attributes as attribute>
		${model.getTemplateScript(attribute)}
		</#list>
		$("#eutl${model.uniqueName}").trigger("onRowAdded", [rowIndex]);
	});
	$("#eutl${model.uniqueName}").on("click", ".remove", function() {
		var tr = $(this).closest("tr");
		var rowIndex = parseInt(tr.attr("row"));
		$("#eutl${model.uniqueName}").trigger("onBeforeRowRemoved", [rowIndex]);
		tr.find(".inputFileDelete").each(function() {
			$(this).click();
		});
		tr.remove();
		eutl${model.uniqueName}UpdateIndexes(-1);
		$("#eutl${model.uniqueName}").trigger("onRowRemoved", [rowIndex]);
	});
});

function eutl${model.uniqueName}UpdateIndexes(delta) {
	var ids = [];
	$("#eutl${model.uniqueName} tr[row]").each(function() {
		ids.push($(this).attr("row")); 
	});
	var idsString = ids.join(",");
	$("input[name$='indexes']").val(idsString);
}
</script>
