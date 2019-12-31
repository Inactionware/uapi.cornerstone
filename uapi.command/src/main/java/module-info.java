import uapi.IModulePortal;
import uapi.command.internal.CommandModulePortal;

module uapi.command {
    requires static uapi.service.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;

    provides IModulePortal with CommandModulePortal;

    exports uapi.command;
}