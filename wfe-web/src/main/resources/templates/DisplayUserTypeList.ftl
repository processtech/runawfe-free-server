<table class="userTypeList list">
	<thead>
		<tr>
			<#list model.attributes as attribute>
			<th class="list">${attribute.name}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list model.variableValue as row>
		<tr>			
			<#list model.attributes as attribute>
			<td class="list">${model.getValue(row, attribute, row?index)}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>