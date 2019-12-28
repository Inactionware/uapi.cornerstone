module uapi.app {
    requires static auto.service.annotations;
    requires static uapi.codegen;
    requires static uapi.service.apt;
    requires static uapi.config.apt;
    requires static uapi.behavior.apt;

    requires uapi.common;
    requires uapi.exception;
    requires uapi.service;
    requires uapi.config;
    requires uapi.event;
    requires uapi.behavior;


    exports uapi.app;
}