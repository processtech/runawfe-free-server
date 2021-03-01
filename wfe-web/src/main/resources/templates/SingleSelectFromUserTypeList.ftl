<div class="inputVariable ${model.uniqueName}" variable="${model.variable.definition.name}">
<table class="userTypeList list">
	<thead>
		<tr>
			<th class="list"></th>
			<#list model.attributes as attribute>
			<th class="list">${attribute.name}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list model.variableValue as row>
		<tr>			
			<td class="list"><input name="${model.inputVariable.definition.name}" type="radio" value="${row?index}" /></td>
			<#list model.attributes as attribute>
			<td class="list">${model.getValue(row, attribute, row?index)}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>
</div>