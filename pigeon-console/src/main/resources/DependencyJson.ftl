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
		"address":  [
		<#list heartbeats[key] as client>
			{
				"server": "${client}"
			}<#if client_has_next>,</#if>
		</#list>
		]
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
],
"serviceAddresses": [
<#list serviceAddresses?keys as key>
	{
		"service": "${key}",
		"addresses":  [
		<#list serviceAddresses[key] as addr>
			{
				"server": "${addr}"
			}<#if addr_has_next>,</#if>
		</#list>
		]
	}<#if key_has_next>,</#if>
</#list>
]
}

