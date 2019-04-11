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
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionInputMeta
import uapi.behavior.ActionOutputMeta
import uapi.behavior.ActionType
import uapi.behavior.BehaviorErrors
import uapi.behavior.BehaviorException
import uapi.behavior.IAction
import uapi.behavior.IIntercepted
import uapi.behavior.IInterceptor
import uapi.common.IAttributed
import uapi.common.Repository

/**
 * Unit test for Behavior
 */
@Ignore
class BehaviorTest extends Specification {

    def 'Test create instance'() {
        when:
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] = new ActionInputMeta(String.class)
        new Behavior(Mock(Responsible), Mock(Repository), 'aaa', inMetas)

        then:
        noExceptionThrown()
    }

    def 'Test create and build instance'() {
        given:
        def actionId = new ActionIdentify('name', ActionType.ACTION)
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] = new ActionInputMeta(actionInputType)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(actionOutputType)
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IAction) {
                getId() >> actionId
                inputMetas() >> inMetas
                outputMetas() >> outMetas
            }
        }


        when:
        def behavior = new Behavior(Mock(Responsible), repo, behaviorName, inMetas)
        def bb = behavior.traceable(true).then(actionId).build()

        then:
        noExceptionThrown()
        bb.id == new ActionIdentify(behaviorName, ActionType.BEHAVIOR)
        bb.inputMetas() == inMetas
        bb.outputMetas() == outMetas
        bb.traceable()
        ((Behavior) bb).newExecution() != null
        ((Behavior) bb).headAction() != null

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'aaa'           | String.class          | Integer.class         | String.class      | Integer.class
    }

    def 'Test validation on incorrect behavior and first action input type mismatch'() {
        given:
        def actionId = new ActionIdentify('name', ActionType.ACTION)
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] = new ActionInputMeta(actionInputType)
        ActionOutputMeta[] outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(actionOutputType)
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IAction) {
                getId() >> actionId
                inputMetas() >> inMetas
                outputMetas() >> outMetas
            }
        }
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(behaviorInputType)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, behaviorName, bInMetas)
        behavior.then(actionId).build()

        then:
        thrown(BehaviorException)

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'aaa'           | String.class          | Integer.class         | Integer.class     | Integer.class
    }

    def 'Test validation when evaluator is not used'() {
        when:
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(behaviorInputType)
        def behavior = new Behavior(Mock(Responsible), Mock(Repository), behaviorName, bInMetas)
        behavior.when({data -> true}).build()

        then:
        thrown(BehaviorException)

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'bName'         | String.class          | Integer.class         | Integer.class     | Integer.class
    }

    def 'Test validation when leaf actions has different output type'() {
        given:
        def a1 = new ActionIdentify(a1Name, ActionType.ACTION)
        def a2 = new ActionIdentify(a2Name, ActionType.ACTION)
        def a1InMetas = new ActionInputMeta[1]
        a1InMetas[0] = new ActionInputMeta(a1IType)
        def a1OutMetas = new ActionOutputMeta[1]
        a1OutMetas[0] = new ActionOutputMeta(a1OType)
        def a2InMetas = new ActionInputMeta[1]
        a2InMetas[0] = new ActionInputMeta(a2IType)
        def a2OutMetas = new ActionOutputMeta[1]
        a2OutMetas[0] = new ActionOutputMeta(a2OType)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IAction) {
                getId() >> a1
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
            }
            get(a2) >> Mock(IAction) {
                getId() >> a2
                inputMetas() >> a2InMetas
                outputMetas() >> a2OutMetas
            }
        }
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(behaviorInput)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, behaviorName, bInMetas)
        behavior.then(a1).navigator().moveToHead().then(a2).build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.INCONSISTENT_LEAF_ACTIONS

        where:
        behaviorName    | behaviorInput     | a1Name    | a1IType       | a1OType       | a2Name    | a2IType       | a2OType
        'bName'         | String.class      | 'a1'      | String.class  | Integer.class | 'a2'      | String.class  | String.class
    }

    def 'Test add interceptive action'() {
        given:
        def actionId = new ActionIdentify(a1Name, ActionType.ACTION)
        def interceptorId = new ActionIdentify(depName, ActionType.ACTION)
        def interceptors = [interceptorId] as ActionIdentify[]
        def a1InMetas = new ActionInputMeta[1]
        a1InMetas[0] = new ActionInputMeta(a1IType)
        def a1OutMetas = new ActionOutputMeta[1]
        a1OutMetas[0] = new ActionOutputMeta(a1OType)
        def depInMetas = new ActionInputMeta[1]
        depInMetas[0] = new ActionInputMeta(depIType)
        def depOutMetas = new ActionOutputMeta[0]
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IInterceptedAction) {
                getId() >> actionId
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
                by() >> interceptors
            }
            get(interceptorId) >> Mock(IInterceptor) {
                getId() >> interceptorId
                inputMetas() >> depInMetas
                outputMetas() >> depOutMetas
            }
        }
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(bInput)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(actionId).build()

        then:
        noExceptionThrown()
        behavior.actionSize() == 1

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | String.class
    }

    def 'Test add interceptive action which does not exist in the repo'() {
        given:
        def actionId = new ActionIdentify(a1Name, ActionType.ACTION)
        def interceptorId = new ActionIdentify(depName, ActionType.ACTION)
        def interceptors = [interceptorId] as ActionIdentify[]
        def a1InMetas = new ActionInputMeta[1]
        a1InMetas[0] = new ActionInputMeta(a1IType)
        def a1OutMetas = new ActionOutputMeta[1]
        a1OutMetas[0] = new ActionOutputMeta(a1OType)
        def depInMetas = new ActionInputMeta[1]
        depInMetas[0] = new ActionInputMeta(depIType)
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IInterceptedAction) {
                getId() >> actionId
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
                by() >> interceptors
            }
        }
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(bInput)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(actionId).build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.INTERCEPTOR_NOT_FOUND

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType      | depOType
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | String.class  | String.class
    }

    def 'Test add interceptive action which IO does not match'() {
        given:
        def actionId = new ActionIdentify(a1Name, ActionType.ACTION)
        def interceptorId = new ActionIdentify(depName, ActionType.ACTION)
        def interceptors = [interceptorId] as ActionIdentify[]
        def a1InMetas = new ActionInputMeta[1]
        a1InMetas[0] = new ActionInputMeta(a1IType)
        def a1OutMetas = new ActionOutputMeta[1]
        a1OutMetas[0] = new ActionOutputMeta(a1OType)
        def depInMetas = new ActionInputMeta[1]
        depInMetas[0] = new ActionInputMeta(depIType)
        def depOutMetas = new ActionOutputMeta[0]
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IInterceptedAction) {
                getId() >> actionId
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
                by() >> interceptors
            }
            get(interceptorId) >> Mock(IInterceptor) {
                getId() >> interceptorId
                inputMetas() >> depInMetas
                outputMetas() >> depOutMetas
            }
        }
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(bInput)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(actionId).build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == errCode

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType      | errCode
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | Integer.class | BehaviorErrors.INCONSISTENT_INTERCEPTOR_INPUT_METAS
        'bName' | String.class  |'a1'       | Integer.class | Integer.class |'dep'      | String.class  | BehaviorErrors.AUTO_WIRE_IO_NOT_MATCH
    }
