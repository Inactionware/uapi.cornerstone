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
 * Unit test for BehaviorExecutingEvent
 */
class BehaviorExecutingEventTest extends Specification {

    def 'Test create instance'() {
        when:
        def execId = new ExecutionIdentify(behaviorName, ActionType.BEHAVIOR, sequence)
        def event = new BehaviorExecutingEvent(srcName, execId, actionId, inputs, outputs, bInputs)

        then:
        noExceptionThrown()
        event.topic() == BehaviorTraceEvent.TOPIC
        event.executionId() == execId
        event.currentActionId() == actionId
        event.behaviorName() == behaviorName
        event.currentOutputs() == outputs
        event.behaviorInputs() == bInputs

        where:
        srcName     | behaviorName    | sequence  | inputs              | outputs               | actionId                              | bInputs
        'srcName'   | 'BName'         | 1         | ['2'] as String[]   | [] as ActionOutput[]  | ActionIdentify.parse('1@ACTION')  | ['2'] as String[]
    }
}
