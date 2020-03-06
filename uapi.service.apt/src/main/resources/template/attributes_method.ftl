return new Object[] {
<#list attrs as attr>
            "${attr.name}"<#sep>, </#sep>
</#list>
        };