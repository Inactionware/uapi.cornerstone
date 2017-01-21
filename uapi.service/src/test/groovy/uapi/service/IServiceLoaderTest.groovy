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
 * Unit test for IServiceLoader
 */
class IServiceLoaderTest extends Specification {

    def 'Test compare to'() {
        when:
        def svcLoader1 = new ServiceLoader()
        svcLoader1.setPriority(p1)
        def svcLoader2 = new ServiceLoader()
        svcLoader2.setPriority(p2)

        then:
        svcLoader1.compareTo(svcLoader2) == result

        where:
        p1      | p2    | result
        1       | 2     | -1
        2       | 1     | 1
        3       | 3     | 0
    }


    private class ServiceLoader implements IServiceLoader {

        private priority = 0;

        void setPriority(int i) {
            this.priority = i;
        }

        @Override
        int getPriority() {
            return this.priority
        }

        @Override
        def <T> T load(String serviceId, Class<?> serviceType) {
            return null
        }

        @Override
        String getId() {
            return null
        }
    }
}
