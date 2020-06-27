return new String[] {
<#list attrs as attr>
    <#if ! attr.optional>
            "${attr.name}"<#sep>, </#sep>
    </#if>
</#list>
        };