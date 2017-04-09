package uapi.service.internal

import spock.lang.Specification
import uapi.service.Dependency

/**
 * Unit test for UnactivatedService
 */
class UnactivatedServiceTest extends Specification {

    def 'Test create instance'() {
        when:
        new UnactivatedService(Mock(Dependency), Mock(ServiceHolder))

        then:
        noExceptionThrown()
    }
}
