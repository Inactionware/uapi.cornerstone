/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal

import spock.lang.Specification
import uapi.Type

/**
 * Unit test for IntValueParser
 */
class BooleanValueParserTest extends Specification {

    def 'Test parser supported types'() {
        given:
        BooleanValueParser parser = new BooleanValueParser()

        expect:
        parser.isSupport(inType, outType)

        where:
        inType                  | outType
        String.canonicalName    | Boolean.canonicalName
        String.canonicalName    | Type.BOOLEAN
        Boolean.canonicalName   | Boolean.canonicalName
        Type.BOOLEAN            | Type.BOOLEAN
    }

    def 'Test parse string to int'() {
        given:
        BooleanValueParser parser = new BooleanValueParser()

        expect:
        parser.parse(input) == output

        where:
        input   | output
        'true'  | true
        'false' | false
        'on'    | false
        'yes'   | false
    }
}
