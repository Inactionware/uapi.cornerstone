module uapi.config {
    requires static uapi.codegen;
    requires static uapi.service.apt;

    requires jackson.core;
    requires uapi.common;
    requires uapi.state;
    requires uapi.service;

    exports uapi.config;
}