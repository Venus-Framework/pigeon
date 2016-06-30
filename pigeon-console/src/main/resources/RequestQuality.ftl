{
    "support" : "${support?string("true","false")}",
    "addrReqUrlQualities" : [
        <#list addrReqUrlQualities?keys as key>
        {
            "host": "${key}",
            "reqUrlQualities": [
                <#list addrReqUrlQualities[key]? keys as key1>
                {
                    "requestUrl" : "${key1}",
                    "total" : "${addrReqUrlQualities[key][key1].getTotalValue()}",
                    "failed" : "${addrReqUrlQualities[key][key1].getFailedValue()}"
                }<#if key1_has_next>,</#if>
                </#list>
            ]
        }<#if key_has_next>,</#if>
        </#list>
    ]
}