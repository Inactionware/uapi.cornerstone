package uapi.event

import spock.lang.Specification

/**
 * The unit test for AttributedEvent
 */
class AttributedEventTest extends Specification {

    def 'Test create instance'() {
        when:
        new AttributedEvent('topic')

        then:
        noExceptionThrown()
    }

    def 'Test set and get attribute'() {
        given:
        def event = new AttributedEvent('topic')

        when:
        event.set(key, attr)

        then:
        event.get(key) == attr

        where:
        key     | attr
        'key'   | new Object()
    }

    def 'Test contains one attribute'() {
        given:
        def event = new AttributedEvent('topic')

        when:
        event.set(key1, attr1)

        then:
        event.contains(key2, attr2) == isContains

        where:
        key1    | attr1     | key2  | attr2     | isContains
        '1'     | '2'       | '1'   | '2'       | true
        '1'     | '2'       | '1'   | '3'       | false
        '1'     | '2'       | '2'   | '2'       | false
        '1'     | '2'       | '3'   | '4'       | false
    }

    def 'Test contains more attributes'() {
        given:
        def event = new AttributedEvent('topic')

        when:
        event.set(key1, attr1)
        event.set(key2, attr2)
        event.set(key3, attr3)

        then:
        event.contains(map) == isContains

        where:
        key1    | attr1     | key2  | attr2 | key3  | attr3 | map                               | isContains
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['1': '2']                        | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['3': '4']                        | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['5': '6']                        | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['1': '2', '3': '4']              | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['1': '2', '5': '6']              | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['3': '4', '5': '6']              | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['1': '2', '3': '4', '5': '6']    | true
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['1': '2', '2': '4']              | false
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['4': '5', '3': '4']              | false
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | ['9': '10']                       | false
        '1'     | '2'       | '3'   | '4'   | '5'   | '6'   | new HashMap()                     | true
    }
}
