import uapi.IModulePortal;
import uapi.app.internal.AppModulePortal;
import uapi.service.IService;

module uapi.app {
    requires static uapi.codegen;
    requires static uapi.service.apt;
    requires static uapi.config.apt;
    requires static uapi.behavior.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.config;
    requires uapi.event;
    requires uapi.behavior;

    uses IModulePortal;

    provides IModulePortal with AppModulePortal;

    exports uapi.app;
}