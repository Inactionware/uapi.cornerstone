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
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionInputMeta
import uapi.behavior.ActionOutputMeta
import uapi.behavior.BehaviorErrors
import uapi.behavior.BehaviorException
import uapi.behavior.IAction
import uapi.common.Functionals
import uapi.common.IAttributed

/**
 * Unit test for ActionHolder
 */
class ActionHolderTest extends Specification {

    def 'Test create instance'() {
        when:
        def action = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> ([new ActionInputMeta(String.class)] as ActionInputMeta[])
            outputMetas() >> ([new ActionOutputMeta(String.class)] as ActionOutputMeta[])
        }
        def actionHolder = new ActionHolder(action, 'label', null, Mock(Behavior), null, 'input')

        then:
        noExceptionThrown()
        actionHolder.action() == action
        ! actionHolder.hasNext()
    }

    def 'Test create instance with evaluator'() {
        when:
        def action = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> ([new ActionInputMeta(String.class)] as ActionInputMeta[])
            outputMetas() >> ([new ActionOutputMeta(String.class)] as ActionOutputMeta[])
        }
        def actionHolder = new ActionHolder(
                action, 'label', null, Mock(Behavior), Mock(Functionals.Evaluator), 'input')

        then:
        noExceptionThrown()
        actionHolder.action() == action
        ! actionHolder.hasNext()
    }

    def 'Test set next action by unmatched input type'() {
        given:
        def action1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }

        def action2 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> ([new ActionInputMeta(String.class)] as ActionInputMeta[])
            outputMetas() >> new ActionOutputMeta[0]
        }

        when:
        def instance1 = new ActionHolder(action1, 'label', Mock(Behavior))
        def instance2 = new ActionHolder(action2, 'label', instance1, Mock(Behavior), null)
        instance2.verify()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.INPUT_OUTPUT_COUNT_MISMATCH
    }

    def 'Test set next action'() {
        given:
        def action = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }

        when:
        def instance = new ActionHolder(action, 'label', Mock(Behavior))
        instance.next(new ActionHolder(action, 'label', Mock(Behavior)))

        then:
        noExceptionThrown()
        instance.hasNext()
    }

    def 'Test find next action with one next action'() {
        when:
        def action1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        def holder1 = new ActionHolder(action1, 'label1', Mock(Behavior))
        def action2 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        holder1.next(new ActionHolder(action2, 'label2', Mock(Behavior)))
        ActionHolder next = holder1.findNext(new Object())

        then:
        noExceptionThrown()
        next != null
        next.action() == action2
    }

    def 'Test find next action with more next action'() {
        when:
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }, 'label', Mock(Behavior))
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        def nextAction2 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        new ActionHolder(nextAction1, 'label2', instance, Mock(Behavior), Mock(Functionals.Evaluator) {
            accept(_ as IAttributed) >> false
        })
        new ActionHolder(nextAction2, 'label3', instance, Mock(Behavior), Mock(Functionals.Evaluator) {
            accept(_ as IAttributed) >> true
        })
        ActionHolder next = instance.findNext(Mock(IAttributed))

        then:
        noExceptionThrown()
        next != null
        next.action() == nextAction2
    }

    def 'Test find next action by default'() {
        when:
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }, 'label', Mock(Behavior))
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        def nextAction2 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        new ActionHolder(nextAction1, 'label1', instance, Mock(Behavior), null)
        new ActionHolder(nextAction2, 'label2', instance, Mock(Behavior), Mock(Functionals.Evaluator) {
            accept(_ as IAttributed) >> false
        })
        ActionHolder next = instance.findNext(Mock(IAttributed))

        then:
        noExceptionThrown()
        next != null
        next.action() == nextAction1
    }

    def 'Test find next but it has multiple default next'() {
        when:
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }, 'label', Mock(Behavior))
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        def nextAction2 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        new ActionHolder(nextAction1, 'label1', instance, Mock(Behavior), null)
        new ActionHolder(nextAction2, 'label2', instance, Mock(Behavior), null)
        ActionHolder next = instance.findNext(Mock(IAttributed))

        then:
        thrown(BehaviorException)
    }

    def 'Test find next but it has multiple non-default next'() {
        when:
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }, 'label', Mock(Behavior))
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        def nextAction2 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> new ActionOutputMeta[0]
        }
        def evaluator = Mock(Functionals.Evaluator) {
            accept(_ as IAttributed) >> true
        }
        new ActionHolder(nextAction1, 'label1', instance, Mock(Behavior), evaluator)
        new ActionHolder(nextAction2, 'label2', instance, Mock(Behavior), evaluator)
        ActionHolder next = instance.findNext(Mock(IAttributed))

        then:
        thrown(BehaviorException)
    }

    def 'Test named input ref verification'() {
        when:
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] =  new ActionInputMeta(String.class)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(String.class, 'name')
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> outMetas
        }, 'label', Mock(Behavior))
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> inMetas
            outputMetas() >> new ActionOutputMeta[0]
        }
        new ActionHolder(nextAction1, 'label1', instance, Mock(Behavior), null, Mock(Behavior.NamedOutput) {
            actionLabel() >> 'label'
            outputName() >> 'name'
        })

        then:
        noExceptionThrown()
    }

    def 'Test name input ref action does not exist'() {
        when:
        def behavior = Mock(Behavior) {
            getId() >> Mock(ActionIdentify) {
                toString() >> 'behavior_id'
            }
        }
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] =  new ActionInputMeta(String.class)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(String.class, 'name')
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> outMetas
        }, 'label', behavior)
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> inMetas
            outputMetas() >> new ActionOutputMeta[0]
        }
        def outRef = Mock(Behavior.NamedOutput) {
            actionLabel() >> 'aaa'
            outputName() >> 'name'
        }
        def instance2 = new ActionHolder(nextAction1, 'label1', instance, behavior, null, outRef)
        instance2.verify()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.REF_ACTION_NOT_EXIST_IN_BEHAVIOR
    }

    def 'Test name input ref input does not exist'() {
        when:
        def behavior = Mock(Behavior) {
            getId() >> Mock(ActionIdentify) {
                toString() >> 'behavior_id'
            }
        }
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] =  new ActionInputMeta(String.class)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(String.class, 'name')
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> outMetas
        }, 'label', behavior)
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> inMetas
            outputMetas() >> new ActionOutputMeta[0]
        }
        def instance2 = new ActionHolder(nextAction1, 'label1', instance, behavior, null, Mock(Behavior.NamedOutput) {
            actionLabel() >> 'label'
            outputName() >> 'aaa'
        })
        instance2.verify()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.REF_OUTPUT_NOT_FOUND_IN_BEHAVIOR
    }

    def 'Test indexed input ref verification'() {
        when:
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] =  new ActionInputMeta(String.class)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(String.class, 'name')
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> outMetas
        }, 'label', Mock(Behavior))
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> inMetas
            outputMetas() >> new ActionOutputMeta[0]
        }
        new ActionHolder(nextAction1, 'label1', instance, Mock(Behavior), null, Mock(Behavior.IndexedOutput) {
            actionLabel() >> 'label'
            outputIndex() >> 0
        })

        then:
        noExceptionThrown()
    }

    def 'Test indexed input ref index does not exist'() {
        when:
        def behavior = Mock(Behavior) {
            getId() >> Mock(ActionIdentify) {
                toString() >> 'behavior_id'
            }
        }
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] =  new ActionInputMeta(String.class)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(String.class, 'name')
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> outMetas
        }, 'label', behavior)
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> inMetas
            outputMetas() >> new ActionOutputMeta[0]
        }
        def instance2 = new ActionHolder(nextAction1, 'label1', instance, behavior, null, Mock(Behavior.IndexedOutput) {
            actionLabel() >> 'label'
            outputIndex() >> 1
        })
        instance2.verify()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.REF_OUTPUT_NOT_FOUND_IN_BEHAVIOR
    }

    def 'Test input ref type does not match'() {
        when:
        def behavior = Mock(Behavior) {
            getId() >> Mock(ActionIdentify) {
                toString() >> 'behavior_id'
            }
        }
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] =  new ActionInputMeta(String.class)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(Integer.class, 'name')
        def instance = new ActionHolder(Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> new ActionInputMeta[0]
            outputMetas() >> outMetas
        }, 'label', behavior)
        def nextAction1 = Mock(IAction) {
            getId() >> Mock(ActionIdentify)
            inputMetas() >> inMetas
            outputMetas() >> new ActionOutputMeta[0]
        }
        def instance2 = new ActionHolder(nextAction1, 'label1', instance, behavior, null, Mock(Behavior.IndexedOutput) {
            actionLabel() >> 'label'
            outputIndex() >> 0
        })
        instance2.verify()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.INPUT_OUTPUT_TYPE_MISMATCH
    }
}
