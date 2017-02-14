return new uapi.service.Dependency[] {
<#list dependencies as dependency>
            new uapi.service.Dependency("${dependency.qualifiedServiceId}", ${dependency.serviceType}.class, ${dependency.single?c}, isOptional("${dependency.serviceId}"))<#sep>, </#sep>
</#list>
            };