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
import uapi.behavior.ActionInitializer
import uapi.behavior.ActionInputMeta
import uapi.behavior.ActionOutput
import uapi.behavior.ActionOutputMeta
import uapi.behavior.ActionType
import uapi.behavior.BehaviorErrors
import uapi.behavior.BehaviorException
import uapi.behavior.IAction
import uapi.behavior.IIntercepted
import uapi.behavior.IInterceptor
import uapi.common.IAttributed
import uapi.event.IEventBus

/**
 * Unit test for Behavior
 */
class BehaviorTest extends Specification {

    def 'Test create instance'() {
        given:
        def actionRepo = Mock(ActionRepository) {
            get(_ as ActionIdentify, _ as Map) >> Mock(IAction)
        }

        when:
        ActionInputMeta[] inMetas = new ActionInputMeta[1]
        inMetas[0] = new ActionInputMeta(String.class)
        new Behavior(Mock(Responsible), actionRepo, 'aaa', inMetas)

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
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> Mock(IAction) {
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
        def repo = Mock(ActionRepository) {
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
        def behavior = new Behavior(Mock(Responsible), Mock(ActionRepository), behaviorName, bInMetas)
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
        def repo = Mock(ActionRepository) {
            get(a1, null) >> Mock(IAction) {
                getId() >> a1
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
            }
            get(a2, null) >> Mock(IAction) {
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
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> Mock(IInterceptedAction) {
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
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> Mock(IInterceptedAction) {
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
        def repo = Mock(ActionRepository) {
            get(actionId, null) >> Mock(IInterceptedAction) {
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

    def 'Test process'() {
        given:
        def aId = new ActionIdentify(aName, ActionType.ACTION)
        def inMetas = new ActionInputMeta[1]
        inMetas[0] = new ActionInputMeta(aiType)
        def outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(aoType)
        def inputs = [input] as Object[]
        def outputs = new ActionOutput[1]
        outputs[0] = new ActionOutput(aId, outMetas[0])
        def exCtx = new ExecutionContext(Mock(IEventBus))
        def repo = Mock(ActionRepository) {
            get(aId, null) >> Mock(IAction) {
                getId() >> aId
                inputMetas() >> inMetas
                outputMetas() >> outMetas
                1 * process(inputs, _, exCtx)
            }
        }
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(biType)

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(aId).build().process(inputs, outputs, exCtx)

        then:
        noExceptionThrown()

        where:
        aName   | aiType        | aoType        | bName     | biType        | input
        'aName' | String.class  | Integer.class | 'bName'   | String.class  | 'A'
    }

    def 'Test set when evaluator twice'() {
        when:
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(behaviorInputType)
        def behavior = new Behavior(Mock(Responsible), Mock(ActionRepository), behaviorName, bInMetas)
        behavior.when({data -> true}).when({data -> true})

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.EVALUATOR_IS_SET

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'bName'         | String.class          | Integer.class         | Integer.class     | Integer.class
    }

    def 'Test set an un-exist action'() {
        when:
        def bInMetas = new ActionInputMeta[1]
        bInMetas[0] = new ActionInputMeta(biType)
        def behavior = new Behavior(Mock(Responsible), Mock(ActionRepository), bName, bInMetas)
        behavior.then(new ActionIdentify(aName, ActionType.ACTION))

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.ACTION_NOT_FOUND

        where:
        aName   | aiType        | aoType        | bName     | biType        | input | output
        'aName' | String.class  | Integer.class | 'bName'   | String.class  | 'A'   | 1
    }

    def 'Test move to specific label'() {
        given:
        def a1 = new ActionIdentify('a1', ActionType.ACTION)
        def a1InMetas = [ new ActionInputMeta(a1IType) ] as ActionInputMeta[]
        def a1OutMetas = [ new ActionOutputMeta(a1OType) ] as ActionOutputMeta[]
        def a2 = new ActionIdentify('a2', ActionType.ACTION)
        def a2InMetas = [ new ActionInputMeta(a2IType) ] as ActionInputMeta[]
        def a2OutMetas = [ new ActionOutputMeta(a2OType) ] as ActionOutputMeta[]
        def a3 = new ActionIdentify('a3', ActionType.ACTION)
        def a3InMetas = [ new ActionInputMeta(a3IType) ] as ActionInputMeta[]
        def a3OutMetas = [ new ActionOutputMeta(a3OType) ] as ActionOutputMeta[]
        def dataShift = Mock(IAttributed)
        def repo = Mock(ActionRepository) {
            get(a1, null) >> Mock(IAction) {
                getId() >> a1
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
                1 * process(_, _, _)
            }
            get(a2, null) >> Mock(IAction) {
                getId() >> a2
                inputMetas() >> a2InMetas
                outputMetas() >> a2OutMetas
                0 * process(_, _, _)
            }
            get(a3, null) >> Mock(IAction) {
                getId() >> a3
                inputMetas() >> a3InMetas
                outputMetas() >> a3OutMetas
                1 * process(_, _, _)
            }
        }
        def bInMetas = [ new ActionInputMeta(bInput) ] as ActionInputMeta[]
        def bInputs = ['A'] as String[]
        def exCtx = Mock(ExecutionContext) {
            behaviorInputs() >> bInputs
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(ActionInitializer.instance(a1).label('a1'))
                .then(ActionInitializer.instance(a2).label('a2'))
                .navigator().moveTo('a1').when({ data -> true }).then(a3).build()
        def o = behavior.process(bInputs, [new ActionOutput<String>(a3, new ActionOutputMeta(a3OType))] as ActionOutput[], exCtx)

        then:
        noExceptionThrown()

        where:
        bName   | bInput        | a1IType       | a1OType       | a2IType       | a2OType       | a3IType       | a3OType
        'bName' | String.class  | String.class  | Integer.class | Integer.class | String.class  | Integer.class | String.class
    }

    def 'Test duplicated action label'() {
        given:
        def a1 = new ActionIdentify('a1', ActionType.ACTION)
        def a1InMetas = [ new ActionInputMeta(a1IType) ] as ActionInputMeta[]
        def a1OutMetas = [ new ActionOutputMeta(a1OType) ] as ActionOutputMeta[]
        def a2 = new ActionIdentify('a2', ActionType.ACTION)
        def a2InMetas = [ new ActionInputMeta(a2IType) ] as ActionInputMeta[]
        def a2OutMetas = [ new ActionOutputMeta(a2OType) ] as ActionOutputMeta[]
        def repo = Mock(ActionRepository) {
            get(a1, null) >> Mock(IAction) {
                getId() >> a1
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
                0 * process(_, _, _)
            }
            get(a2, null) >> Mock(IAction) {
                getId() >> a2
                inputMetas() >> a2InMetas
                outputMetas() >> a2OutMetas
                0 * process(_, _, _)
            }
        }
        def bInMetas = [ new ActionInputMeta(bInput) ] as ActionInputMeta[]

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(ActionInitializer.instance(a1).label('a1'))
                .then(ActionInitializer.instance(a2).label('a1'))
                .build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.DUPLICATED_ACTION_LABEL

        where:
        bName   | bInput        | a1IType       | a1OType       | a2IType       | a2OType
        'bName' | String.class  | String.class  | Integer.class | Integer.class | String.class
    }

    def 'Test move to un-exist label'() {
        given:
        def a1 = new ActionIdentify('a1', ActionType.ACTION)
        def a1InMetas = [ new ActionInputMeta(a1IType) ] as ActionInputMeta[]
        def a1OutMetas = [ new ActionOutputMeta(a1OType) ] as ActionOutputMeta[]
        def a2 = new ActionIdentify('a2', ActionType.ACTION)
        def a2InMetas = [ new ActionInputMeta(a2IType) ] as ActionInputMeta[]
        def a2OutMetas = [ new ActionOutputMeta(a2OType) ] as ActionOutputMeta[]
        def repo = Mock(ActionRepository) {
            get(a1, null) >> Mock(IAction) {
                getId() >> a1
                inputMetas() >> a1InMetas
                outputMetas() >> a1OutMetas
                0 * process(_, _, _)
            }
            get(a2, null) >> Mock(IAction) {
                getId() >> a2
                inputMetas() >> a2InMetas
                outputMetas() >> a2OutMetas
                0 * process(_, _, _)
            }
        }
        def bInMetas = [ new ActionInputMeta(bInput) ] as ActionInputMeta[]

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInMetas)
        behavior.then(ActionInitializer.instance(a1).label('a1'))
                .navigator().moveTo('a3')
                .then(ActionInitializer.instance(a2).label('a1'))
                .build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.NO_ACTION_WITH_LABEL

        where:
        bName   | bInput        | a1IType       | a1OType       | a2IType       | a2OType
        'bName' | String.class  | String.class  | Integer.class | Integer.class | String.class
    }

    def 'Test anonymous action'() {
        given:
        def repo = Mock(ActionRepository)
        def bInMetas = [ new ActionInputMeta(String.class) ] as ActionInputMeta[]

        when:
        def behavior = new Behavior(Mock(Responsible), repo, 'aaa', bInMetas)
        behavior.call({str, execCtx -> 'string'})

        then:
        noExceptionThrown()
    }

    def 'Test anonymous call'() {
        given:
        def repo = Mock(ActionRepository)
        def bInMetas = [ new ActionInputMeta(String.class) ] as ActionInputMeta[]

        when:
        def behavior = new Behavior(Mock(Responsible), repo, 'aaa', bInMetas)
        behavior.call({str, execCtx -> })

        then:
        noExceptionThrown()
    }

    private interface IInterceptedAction extends IIntercepted, IAction {}
}