///Verified here
    def 'Test process'() {
        given:
        def aId = new ActionIdentify(aName, ActionType.ACTION)
        def repo = Mock(Repository) {
            get(aId) >> Mock(IAction) {
                getId() >> aId
                inputType() >> aiType
                outputType() >> aoType
                1 * process(input, _) >> output
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, biType)
        def o = behavior.then(aId).build().process(input, Mock(ExecutionContext))

        then:
        noExceptionThrown()
        o == output

        where:
        aName   | aiType        | aoType        | bName     | biType        | input | output
        'aName' | String.class  | Integer.class | 'bName'   | String.class  | 'A'   | 1
    }

    def 'Test set when evaluator twice'() {
        when:
        def behavior = new Behavior(Mock(Responsible), Mock(Repository), behaviorName, behaviorInputType)
        behavior.when({data -> true}).when({data -> true})

        then:
        thrown(BehaviorException)

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'bName'         | String.class          | Integer.class         | Integer.class     | Integer.class
    }

    def 'Test set an un-exist action'() {
        when:
        def behavior = new Behavior(Mock(Responsible), Mock(Repository), bName, biType)
        behavior.then(new ActionIdentify(aName, ActionType.ACTION))

        then:
        thrown(BehaviorException)

        where:
        aName   | aiType        | aoType        | bName     | biType        | input | output
        'aName' | String.class  | Integer.class | 'bName'   | String.class  | 'A'   | 1
    }

    def 'Test move to specific label'() {
        given:
        def a1 = new ActionIdentify('a1', ActionType.ACTION)
        def a2 = new ActionIdentify('a2', ActionType.ACTION)
        def a3 = new ActionIdentify('a3', ActionType.ACTION)
        def dataShift = Mock(IAttributed)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
                1 * process('A', _) >> dataShift
            }
            get(a2) >> Mock(IAction) {
                getId() >> a2
                inputType() >> a2IType
                outputType() >> a2OType
                0 * process(_, _)
            }
            get(a3) >> Mock(IAction) {
                getId() >> a3
                inputType() >> a3IType
                outputType() >> a3OType
                1 * process(dataShift, _) >> 'B'
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1, 'a1').then(a2, 'a2').navigator().moveTo('a1').when({ data -> true }).then(a3).build()
        def o = behavior.process('A', Mock(ExecutionContext))

        then:
        noExceptionThrown()
        o == 'B'

        where:
        bName   | bInput        | a1IType       | a1OType       | a2IType       | a2OType       | a3IType       | a3OType
        'bName' | String.class  | String.class  | Integer.class | Integer.class | String.class  | Integer.class | String.class
    }

    def 'Test duplicated action label'() {
        given:
        def a1 = new ActionIdentify('a1', ActionType.ACTION)
        def a2 = new ActionIdentify('a2', ActionType.ACTION)
        def a3 = new ActionIdentify('a3', ActionType.ACTION)
        def dataShift = Mock(IAttributed)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
            }
            get(a2) >> Mock(IAction) {
                getId() >> a2
                inputType() >> a2IType
                outputType() >> a2OType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1, 'a1').then(a2, 'a1').build()

        then:
        thrown(BehaviorException)

        where:
        bName   | bInput        | a1IType       | a1OType       | a2IType       | a2OType
        'bName' | String.class  | String.class  | Integer.class | Integer.class | String.class
    }

    def 'Test move to un-exist label'() {
        given:
        def a1 = new ActionIdentify('a1', ActionType.ACTION)
        def a2 = new ActionIdentify('a2', ActionType.ACTION)
        def a3 = new ActionIdentify('a3', ActionType.ACTION)
        def dataShift = Mock(IAttributed)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
            }
            get(a2) >> Mock(IAction) {
                getId() >> a2
                inputType() >> a2IType
                outputType() >> a2OType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1, 'a1').navigator().moveTo('a3').then(a2, 'a1').build()

        then:
        thrown(BehaviorException)

        where:
        bName   | bInput        | a1IType       | a1OType       | a2IType       | a2OType
        'bName' | String.class  | String.class  | Integer.class | Integer.class | String.class
    }

    def 'Test anonymous action'() {
        given:
        def repo = Mock(Repository)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, 'aaa', String.class)
        behavior.then({str, execCtx -> 'string'})

        then:
        noExceptionThrown()
    }

    def 'Test anonymous call'() {
        given:
        def repo = Mock(Repository)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, 'aaa', String.class)
        behavior.call({str, execCtx -> })

        then:
        noExceptionThrown()
    }

    private interface IInterceptedAction extends IIntercepted, IAction {}
}
