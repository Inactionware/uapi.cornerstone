return new uapi.command.IParameterMeta[] {
<#list parameters as param>
            new uapi.command.ParameterMeta("${param.name()}", ${param.required()?c}, "${param.description()}")<#sep>, </#sep>
</#list>
};