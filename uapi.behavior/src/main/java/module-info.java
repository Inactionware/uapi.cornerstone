import uapi.IModulePortal;
import uapi.behavior.internal.BehaviorModulePortal;

module uapi.behavior {
    requires static uapi.codegen;
    requires static uapi.service.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.event;
    requires uapi.command;
    requires uapi.command.apt;

    provides IModulePortal with BehaviorModulePortal;

    exports uapi.behavior;
}