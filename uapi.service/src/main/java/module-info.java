module uapi.service {
    requires auto.service.annotations;
    requires com.google.common;
    requires freemarker;
    requires uapi.common;
    requires uapi.codegen;
    requires uapi.annotation;
    requires uapi.exception;
    requires uapi.state;

    exports uapi.service;
    exports uapi.log;
}