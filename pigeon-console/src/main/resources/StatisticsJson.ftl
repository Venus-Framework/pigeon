{"serverRequestsSent": [
<#list requestsInLastSecondOfInvoker?keys as key>
	{
		"address": "${key}",
		"requestsInLastSecond": "${requestsInLastSecondOfInvoker[key]}"
	}<#if key_has_next>,</#if>
</#list>
],"appRequestsSent": [
<#list appRequestsOfInvoker?keys as key>
	{
		"app": "${key}",
		"requests": "${appRequestsOfInvoker[key]}"
	}<#if key_has_next>,</#if>
</#list>
],"appRequestsReceived": [
<#list appRequestsOfProvider?keys as key>
	{
		"app": "${key}",
		"requests": "${appRequestsOfProvider[key]}"
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

