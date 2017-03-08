/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal

import spock.lang.Specification
import uapi.app.AppException
import uapi.service.IService
import uapi.service.ITagged

/**
 * Unit test for Profile
 */
class ProfileTest extends Specification {

    def 'Test Model.parser'() {
        expect:
        Profile.Model.parse(str) == model

        where:
        str         | model
        'INCLUDE'   | Profile.Model.INCLUDE
        'include'   | Profile.Model.INCLUDE
        'InClUdE'   | Profile.Model.INCLUDE
        'EXCLUDE'   | Profile.Model.EXCLUDE
        'exclude'   | Profile.Model.EXCLUDE
        'eXcLuDe'   | Profile.Model.EXCLUDE
    }

    def 'Test Model.parser with exception'() {
        when:
        Profile.Model.parse(str)

        then:
        thrown(AppException)

        where:
        str         | placeholder
        'A'         | null
        'included'  | null
        'excluded'  | null
    }

    def 'Test Matching.parser'() {
        expect:
        Profile.Matching.parse(str) == matching

        where:
        str             | matching
        'SATISFY-ALL'   | Profile.Matching.SATISFY_ALL
        'satisfy-all'   | Profile.Matching.SATISFY_ALL
        'sAtiSfy-aLL'   | Profile.Matching.SATISFY_ALL
        'SATISFY-ANY'   | Profile.Matching.SATISFY_ANY
        'satisfy-any'   | Profile.Matching.SATISFY_ANY
        'SatiSfY-AnY'   | Profile.Matching.SATISFY_ANY
    }

    def 'Test Matching.parser with exception'() {
        when:
        Profile.Matching.parse(str)

        then:
        thrown(AppException)

        where:
        str         | placeholder
        'A'         | null
        'satisfy'   | null
        'all'       | null
        'any'       | null
    }

    def 'Test isAllow'() {
        given:
        def svc = Mock(ITaggedService) {
            getTags() >> svcTags
        }
        Profile profile = new Profile(name, model, matching, cfgTags)

        expect:
        profile.isAllow(svc) == allowed

        where:
        name    | model                 | matching                      | cfgTags                   | svcTags                           | allowed
        'p1'    | Profile.Model.INCLUDE | Profile.Matching.SATISFY_ALL  | ['p1', 'p2'] as String[]  | ['p1', 'p2'] as String[]          | true
        'p1'    | Profile.Model.INCLUDE | Profile.Matching.SATISFY_ALL  | ['p1', 'p2'] as String[]  | ['p1', 'p2', 'p3'] as String[]    | true
        'p1'    | Profile.Model.INCLUDE | Profile.Matching.SATISFY_ALL  | ['p1', 'p2'] as String[]  | ['p1'] as String[]                | false
        'p1'    | Profile.Model.EXCLUDE | Profile.Matching.SATISFY_ALL  | ['p1', 'p2'] as String[]  | ['p1', 'p2'] as String[]          | false
        'p1'    | Profile.Model.EXCLUDE | Profile.Matching.SATISFY_ALL  | ['p1', 'p2'] as String[]  | ['p1', 'p2', 'p3'] as String[]    | false
        'p1'    | Profile.Model.EXCLUDE | Profile.Matching.SATISFY_ALL  | ['p1', 'p2'] as String[]  | ['p1'] as String[]                | true
        'p1'    | Profile.Model.INCLUDE | Profile.Matching.SATISFY_ANY  | ['p1', 'p2'] as String[]  | ['p1', 'p2'] as String[]          | true
        'p1'    | Profile.Model.INCLUDE | Profile.Matching.SATISFY_ANY  | ['p1', 'p2'] as String[]  | ['p1', 'p2', 'p3'] as String[]    | true
        'p1'    | Profile.Model.INCLUDE | Profile.Matching.SATISFY_ANY  | ['p1', 'p2'] as String[]  | ['p1'] as String[]                | true
        'p1'    | Profile.Model.EXCLUDE | Profile.Matching.SATISFY_ANY  | ['p1', 'p2'] as String[]  | ['p1', 'p2'] as String[]          | false
        'p1'    | Profile.Model.EXCLUDE | Profile.Matching.SATISFY_ANY  | ['p1', 'p2'] as String[]  | ['p1', 'p2', 'p3'] as String[]    | false
        'p1'    | Profile.Model.EXCLUDE | Profile.Matching.SATISFY_ANY  | ['p1', 'p2'] as String[]  | ['p1'] as String[]                | false
    }

    interface ITaggedService extends IService, ITagged {}
}
