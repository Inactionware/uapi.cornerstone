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
import uapi.behavior.ActionType
import uapi.behavior.BehaviorErrors
import uapi.behavior.BehaviorException
import uapi.behavior.IAction
import uapi.behavior.IAnonymousAction
import uapi.behavior.IAnonymousCall
import uapi.common.IAttributed
import uapi.common.IDependent
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

    def 'Test create and build instance'() {
        given:
        def actionId = new ActionIdentify('name', ActionType.ACTION)
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IAction) {
                getId() >> actionId
                inputType() >> actionInputType
                outputType() >> actionOutputType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, behaviorName, behaviorInputType)
        def bb = behavior.traceable(true).then(actionId).build()

        then:
        noExceptionThrown()
        bb.id == new ActionIdentify('aaa', ActionType.BEHAVIOR)
        bb.inputType() == behaviorInputType
        bb.outputType() == behaviorOutputType
        bb.traceable()
        ((Behavior) bb).newExecution() != null
        ((Behavior) bb).entranceAction() != null

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'aaa'           | String.class          | Integer.class         | String.class      | Integer.class
    }

    def 'Test validation on incorrect behavior and first action input type mismatch'() {
        given:
        def actionId = new ActionIdentify('name', ActionType.ACTION)
        def repo = Mock(Repository) {
            get(actionId) >> Mock(IAction) {
                getId() >> actionId
                inputType() >> actionInputType
                outputType() >> actionOutputType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, behaviorName, behaviorInputType)
        behavior.then(actionId).build()

        then:
        thrown(BehaviorException)

        where:
        behaviorName    | behaviorInputType     | behaviorOutputType    | actionInputType   | actionOutputType
        'aaa'           | String.class          | Integer.class         | Integer.class     | Integer.class
    }

    def 'Test validation when evaluator is not used'() {
        when:
        def behavior = new Behavior(Mock(Responsible), Mock(Repository), behaviorName, behaviorInputType)
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
        def behavior = new Behavior(Mock(Responsible), repo, behaviorName, behaviorInput)
        behavior.then(a1).navigator().moveToStarting().then(a2).build()

        then:
        thrown(BehaviorException)

        where:
        behaviorName    | behaviorInput     | a1Name    | a1IType       | a1OType       | a2Name    | a2IType       | a2OType
        'bName'         | String.class      | 'a1'      | String.class  | Integer.class | 'a2'      | String.class  | String.class
    }

    def 'Test add dependent action'() {
        given:
        def a1 = new ActionIdentify(a1Name, ActionType.ACTION)
        def dep = new ActionIdentify(depName, ActionType.ACTION)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IDependentAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
                dependsOn() >> dep
            }
            get(dep) >> Mock(IAction) {
                getId() >> dep
                inputType() >> depIType
                outputType() >> depOType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1).build()

        then:
        noExceptionThrown()
        behavior.actionSize() == 2

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType      | depOType
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | String.class  | String.class
    }

    def 'Test add dependent action which depends on other action'() {
        given:
        def a1 = new ActionIdentify(a1Name, ActionType.ACTION)
        def dep = new ActionIdentify(depName, ActionType.ACTION)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IDependentAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
                dependsOn() >> dep
            }
            get(dep) >> Mock(IDependentAction) {
                getId() >> dep
                inputType() >> depIType
                outputType() >> depOType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1).build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.UNSUPPORTED_DEPENTENT_DEPENDENCY

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType      | depOType
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | String.class  | String.class
    }

    def 'Test add dependent action which does not exist in the repo'() {
        given:
        def a1 = new ActionIdentify(a1Name, ActionType.ACTION)
        def dep = new ActionIdentify(depName, ActionType.ACTION)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IDependentAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
                dependsOn() >> dep
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1).build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.DEPENDENT_ACTION_NOT_FOUND

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType      | depOType
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | String.class  | String.class
    }

    def 'Test add dependent action which IO does not match'() {
        given:
        def a1 = new ActionIdentify(a1Name, ActionType.ACTION)
        def dep = new ActionIdentify(depName, ActionType.ACTION)
        def repo = Mock(Repository) {
            get(a1) >> Mock(IDependentAction) {
                getId() >> a1
                inputType() >> a1IType
                outputType() >> a1OType
                dependsOn() >> dep
            }
            get(dep) >> Mock(IAction) {
                getId() >> dep
                inputType() >> depIType
                outputType() >> depOType
            }
        }

        when:
        def behavior = new Behavior(Mock(Responsible), repo, bName, bInput)
        behavior.then(a1).build()

        then:
        def ex = thrown(BehaviorException)
        ex.errorCode() == BehaviorErrors.DEPENDENT_IO_NOT_MATCH_ACTION_INPUT

        where:
        bName   | bInput        | a1Name    | a1IType       | a1OType       | depName   | depIType      | depOType
        'bName' | String.class  |'a1'       | String.class  | Integer.class |'dep'      | Integer.class | String.class
        'bName' | String.class  |'a1'       | Integer.class | Integer.class |'dep'      | String.class  | String.class
    }

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

    private interface IDependentAction extends IAction, IDependent {}
}
