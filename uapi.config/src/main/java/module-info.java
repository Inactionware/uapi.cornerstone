module uapi.config {
    requires jackson.core;
    requires uapi.common;
    requires uapi.state;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;

    exports uapi.config;
}