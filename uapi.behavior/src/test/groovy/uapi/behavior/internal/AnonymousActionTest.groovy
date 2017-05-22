package uapi.behavior.internal

import spock.lang.Specification

/**
 * Created by xquan on 5/22/2017.
 */
class AnonymousActionTest extends Specification {

    def 'Test create instance'() {
        when:
        AnonymousAction<String, String> action = new AnonymousAction<String, String>({str, ctx -> 'test'})

        then:
        noExceptionThrown()
    }
}
