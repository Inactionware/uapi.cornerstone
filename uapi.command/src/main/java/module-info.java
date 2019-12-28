module uapi.command {
    requires static auto.service.annotations;
    requires static uapi.service.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;

    exports uapi.command;
}