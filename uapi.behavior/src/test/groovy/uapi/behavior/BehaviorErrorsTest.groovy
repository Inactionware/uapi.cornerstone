/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior

import spock.lang.Ignore
import spock.lang.Specification

/**
 * Unit test for BehaviorErrors
 */
class BehaviorErrorsTest extends Specification {

//    def 'Test get exception message'() {
//        when:
//        def ex = BehaviorException.builder()
//                .errorCode(BehaviorErrors.UNMATCHED_ACTION)
//                .variables(new BehaviorErrors.UnmatchedAction()
//                        .inputType('1')
//                        .outputType('2')
//                        .inputAction(new ActionIdentify('ia', ActionType.ACTION))
//                        .outputAction(new ActionIdentify('oa', ActionType.ACTION)))
//                .build()
//
//        then:
//        ex.getMessage() == "Unmatched output type 2 of action oa@ACTION to input type 1 of action ia@ACTION"
//    }

    @Ignore
    def 'tt'() {
        when:
        def ex = BehaviorException.builder()
                    .errorCode(BehaviorErrors.NO_ACTION_WITH_LABEL)
                    .variables(new BehaviorErrors.NoActionInBehavior()
                        .behaviorId(new ActionIdentify('tt', ActionType.ACTION)))
                    .build();

        then:
        ex.getMessage() == 'aaa'
    }
}
