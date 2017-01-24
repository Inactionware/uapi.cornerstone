/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config

import spock.lang.Specification
import uapi.InvalidArgumentException

import java.util.concurrent.TimeUnit

/**
 * Unit test for IntervalTime
 */
class IntervalTimeTest extends Specification {

    def 'Test parse and toString'() {
        when:
        def intervalTime = IntervalTime.parse(timeString)

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
        '1m'        | 0     | 0     | 1             | 60                    | 60 * 1000                     | '1m'
        '1m50s'     | 0     | 0     | 1             | 110                   | 110 * 1000                    | '1m50s'
        '2m30s20ms' | 0     | 0     | 2             | 150                   | 150 * 1000 + 20               | '2m30s20ms'
        '3h'        | 0     | 3     | 180           | 180 * 60              | 180 * 60 * 1000               | '3h'
        '3h5s'      | 0     | 3     | 180           | 180 * 60 + 5          | (180 * 60 + 5) * 1000         | '3h5s'
        '1d5m'      | 1     | 24    | 24 * 60 + 5   | (24 * 60 + 5) * 60    | (24 * 60 + 5) * 60 * 1000     | '1d5m'
        '1h1d'      | 1     | 25    | 24 * 60 + 60  | (24 * 60 + 60) * 60   | (24 * 60 + 60) * 60 * 1000    | '1d1h'
        '1h1h'      | 0     | 2     | 2 * 60        | 2 * 60 * 60           | 2 * 60 * 60 * 1000            | '2h'
        '1h1ms'     | 0     | 1     | 1 * 60        | 1 * 60 * 60           | 1 * 60 * 60 * 1000 + 1        | '1h1ms'
        '1m1d'      | 1     | 24    | 24 * 60 + 1   | (24 * 60 + 1) * 60    | (24 * 60 + 1) * 60 * 1000     | '1d1m'
        '1m1h'      | 0     | 1     | 1 * 60 + 1    | (1 * 60 + 1) * 60     | (1 * 60 + 1) * 60 * 1000      | '1h1m'
        '1m1m'      | 0     | 0     | 2             | 2 * 60                | 2 * 60 * 1000                 | '2m'
        '1m1ms'     | 0     | 0     | 1             | 1 * 60                | 1 * 60 * 1000 + 1             | '1m1ms'
        '1s1d'      | 1     | 24    | 24 * 60       | 24 * 60 * 60 + 1      | (24 * 60 * 60 + 1) * 1000     | '1d1s'
        '1s1h'      | 0     | 1     | 1 * 60        | 1 * 60 * 60 + 1       | (1 * 60 * 60 + 1) * 1000      | '1h1s'
        '1s1m'      | 0     | 0     | 1             | 1 * 60 + 1            | (1 * 60 + 1) * 1000           | '1m1s'
        '1s1s'      | 0     | 0     | 0             | 2                     | 2 * 1000                      | '2s'
        '1ms1d'     | 1     | 24    | 24 * 60       | 24 * 60 * 60          | ( 24 * 60 * 60) * 1000 + 1    | '1d1ms'
        '1ms1h'     | 0     | 1     | 1 * 60        | 1 * 60 * 60           | 1 * 60 * 60 * 1000 + 1        | '1h1ms'
        '1ms1s'     | 0     | 0     | 0             | 1                     | 1 * 1000 + 1                  | '1s1ms'
        '1ms1ms'    | 0     | 0     | 0             | 0                     | 2                             | '2ms'
        '2d'        | 2     | 48    | 48 * 60       | 48 * 60 * 60          | 48 * 60 * 60 * 1000           | '2d'
    }

    def 'Test parse with incorrect unit'() {
        when:
        IntervalTime.parse(timeString)

        then:
        thrown(ex)

        where:
        timeString  | ex
        ''          | InvalidArgumentException
        null        | InvalidArgumentException
        '1b'        | InvalidArgumentException
        '1m3'       | InvalidArgumentException
        'm23'       | InvalidArgumentException
        '1.5m'      | InvalidArgumentException
    }

    def 'Test equals'() {
        given:
        def iTime1 = IntervalTime.parse(timeString1)

        expect:
        (iTime1.equals(iTime2)) == result

        where:
        timeString1 | iTime2                    | result
        '1s'        | null                      | false
        '1s'        | new Object()              | false
        '1s'        | IntervalTime.parse('1s')  | true
    }

    def 'Test hashcode'() {
        expect:
        IntervalTime.parse('1s').hashCode() != null
    }
}
