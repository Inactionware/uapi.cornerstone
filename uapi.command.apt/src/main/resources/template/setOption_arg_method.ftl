<#list options as opt>
<#if opt.isString()>
if ("${opt.name()}".equals(name)) {
            this.${opt.userCommandField()}.${opt.setterName()}("${opt.argument()}", argument);
            return;
        }
</#if>
</#list>
        throw uapi.command.CommandException.builder()
                .errorCode(uapi.command.CommandErrors.UNSUPPORTED_OPTION)
                .variables(new uapi.command.CommandErrors.UnsupportedOption()
                        .optionName(name)
                        .command(commandId()))
                .build();