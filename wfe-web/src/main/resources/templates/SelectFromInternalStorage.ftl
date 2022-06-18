<div class="inputVariable ${model.uniqueName}" variable="${model.variable.definition.name}">
<table class="userTypeList list">
	<thead>
		<tr>
			<th class="list"><#if model.selectList><input class="selectionStatusPropagator" type="checkbox"></#if></th>
			<#list model.attributes as attribute>
			<th class="list">${attribute.name}</th>
			</#list>
		</tr>
	</thead>
	<tbody>
		<#list model.variableValue as row>
		<tr>
			<td><input name="${model.variable.definition.name}" type="${model.selectList?string('checkbox','radio')}" value="${row?index}"} /></td>
			<#list model.attributes as attribute>
			<td>${model.getValue(row, attribute, row?index)}</td>
			</#list>
		</tr>
		</#list>
	</tbody>
</table>
</div>
