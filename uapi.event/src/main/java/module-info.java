module uapi.event {
    requires auto.service.annotations;
    requires uapi.common;
    requires uapi.exception;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;
    requires uapi.config;
    requires uapi.config.apt;

    exports uapi.event;
}