import uapi.IModulePortal;
import uapi.config.internal.ConfigModulePortal;

module uapi.config {
    requires static uapi.codegen;
    requires static uapi.service.apt;

    requires jackson.core;
    requires uapi.common;
    requires uapi.state;
    requires uapi.service;

    provides IModulePortal with ConfigModulePortal;

    exports uapi.config;
}