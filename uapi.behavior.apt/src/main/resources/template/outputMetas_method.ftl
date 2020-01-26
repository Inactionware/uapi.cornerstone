return new uapi.behavior.ActionOutputMeta[] {
<#if isHandyOutput>
        new uapi.behavior.ActionOutputMeta(${handyOutputMeta.className}.class)
<#else>
    <#list actionParameterMetas as actionParameterMeta>
        <#if actionParameterMeta.type == "OUTPUT">
        new uapi.behavior.ActionOutputMeta(${actionParameterMeta.className}.class, "${actionParameterMeta.name}")<#sep>, </#sep>
        </#if>
    </#list>
</#if>
        };