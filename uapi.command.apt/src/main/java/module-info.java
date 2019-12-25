module uapi.command.apt {
    requires java.compiler;
    requires auto.service.annotations;
    requires auto.common;
    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;
    requires uapi.command;

    exports uapi.command.annotation;
}