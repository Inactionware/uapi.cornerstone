module uapi.command {
    requires auto.service.annotations;
    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;

    exports uapi.command;
}