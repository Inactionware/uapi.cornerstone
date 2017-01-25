/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.event

import spock.lang.Specification

/**
 * Unit test for PlainEvent
 */
class PlainEventTest extends Specification {

    def 'Test create instance and get topic'() {
        when:
        def event = new PlainEvent(topic)

        then:
        event.topic() == topic

        where:
        topic   | placeholder
        'a'     | null
    }
}
