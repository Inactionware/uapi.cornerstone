<#list parameters as param>
if ("${param.name()}".equals(name)) {
            this.${param.userCommandField()}.${param.setterName()}((${param.type()}) value);
            return;
        }
</#list>
        throw uapi.command.CommandException.builder()
                .errorCode(uapi.command.CommandErrors.UNSUPPORTED_PARAMETER)
                .variables(new uapi.command.CommandErrors.UnsupportedParameter()
                        .parameterName(name)
                        .commandId(commandId()))
                .build();