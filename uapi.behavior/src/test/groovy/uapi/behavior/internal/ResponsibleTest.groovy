/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.BehaviorException
import uapi.common.Repository
import uapi.event.IEventBus

/**
 * Unit test for Responsible
 */
class ResponsibleTest extends Specification {

    def 'Test create instance'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))

        then:
        noExceptionThrown()
        responsible.name() == 'name'
    }

    def 'Test create new behavior by topic'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        def bBuilder = responsible.newBehavior('bName', 'topic')

        then:
        noExceptionThrown()
        bBuilder != null
    }

    def 'Test create duplicated name behavior by topic'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        responsible.newBehavior('bName', 'topic')
        responsible.newBehavior('bName', 'topic2')

        then:
        thrown(BehaviorException)
    }

    def 'Test create new behavior by type'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        def bBuilder = responsible.newBehavior('bName', String.class)

        then:
        noExceptionThrown()
        bBuilder != null
    }

    def 'Test create duplicated name behavior by type'() {
        when:
        def responsible = new Responsible('name', Mock(IEventBus), Mock(Repository))
        responsible.newBehavior('bName', String.class)
        responsible.newBehavior('bName', Integer.class)

        then:
        thrown(BehaviorException)
    }
}
