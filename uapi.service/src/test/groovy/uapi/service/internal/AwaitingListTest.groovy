/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import spock.lang.Specification

/**
 * Unit tests for Awaiting
 */
class AwaitingListTest extends Specification {

    def 'Test create instance'() {
        when:
        new AwaitingList(10)

        then:
        noExceptionThrown()
    }

    def 'Test put item'() {
        given:
        def alist = new AwaitingList(maxSize)

        when:
        def result = alist.put(item)

        then:
        result
        alist.get(0) == item

        where:
        maxSize     | item
        1           | Mock(Object)
    }

    def 'Test put item over limitation'() {
        given:
        def alist = new AwaitingList(maxSize)
        alist.put(item)

        when:
        def result = alist.put(item)

        then:
        ! result

        where:
        maxSize     | item
        1           | Mock(Object)
    }

    def 'Test remove item'() {
        given:
        def alist = new AwaitingList(maxSize)
        alist.put(item)

        when:
        alist.remove(item)

        then:
        noExceptionThrown()

        where:
        maxSize     | item
        1           | Mock(Object)
    }

    def 'Test await on list'() {
        given:
        def alist = new AwaitingList(maxSize)

        when:
        def result = alist.await(1000)

        then:
        result

        where:
        maxSize     | item
        1           | Mock(Object)
    }

    def 'Test await on full list'() {
        given:
        def alist = new AwaitingList(maxSize)
        alist.put(item)

        when:
        def result = alist.await(1000)

        then:
        ! result

        where:
        maxSize     | item
        1           | Mock(Object)
    }

    def 'Test get iterator'() {
        given:
        def alist = new AwaitingList(maxSize)
        alist.put(item)

        when:
        def result = alist.iterator()

        then:
        result != null

        where:
        maxSize     | item
        1           | Mock(Object)
    }
}
