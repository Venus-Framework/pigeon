{"invokers": [
<#list invokers as x>
	{
		"invoker": "${x}"
	}<#if x_has_next>,</#if>
</#list>
],
"heartbeats": [
<#list heartbeats?keys as key>
	{
		"service": "${key}",
		"address": "${heartbeats[key]}"
	}<#if key_has_next>,</#if>
</#list>
],
"reconnects": [
<#list reconnects?keys as key>
	{
		"service": "${key}",
		"address": "${reconnects[key]}"
	}<#if key_has_next>,</#if>
</#list>
]
}

