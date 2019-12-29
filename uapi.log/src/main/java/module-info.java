import uapi.IModulePortal;
import uapi.log.internal.LogModulePortal;

module uapi.log {
    requires static uapi.codegen;
    requires static uapi.service.apt;

    requires slf4j.api;
    requires uapi.common;
    requires uapi.service;

    provides IModulePortal with LogModulePortal;
}