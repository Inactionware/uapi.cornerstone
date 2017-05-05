<#list methods as method>
    if ("${method.serviceId}".equals(serviceId)) {
        super.${method.methodName}((${method.serviceType}) service);
        return;
    }
</#list>
    throw new uapi.GeneralException("Inject unsupported service - id: {}, service: {}", serviceId, service.getClass().getName());