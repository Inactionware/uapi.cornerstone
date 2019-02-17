try {
            super.${actionMethodName}(
<#list actionParameterMetas as actionParameterMeta>
    <#if actionParameterMeta.type == uapi.behavior.IActionHandlerHelper.ParameterType.INPUT>
                inputs[${actionParameterMeta.index}]<#sep>, </#sep>
    <#elseif actionParameterMeta.type == uapi.behavior.IActionHandlerHelper.ParameterType.OUTPUT>
                outputs[${actionParameterMeta.index}]<#sep>, </#sep>
    <#else>
                context<#sep>, </#sep>
    </#if>
</#list>
            );
        } catch (Exception ex) {
            throw new uapi.GeneralException(ex);
        }