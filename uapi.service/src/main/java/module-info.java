module uapi.service {
    requires auto.service.annotations;
    requires freemarker;
    requires uapi.common;
    requires uapi.annotation;
    requires uapi.exception;
    requires uapi.state;

    exports uapi.service;
    exports uapi.log;
}