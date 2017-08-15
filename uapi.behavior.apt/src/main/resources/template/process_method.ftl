try {
<#if needContext>
    <#if isOutVoid>
        <#if isInVoid>
            super.${actionMethodName}(context);
            return null;
        <#else>
            super.${actionMethodName}(input, context);
            return null;
        </#if>
    <#else>
        <#if isInVoid>
            return super.${actionMethodName}(context);
        <#else>
            return super.${actionMethodName}(input, context);
        </#if>
    </#if>
<#else>
    <#if isOutVoid>
        <#if isInVoid>
            super.${actionMethodName}();
            return null;
        <#else>
            super.${actionMethodName}(input);
            return null;
        </#if>
    <#else>
        <#if isInVoid>
            return super.${actionMethodName}();
        <#else>
            return super.${actionMethodName}(input);
        </#if>
    </#if>
</#if>
        } catch (Exception ex) {
            throw new uapi.GeneralException(ex);
        }