/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior

import spock.lang.Specification
import uapi.InvalidArgumentException

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

    def 'Test to action id'() {
        when:
        def actionId = ActionIdentify.toActionId(classType)

        then:
        noExceptionThrown()
        actionId.id == id
        actionId.name == name
        actionId.type == type

        where:
        classType       | id                                                | name                                      | type
        String.class    | 'java.lang.String@ACTION'                         | 'java.lang.String'                        | ActionType.ACTION
        Test.class      | 'uapi.behavior.ActionIdentifyTest.Test@ACTION'    | 'uapi.behavior.ActionIdentifyTest.Test'   | ActionType.ACTION
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
        'A@ABVC'        | null
    }

    def 'Test hashCode'() {
        when:
        def actionId = ActionIdentify.parse(id)

        then:
        actionId.hashCode() != null

        where:
        id          | placeholder
        '1@ACTION'  | null
    }

    def 'Test toString'() {
        when:
        def actionId = ActionIdentify.parse(id)

        then:
        actionId.toString() == id

        where:
        id          | placeholder
        '1@ACTION'  | null
    }

    def 'Test equals'() {
        when:
        def actionId1 = ActionIdentify.parse(id1)
        def actionId2 = ActionIdentify.parse(id2)

        then:
        actionId1.equals(actionId2) == result
        ! actionId1.equals(null)
        ! actionId2.equals(null)
        ! actionId1.equals(new Object())

        where:
        id1             | id2           | result
        '1@ACTION'      | '1@ACTION'    | true
        '1@BEHAVIOR'    | '1@BEHAVIOR'  | true
    }

    public class Test {}
}

