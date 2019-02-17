return new uapi.behavior.ActionOutputMeta[] {
<#list actionParameterMetas as actionParameterMeta>
    <#if actionParameterMeta.type == "OUTPUT">
        new uapi.behavior.ActionOutputMeta(${actionParameterMeta.className}.class, "${actionParameterMeta.name}")<#sep>, </#sep>
    </#if>
</#list>
        };