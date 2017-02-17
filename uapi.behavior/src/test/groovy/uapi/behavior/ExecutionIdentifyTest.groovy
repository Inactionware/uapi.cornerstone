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
 * Unit test for ExecutionIdentify
 */
class ExecutionIdentifyTest extends Specification {

    def 'Test create instance with incorrect action type'() {
        when:
        new ExecutionIdentify(name, type, seq)

        then:
        thrown(InvalidArgumentException)

        where:
        name    | type              | seq
        'Test'  | ActionType.ACTION | 1
    }

    def 'Test create instance'() {
        when:
        def actionId = ActionIdentify.parse(aid)
        def exeId = new ExecutionIdentify(actionId, seq)

        then:
        noExceptionThrown()
        exeId.id == eid
        exeId.parts[0] == name
        exeId.parts[1] == type
        exeId.getSequence() == seq

        where:
        aid             | name  | type                  | seq   | eid
        '1@BEHAVIOR'    | '1'   | ActionType.BEHAVIOR   | 1     | '1@BEHAVIOR@1'
    }
}
