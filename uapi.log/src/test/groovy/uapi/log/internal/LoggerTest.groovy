/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.log.internal

import spock.lang.Specification

/**
 * Unit test for Logger
 */
class LoggerTest extends Specification {

    def 'Test create instance'() {
        when:
        def logger = new Logger()
        logger._serveFor = 'test'
        logger.trace('aaa')

        then:
        logger._slfLogger != null
    }

    def 'Test trace'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger

        when:
        logger.trace(msg, params)

        then:
        1 * slfLogger.trace(msg, params)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }

    def 'Test debug'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger

        when:
        logger.debug(msg, params)

        then:
        1 * slfLogger.debug(msg, params)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }

    def 'Test info'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger

        when:
        logger.info(msg, params)

        then:
        1 * slfLogger.info(msg, params)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }

    def 'Test warn'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger

        when:
        logger.warn(msg, params)

        then:
        1 * slfLogger.warn(msg, params)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }

    def 'Test warn with exception'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger
        def ex = Mock(Throwable) {
            getMessage() >> msg
        }

        when:
        logger.warn(ex)

        then:
        1 * slfLogger.warn(msg, ex)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }

//    def 'Test warn with exception and message'() {
//        given:
//        def slfLogger = Mock(org.slf4j.Logger)
//        def logger = new Logger(slfLogger)
//        def ex = Mock(Throwable) {
//            getMessage() >> msg
//        }
//
//        when:
//        logger.warn(ex)
//
//        then:
//        1 * slfLogger.warn(msg, ex)
//
//        where:
//        msg     | params
//        'a'     | ['b', 'c'] as String[]
//    }

    def 'Test error'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger

        when:
        logger.error(msg, params)

        then:
        1 * slfLogger.error(msg, params)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }

    def 'Test error with exception'() {
        given:
        def slfLogger = Mock(org.slf4j.Logger)
        def logger = new Logger()
        logger._slfLogger = slfLogger
        def ex = Mock(Throwable) {
            getMessage() >> msg
        }

        when:
        logger.error(ex)

        then:
        1 * slfLogger.error(msg, ex)

        where:
        msg     | params
        'a'     | ['b', 'c'] as String[]
    }
}
