import uapi.IModulePortal;
import uapi.annotation.Module;
import uapi.service.internal.ServiceModulePortal;

@Module
module uapi.service {
    requires auto.service.annotations;
    requires com.google.common;
    requires freemarker;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.annotation;
    requires uapi.exception;
    requires uapi.state;

    exports uapi.service;
    exports uapi.log;

    provides IModulePortal with ServiceModulePortal;
}