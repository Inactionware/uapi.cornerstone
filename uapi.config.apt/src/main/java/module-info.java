module uapi.config.apt {
    requires java.compiler;
    requires auto.service.annotations;
    requires auto.common;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;
    requires uapi.config;

    exports uapi.config.annotation;
}