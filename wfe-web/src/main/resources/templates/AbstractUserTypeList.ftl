<table class="userTypeList list">
	<thead>
		<tr>
			<#if model.selectable>
			<th class="list"><input class="selectionStatusPropagator" type="checkbox"></th>
			</#if>
			<#list model.attributes as attribute>
			<th class="list">${attribute.name}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list model.variable.value as row>
		<tr>			
			<#if model.selectable>
			<td class="list"><input name="${model.selectableVariable.definition.name}" type="checkbox" value="${row?index}"></td>
			</#if>
			<#list model.attributes as attribute>
			<td class="list">${model.getValue(row, attribute)}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>