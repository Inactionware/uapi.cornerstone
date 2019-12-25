module uapi.behavior {
    requires auto.service.annotations;
    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;
    requires uapi.event;
    requires uapi.command;
    requires uapi.command.apt;

    exports uapi.behavior;
}