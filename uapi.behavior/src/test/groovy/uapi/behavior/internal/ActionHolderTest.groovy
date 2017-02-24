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
import uapi.GeneralException
import uapi.InvalidArgumentException
import uapi.behavior.ActionIdentify
import uapi.behavior.BehaviorException
import uapi.behavior.IAction
import uapi.behavior.IExecutionContext
import uapi.common.Functionals
import uapi.common.IAttributed

/**
 * Unit test for ActionHolder
 */
class ActionHolderTest extends Specification {

    def 'Test create instance'() {
        when:
        def action = Mock(IAction)
        def actionHolder = new ActionHolder(action)

        then:
        noExceptionThrown()
        actionHolder.action() == action
        ! actionHolder.hasNext()
    }

    def 'Test create instance with evaluator'() {
        when:
        def action = Mock(IAction)
        def actionHolder = new ActionHolder(action, Mock(Functionals.Evaluator))

        then:
        noExceptionThrown()
        actionHolder.action() == action
        ! actionHolder.hasNext()
    }

    def 'Test set next action by unmatched input type'() {
        when:
        def instance = new ActionHolder(new TestAction1())
        instance.next(new TestAction2())

        then:
        thrown(BehaviorException)
        ! instance.hasNext()
    }

    def 'Test set next action'() {
        when:
        def instance = new ActionHolder(new TestAction1())
        instance.next(new TestAction3())

        then:
        noExceptionThrown()
        instance.hasNext()
    }

    def 'Test find next action but no next action'() {
        when:
        def instance = new ActionHolder(new TestAction1())
        instance.findNext(new Object())

        then:
        thrown(GeneralException)
    }

    def 'Test find next action with one next action'() {
        when:
        def instance = new ActionHolder(new TestAction1())
        def nextAction = new TestAction3()
        instance.next(nextAction)
        ActionHolder next = instance.findNext(new Object())

        then:
        noExceptionThrown()
        next != null
        next.action() == nextAction
    }

    def 'Test find next action with more next action'() {
        when:
        def instance = new ActionHolder(new TestAction1())
        def nextAction1 = new TestAction3()
        def nextAction2 = new TestAction4()
        instance.next(nextAction1, Mock(Functionals.Evaluator) {
            accept(_ as IAttributed) >> false
        })
        instance.next(nextAction2, Mock(Functionals.Evaluator) {
            accept(_ as IAttributed) >> true
        })
        ActionHolder next = instance.findNext(Mock(IAttributed))

        then:
        noExceptionThrown()
        next != null
        next.action() == nextAction2
    }

    class TestAction1 implements IAction<Void, String> {

        @Override
        String process(Void input, IExecutionContext context) {
            return null
        }

        @Override
        Class<Void> inputType() {
            return Void.class
        }

        @Override
        Class<String> outputType() {
            return String.class
        }

        @Override
        ActionIdentify getId() {
            return ActionIdentify.parse('1@ACTION')
        }
    }

    class TestAction2 implements IAction<Integer, Void> {

        @Override
        Void process(Integer input, IExecutionContext context) {
            return null
        }

        @Override
        Class<Integer> inputType() {
            return Integer.class
        }

        @Override
        Class<Void> outputType() {
            return Void.class
        }

        @Override
        ActionIdentify getId() {
            return ActionIdentify.parse('1@ACTION')
        }
    }

    class TestAction3 implements IAction<String, Void> {

        @Override
        Void process(String input, IExecutionContext context) {
            return null
        }

        @Override
        Class<String> inputType() {
            return String.class
        }

        @Override
        Class<Void> outputType() {
            return Void.class
        }

        @Override
        ActionIdentify getId() {
            return ActionIdentify.parse('1@ACTION')
        }
    }

    class TestAction4 implements IAction<String, Void> {

        @Override
        Void process(String input, IExecutionContext context) {
            return null
        }

        @Override
        Class<String> inputType() {
            return String.class
        }

        @Override
        Class<Void> outputType() {
            return Void.class
        }

        @Override
        ActionIdentify getId() {
            return ActionIdentify.parse('1@ACTION')
        }
    }
}
