return new String[] {
<#list attrs as attr>
            "${attr.name}"
</#list>
        };