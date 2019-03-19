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

/**
 * Unit test for BehaviorFinishedEvent
 */
class BehaviorFinishedEventTest extends Specification {

    def 'Test create instance'() {
        when:
        def exeId = new ExecutionIdentify(behaviorName, ActionType.BEHAVIOR, sequence)
        def instance = new BehaviorFinishedEvent(srcName, exeId, inputs, outputs, null)

        then:
        noExceptionThrown()
        instance.topic() == BehaviorTraceEvent.TOPIC
        instance.executionId() == exeId
        instance.behaviorName() == behaviorName
        instance.behaviorInputs() == inputs
        instance.behaviorOutputs() == outputs

        where:
        srcName | behaviorName    | sequence  | inputs                  | outputs
        'src'   | 'BName'         | 2         | ['abc'] as String[]     | [] as ActionOutput[]
    }
}
