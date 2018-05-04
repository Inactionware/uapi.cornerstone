return new String[] {
<#list attrs as attr>
            "${attr.name}"<#sep>, </#sep>
</#list>
        };