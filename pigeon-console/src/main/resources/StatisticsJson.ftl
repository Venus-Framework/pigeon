{"requestsSendInLastSecond": [
<#list requestsInLastSecond?keys as key>
	{
		"address": "${key}",
		"requestsInLastSecond": "${requestsInLastSecond[key]}"
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
]
}

