module uapi.service.apt {
    requires auto.service.annotations;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.service;

    exports uapi.service;
    exports uapi.service.annotation;
}