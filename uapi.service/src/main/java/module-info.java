import uapi.IModulePortal;
import uapi.annotation.Module;
import uapi.service.internal.ServiceModulePortal;

@Module
module uapi.service {
    requires static uapi.codegen;

    requires com.google.common;
    requires uapi.common;
    requires uapi.annotation;
    requires uapi.exception;
    requires uapi.state;

    exports uapi.service;
    exports uapi.log;

    provides IModulePortal with ServiceModulePortal;
}