import uapi.IModulePortal;
import uapi.behavior.internal.BehaviorModulePortal;

module uapi.behavior {
    requires static uapi.codegen;
    requires static uapi.service.apt;
    requires static uapi.command.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.event;
    requires uapi.command;

    provides IModulePortal with BehaviorModulePortal;

    exports uapi.behavior;
    exports uapi.behavior.generated to uapi.service;
}