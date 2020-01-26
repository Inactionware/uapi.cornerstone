try {
<#if isHandyOutput>
            outputs[0].set(super.${actionMethodName}(
     <#list actionParameterMetas as actionParameterMeta>
         <#if actionParameterMeta.type == "INPUT">
                 (${actionParameterMeta.className}) inputs[${actionParameterMeta.index}]<#sep>, </#sep>
         <#else>
                 context<#sep>, </#sep>
         </#if>
     </#list>
            ));
<#else>
            super.${actionMethodName}(
    <#list actionParameterMetas as actionParameterMeta>
        <#if actionParameterMeta.type == "INPUT">
                (${actionParameterMeta.className}) inputs[${actionParameterMeta.index}]<#sep>, </#sep>
        <#elseif actionParameterMeta.type == "OUTPUT">
                outputs[${actionParameterMeta.index}]<#sep>, </#sep>
        <#else>
                context<#sep>, </#sep>
        </#if>
    </#list>
            );
</#if>
        } catch (Exception ex) {
            throw new uapi.GeneralException(ex);
        }