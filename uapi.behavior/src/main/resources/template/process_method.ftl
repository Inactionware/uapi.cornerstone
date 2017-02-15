<#if needContext>
        return super.${actionMethodName}(input);
<#else>
        return super.${actionMethodName}(input, context);
</#if>