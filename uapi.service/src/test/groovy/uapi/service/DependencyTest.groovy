/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service

import spock.lang.Specification

/**
 * Unit test for Dependency
 */
class DependencyTest extends Specification {

    def 'Test create instance'() {
        when:
        new Dependency(qid, type)
        new Dependency(qid, type, single, optional)

        then:
        noExceptionThrown()

        where:
        qid     | type          | single    | optional
        'a@b'   | String.class  | true      | false
    }

    def 'Test create instance with properties'() {
        given:
        def dependency = new Dependency(qid, type)

        when:
        dependency.single = single
        dependency.optional = optional

        then:
        dependency.serviceId == qsvcId
        dependency.serviceType == type
        dependency.single == single
        dependency.optional == optional

        where:
        qid     | type          | single    | optional  | qsvcId
        'a@b'   | String.class  | true      | false     | QualifiedServiceId.splitTo('a@b')
    }

    def 'Test get hash code'() {
        when:
        def dependency = new Dependency(qid, type)

        then:
        dependency.hashCode() != null

        where:
        qid     | type
        'a@b'   | String.class
    }

    def 'Test equals'() {
        given:
        def dependency1 = new Dependency(qid1, type)
        def dependency2 = new Dependency(qid2, type)

        when:
        dependency1.single = single
        dependency1.optional = optional

        then:
        dependency1 == dependency2

        where:
        qid1    | qid2  | type          | single    | optional  | qsvcId
        'a@b'   | 'a@b' | String.class  | true      | false     | QualifiedServiceId.splitTo('a@b')
    }

    def 'Test to string'() {
        when:
        def dependency = new Dependency(qid, type)

        then:
        dependency.toString() == string

        where:
        qid     | type          | string
        'a@b'   | String.class  | 'Dependency[a@b]'
    }
}
