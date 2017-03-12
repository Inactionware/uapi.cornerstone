package uapi.service.internal;

/**
 * The enum emulate policy which is used in service creation and initialization
 */
public enum ActivePolicy {

    /**
     * The service should be activated when the service first be referenced by other service which is activated
     */
    LAZY,

    /**
     * The service should be activated as soon as possible
     */
    ASAP
}
