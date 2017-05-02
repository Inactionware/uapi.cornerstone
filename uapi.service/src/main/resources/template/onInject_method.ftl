<#list methods as method>
    if (${method.id}.equals(serviceId) {
        super.${method.name}((method.type) service);
        return;
    }
</#list>
    throw new uapi.common.GeneralException("Inject unsupported service - id: {}, type: {}", ${method.id}, ${method.type});