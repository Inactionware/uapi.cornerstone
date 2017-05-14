package uapi.config.internal

import spock.lang.Specification
import uapi.common.IntervalTime

/**
 * Unit tests for IntervalTimeParser
 */
class IntervalTimeParserTest extends Specification {

    def 'Test create instance'() {
        when:
        def parser = new IntervalTimeParser()

        then:
        noExceptionThrown()
        parser.name == IntervalTimeParser.canonicalName
        parser.isSupport(inType, outType) == isSupport

        where:
        inType                      | outType                       | isSupport
        String.canonicalName        | IntervalTime.canonicalName    | true
        String.canonicalName        | String.canonicalName          | false
        IntervalTime.canonicalName  | IntervalTime.canonicalName    | false
        String.canonicalName        | Integer.canonicalName         | false
        Float.canonicalName         | IntervalTime.canonicalName    | false
        Integer.canonicalName       | Float.canonicalName           | false
    }

    def 'Test parse'() {
        given:
        def parser = new IntervalTimeParser()

        when:
        def intervalTime = parser.parse(timeString)

        then:
        intervalTime.days()         == days
        intervalTime.hours()        == hours
        intervalTime.minutes()      == minutes
        intervalTime.seconds()      == seconds
        intervalTime.milliseconds() == milliseconds
        intervalTime.toString()     == toString

        where:
        timeString  | days  | hours | minutes       | seconds               | milliseconds                  | toString
        '50ms'      | 0     | 0     | 0             | 0                     | 50                            | '50ms'
        '2s'        | 0     | 0     | 0             | 2                     | 2000                          | '2s'
        '2s30ms'    | 0     | 0     | 0             | 2                     | 2030                          | '2s30ms'
        '2s900ms'   | 0     | 0     | 0             | 2                     | 2900                          | '2s900ms'

    }
}
