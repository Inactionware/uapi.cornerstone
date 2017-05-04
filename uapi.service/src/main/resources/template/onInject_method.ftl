<#list methods as method>
    if (${method.serviceId}.equals(serviceId) {
        super.${method.methodName}((method.serviceType) service);
        return;
    }
</#list>
    throw new uapi.common.GeneralException("Inject unsupported service - id: {}, type: {}", ${method.serviceId}, ${method.serviceType});