module uapi.behavior.apt {
    requires java.compiler;
    requires auto.service.annotations;
    requires auto.common;
    requires com.google.common;
    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;
    requires uapi.behavior;

    exports uapi.behavior.annotation;
    exports uapi.behavior.annotation.helper;
}