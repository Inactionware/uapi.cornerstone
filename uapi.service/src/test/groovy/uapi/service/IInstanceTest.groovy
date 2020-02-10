/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service

import spock.lang.Specification

class IInstanceTest extends Specification {

    def 'Test getIds'() {
        given:
        def testinst = new TestInstance()

        when:
        def ids = testinst.getIds()

        then:
        noExceptionThrown()
        ids == ['prototypeId_b_d']
    }

    private static class TestInstance implements IInstance {

        @Override
        Map<Object, Object> attributes() {
            return ['a': 'b', 'c': 'd']
        }

        @Override
        String prototypeId() {
            return 'prototypeId'
        }

        @Override
        def <T> T get(Object key) {
            return null
        }

        @Override
        def <T> T set(Object key, Object value) {
            return null
        }

        @Override
        boolean contains(Object key, Object value) {
            return false
        }

        @Override
        boolean contains(Map<Object, Object> attributes) {
            return false
        }

        @Override
        int count() {
            return 0
        }
    }
}
