{"invokers": [
<#list invokers as x>
	{
		"invoker": "${x}"
	}<#if x_has_next>,</#if>
</#list>
]
}
