import uapi.service.annotation.ModuleServiceLoader;

module uapi.log {
    requires slf4j.api;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.service;
    requires uapi.service.apt;
}