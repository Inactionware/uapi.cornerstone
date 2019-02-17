return new uapi.behavior.ActionInputMeta[] {
<#list actionParameterMetas as actionParameterMeta>
    <#if actionParameterMeta.type == "INPUT">
        new uapi.behavior.ActionInputMeta(${actionParameterMeta.className}.class)<#sep>, </#sep>
    </#if>
</#list>
        };