package uapi.behavior.internal

import spock.lang.Specification
import uapi.InvalidArgumentException
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionType

/**
 * Unit test for ActionIdentify
 */
class ActionIdentifyTest extends Specification {

    def 'Test create instance'() {
        when:
        def actionId = ActionIdentify.parse(id)

        then:
        noExceptionThrown()
        actionId.id == id
        actionId.parts == parts
        actionId.name == name
        actionId.type == type

        where:
        id              | name  | type                  | parts
        '1@ACTION'      | '1'   | ActionType.ACTION     | ['1', ActionType.ACTION] as Object[]
        '2@BEHAVIOR'    | '2'   | ActionType.BEHAVIOR   | ['2', ActionType.BEHAVIOR] as Object[]
    }

    def 'Test create instance with incorrect id'() {
        when:
        def actionId = ActionIdentify.parse(id)

        then:
        thrown(InvalidArgumentException)

        where:
        id              | placeholder
        '@ACTION'       | null
        'BEHAVIOR'      | null
        'A@B@ACTION'    | null
    }
}
