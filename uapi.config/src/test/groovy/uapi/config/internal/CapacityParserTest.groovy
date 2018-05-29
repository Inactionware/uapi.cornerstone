/*
 * Copyright (c) 2018. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal

import spock.lang.Specification
import uapi.common.Capacity
import uapi.common.IntervalTime

class CapacityParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new CapacityParser()

        then:
        noExceptionThrown()
        parser.name == CapacityParser.canonicalName
        parser.isSupport(inType, outType) == isSupport

        where:
        inType                      | outType                    | isSupport
        String.canonicalName        | Capacity.canonicalName     | true
        String.canonicalName        | String.canonicalName       | false
        IntervalTime.canonicalName  | IntervalTime.canonicalName | false
        String.canonicalName        | Integer.canonicalName      | false
        Float.canonicalName         | IntervalTime.canonicalName | false
        Integer.canonicalName       | Float.canonicalName        | false
    }
}
