try {
<#if needContext>
    <#if isVoid>
        super.${actionMethodName}(input, context);
        return null;
    <#else>
        return super.${actionMethodName}(input, context);
    </#if>
<#else>
    <#if isVoid>
        super.${actionMethodName}(input);
        return null;
    <#else>
        return super.${actionMethodName}(input);
    </#if>
</#if>
    } catch (Exception ex) {
        throw new uapi.GeneralException(ex);
    }