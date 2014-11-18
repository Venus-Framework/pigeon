{"requestsSendInLastSecond": [
<#list requestsInLastSecond?keys as key>
	{
		"address": "${key}",
		"requestsInLastSecond": "${requestsInLastSecond[key]}"
	}<#if key_has_next>,</#if>
</#list>
],"requestsReceived": [
<#list appRequests?keys as key>
	{
		"app": "${key}",
		"requests": "${appRequests[key]}"
	}<#if key_has_next>,</#if>
</#list>
],"serverProcessorStatistics": [
<#list serverProcessorStatistics?keys as key>
	{
		"server": "${key}",
		"processorStatistics": "${serverProcessorStatistics[key]}"
	}<#if key_has_next>,</#if>
</#list>
],"serverWeightStatistics": [
<#list weightFactors?keys as key>
	{
		"server": "${key}",
		"weightFactors": "${weightFactors[key]}"
	}<#if key_has_next>,</#if>
</#list>
],"otherStatistics": [
<#list others?keys as key>
	{
		"source": "${key}",
		"info": "${others[key]}"
	}<#if key_has_next>,</#if>
</#list>
]
}

