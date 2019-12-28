module uapi.behavior {
    requires static auto.service.annotations;
    requires static uapi.codegen;
    requires static uapi.service.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.event;
    requires uapi.command;
    requires uapi.command.apt;

    exports uapi.behavior;
}