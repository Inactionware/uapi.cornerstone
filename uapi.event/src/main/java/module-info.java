import uapi.IModulePortal;
import uapi.event.internal.EventModulePortal;

module uapi.event {
    requires static uapi.codegen;
    requires static uapi.service.apt;
    requires static uapi.config.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.config;

    provides IModulePortal with EventModulePortal;

    exports uapi.event;
}