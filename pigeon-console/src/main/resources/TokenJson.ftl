{"tokens": [
<#list tokens?keys as key>
	{
		"service": "${key}",
		"token": "${tokens[key]}"
	}<#if key_has_next>,</#if>
</#list>
]
}

