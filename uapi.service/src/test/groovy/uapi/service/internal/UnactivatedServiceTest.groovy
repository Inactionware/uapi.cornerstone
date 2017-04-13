package uapi.service.internal

import spock.lang.Specification
import uapi.service.Dependency
import uapi.service.QualifiedServiceId
import uapi.service.ServiceException

/**
 * Unit test for UnactivatedService
 */
class UnactivatedServiceTest extends Specification {

    def 'Test create instance'() {
        given:
        def dependency = Mock(Dependency)
        def serviceHolder = Mock(ServiceHolder) {
            getId() >> 'svc'
            getUnactivatedServices() >> []
        };

        when:
        def unactivatedSvc = new UnactivatedService(dependency, serviceHolder)

        then:
        noExceptionThrown()
        unactivatedSvc.dependency() == dependency
        unactivatedSvc.serviceId() == 'svc'
        ! unactivatedSvc.isActivated()
        unactivatedSvc.service() == null
        ! unactivatedSvc.isExternalService()
        unactivatedSvc.unactivatedDependencies == []
    }

    def 'Test get external service id'() {
        given:
        def dependency = Mock(Dependency) {
            getServiceId() >> Mock(QualifiedServiceId) {
                toString() >> 'svc'
            }
        }
        def unactivatedSvc = new UnactivatedService(dependency, null)

        expect:
        unactivatedSvc.isExternalService()
        unactivatedSvc.serviceId() == 'svc'
        unactivatedSvc.unactivatedDependencies == []
    }

    def 'Test cycle dependencies check'() {
        given:
        def serviceHolder = Mock(ServiceHolder) {
            getId() >> 'svc'
        };
        def unactivatedSvc1 = new UnactivatedService(Mock(Dependency), serviceHolder)

        def unactivatedSvc2 = new UnactivatedService(Mock(Dependency), Mock(ServiceHolder) {
            getId() >> 'svc2'
        })
        unactivatedSvc2.referencedBy(unactivatedSvc1)

        def unactivatedSvc = new UnactivatedService(Mock(Dependency), serviceHolder)
        unactivatedSvc.referencedBy(unactivatedSvc2)

        when:
        unactivatedSvc.checkCycleDependency()

        then:
        thrown(ServiceException)
    }

    def 'Test activate service'() {
        given:
        def dependency = Mock(Dependency)
        def serviceHolder = Mock(ServiceHolder) {
            getId() >> 'svc'
        };
        def unactivatedSvc = new UnactivatedService(dependency, serviceHolder)

        when:
        unactivatedSvc.activate()

        then:
        noExceptionThrown()
        1 * serviceHolder.activate()
    }

    def 'Test activate an activated service'() {
        given:
        def dependency = Mock(Dependency)
        def serviceHolder = Mock(ServiceHolder) {
            getId() >> 'svc'
            isActivated() >> true
        };
        def unactivatedSvc = new UnactivatedService(dependency, serviceHolder)

        when:
        unactivatedSvc.activate()

        then:
        noExceptionThrown()
        1 * serviceHolder.activate()
    }

    def 'Test await activated service'() {
        given:
        def dependency = Mock(Dependency)
        def serviceHolder = Mock(ServiceHolder) {
            getId() >> 'svc'
            isActivated() >> true
        };
        def unactivatedSvc = new UnactivatedService(dependency, serviceHolder)

        when:
        def result = unactivatedSvc.await(1000)

        then:
        noExceptionThrown()
        result
    }

    def 'Test await unactivated service'() {
        given:
        def dependency = Mock(Dependency)
        def serviceHolder = Mock(ServiceHolder) {
            getId() >> 'svc'
            isActivated() >> false
        };
        def unactivatedSvc = new UnactivatedService(dependency, serviceHolder)

        when:
        def result = unactivatedSvc.await(1000)

        then:
        noExceptionThrown()
        ! result
    }

    def 'Test equals'() {
        given:
        def serviceHolder = Mock(ServiceHolder)
        def serviceHolder1 = Mock(ServiceHolder)
        def unactivatedSvc = new UnactivatedService(Mock(Dependency), serviceHolder)
        def unactivatedSvc1 = new UnactivatedService(Mock(Dependency), serviceHolder1)
        def unactivatedSvc2 = new UnactivatedService(Mock(Dependency), serviceHolder)

        def dependency = Mock(Dependency)
        dependency.equals(dependency) >> true
        def unactivatedSvc3 = new UnactivatedService(dependency, null)
        def unactivatedSvc4 = new UnactivatedService(Mock(Dependency), null)
        def unactivatedSvc5 = new UnactivatedService(dependency, null)

        expect:
        unactivatedSvc != unactivatedSvc1
        unactivatedSvc == unactivatedSvc2
        unactivatedSvc1 != unactivatedSvc2

        unactivatedSvc3 != unactivatedSvc4
        unactivatedSvc3 == unactivatedSvc5
    }
}
