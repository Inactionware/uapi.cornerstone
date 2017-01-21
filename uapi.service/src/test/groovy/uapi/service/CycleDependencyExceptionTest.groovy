package uapi.service

import spock.lang.Specification
import uapi.service.internal.IServiceHolder

/**
 * Unit test for CycleDependencyException
 */
class CycleDependencyExceptionTest extends Specification {

    def 'Test create instance'() {
        given:
        def svcHolder = Mock(IServiceHolder)
        def dependencies = new Stack()
        dependencies.push(svcHolder)

        when:
        new CycleDependencyException(dependencies)

        then:
        noExceptionThrown()
    }

    def 'Test get message'() {
        given:
        def svcHolder = Mock(IServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> qid
            }
        }
        def svcHolder2 = Mock(IServiceHolder) {
            getQualifiedId() >> Mock(QualifiedServiceId) {
                toString() >> qid2
            }
        }
        def dependencies = new Stack()
        dependencies.push(svcHolder)

        when:
        def ex = new CycleDependencyException(dependencies)

        then:
        noExceptionThrown()
        ex.getMessage() == msg

        where:
        qid         | qid2      | msg
        'svcid'     | 'svcid2'  | 'Found dependency cycle -> svcid'
    }
}
