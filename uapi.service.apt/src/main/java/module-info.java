module uapi.service.apt {
    requires java.compiler;
    requires auto.common;
    requires auto.service.annotations;
    requires freemarker;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.service;

    exports uapi.service.annotation;
    exports uapi.service.annotation.helper;
}