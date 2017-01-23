uapi.common.ArgumentChecker.notEmpty(path, "path");
        uapi.common.ArgumentChecker.notNull(configObject, "configObject");
        uapi.config.ConfigValueParsers parsers = this.${fieldSvcReg}.findService(uapi.config.ConfigValueParsers.class);
        uapi.common.ArgumentChecker.notNull(parsers, "parsers");
<#list configInfos as configInfo>
        if (path.equals("${configInfo.path}")) {
            <#if configInfo.hasParser()>
            uapi.config.IConfigValueParser parser = parsers.findParser("${configInfo.parserName}");
            <#else>
            uapi.config.IConfigValueParser parser = parsers.findParser(configObject.getClass().getCanonicalName(), "${configInfo.fieldType}");
            </#if>
            this.${configInfo.fieldName} = parser.parse(configObject);
            return;
        }
</#list>
        throw new uapi.GeneralException("Can't set config object {} into service {}", configObject, this);