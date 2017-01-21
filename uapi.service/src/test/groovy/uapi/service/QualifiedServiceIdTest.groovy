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
import uapi.GeneralException
import uapi.InvalidArgumentException

/**
 * Test case for QualifiedServiceId
 */
class QualifiedServiceIdTest extends Specification {

    def 'Test create instance with incorrect argument'() {
        when:
        QualifiedServiceId.splitTo(incorrectId)

        then:
        thrown(exception)

        where:
        incorrectId | exception
        'a'         | GeneralException
        'a@b@c'     | GeneralException
        '@b'        | InvalidArgumentException
    }

    def 'Test combine'() {
        when:
        def qid = QualifiedServiceId.combine(id, from)

        then:
        qid == combined

        where:
        id      | from      | combined
        'id'    | 'Remote'  | 'id@Remote'
    }

    def 'Test get id and from'() {
        when:
        def qid = QualifiedServiceId.splitTo(combined)

        then:
        qid.id == id
        qid.from == from

        where:
        id      | from      | combined
        'id'    | 'Local'   | 'id@Local'
    }

    def 'Test isAssignTo method'() {
        expect:
        def qsId1 = QualifiedServiceId.splitTo(id1)
        def qsId2 = QualifiedServiceId.splitTo(id2)
        qsId1.isAssignTo(qsId2) == result

        where:
        id1         | id2           | result
        'a@Local'   | 'b@Local'     | false
        'a@Local'   | 'a@Local'     | true
        'a@Local'   | 'a@Any'       | true
    }

    def 'Test can from'() {
        when:
        def qsId = QualifiedServiceId.splitTo(combined)

        then:
        qsId.canFrom(from) == result

        where:
        combined    | from      | result
        'a@Local'   | 'Local'   | true
        'a@Remote'  | 'Local'   | false
        'a@Any'     | 'Remote'  | true
        'a@Any'     | 'Local'   | true
        'a@Any'     | 'AAa'     | true
    }

    def 'Test can from incorrect'() {
        when:
        def qsId = QualifiedServiceId.splitTo(combined)
        qsId.canFrom(from)

        then:
        thrown(GeneralException)

        where:
        combined    | from
        'a@b'       | 'Any'
    }

    def 'Test to string'() {
        when:
        def qsId = QualifiedServiceId.splitTo(combined)

        then:
        qsId.toString() == string

        where:
        combined    | string
        'a@b'       | 'a@b'
    }
}
