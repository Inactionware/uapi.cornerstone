return new uapi.command.IOptionMeta[] {
<#list options as opt>
            new uapi.command.internal.OptionMeta("${opt.name()}", '${opt.shortName()}', "${opt.argument()}", "${opt.description()}", uapi.command.OptionType.${opt.type()})<#sep>, </#sep>
</#list>
        };