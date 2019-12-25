module uapi.app {
    requires auto.service.annotations;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.service.apt;
    requires uapi.config;
    requires uapi.config.apt;
    requires uapi.event;
    requires uapi.behavior;
    requires uapi.behavior.apt;

    exports uapi.app;
}