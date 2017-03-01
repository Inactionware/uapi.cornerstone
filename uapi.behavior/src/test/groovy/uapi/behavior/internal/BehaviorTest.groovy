/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal

import spock.lang.Ignore
import spock.lang.Specification
import uapi.GeneralException
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionType
import uapi.behavior.IAction
import uapi.behavior.IExecutionContext
import uapi.common.Repository

/**
 * Unit test for Behavior
 */
class BehaviorTest extends Specification {

    def 'Test create instance'() {
        when:
        new Behavior(Mock(Responsible), Mock(Repository), 'aaa', String.class)

        then:
        noExceptionThrown()
    }

    @Ignore
    def 'Test create and build instance'() {
        given:
        def actionId = new ActionIdentify('name', ActionType.ACTION)
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IAction) {
                inputType() >> String.class
                outputType() >> Integer.class
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, 'aaa', String.class)
        def bb = behavior.then(actionId).build()

        then:
        noExceptionThrown()
    }

    def 'Test get id before built'() {
        given:
        def behavior = new Behavior(Mock(Responsible), Mock(Repository), 'aaa', String.class)

        when:
        behavior.getId()

        then:
        thrown(GeneralException)
    }
}
