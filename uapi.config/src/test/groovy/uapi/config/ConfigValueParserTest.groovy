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
import uapi.GeneralException

/**
 * Test case for ConfigValueParsers
 */
class ConfigValueParsersTest extends Specification {

    def 'Test find parser by type'() {
        def IConfigValueParser mockParser = Mock(IConfigValueParser)
        mockParser.isSupport(inType, outType) >> {
            return true;
        }

        given:
        ConfigValueParsers parsers = new ConfigValueParsers()
        parsers._parsers.add(mockParser)

        expect:
        parsers.findParser(inType, outType) == mockParser

        where:
        inType                  | outType
        String.canonicalName    | Integer.canonicalName
        'int'                   | Long.canonicalName
    }

    def 'Test no parser is found by type'() {
        given:
        ConfigValueParsers parsers = new ConfigValueParsers()

        when:
        parsers.findParser(inType, outType)

        then:
        thrown(GeneralException)

        where:
        inType                  | outType
        String.canonicalName    | Integer.canonicalName
        'int'                   | Long.canonicalName
    }

    def 'Test found multiple parser by type'() {
        def IConfigValueParser mockParser = Mock(IConfigValueParser)
        mockParser.isSupport(inType, outType) >> {
            return true;
        }

        given:
        ConfigValueParsers parsers = new ConfigValueParsers()
        parsers._parsers.add(mockParser)
        parsers._parsers.add(mockParser)

        when:
        parsers.findParser(inType, outType)

        then:
        thrown(GeneralException)

        where:
        inType                  | outType
        String.canonicalName    | Integer.canonicalName
        'int'                   | Long.canonicalName
    }

    def 'Test find parser by name'() {
        def IConfigValueParser mockParser = Mock(IConfigValueParser)
        mockParser.getName() >> {
            return parserName;
        }

        given:
        ConfigValueParsers parsers = new ConfigValueParsers()
        parsers._parsers.add(mockParser)

        expect:
        parsers.findParser(parserName) == mockParser

        where:
        parserName  | node
        'IntParser' | ''
    }

    def 'Test no parser is found by name'() {
        given:
        ConfigValueParsers parsers = new ConfigValueParsers()

        when:
        parsers.findParser(parserName)

        then:
        thrown(GeneralException)

        where:
        parserName  | node
        'IntParser' | ''
    }

    def 'Test found multiple parser by name'() {
        def IConfigValueParser mockParser = Mock(IConfigValueParser)
        mockParser.getName() >> {
            return parserName;
        }

        given:
        ConfigValueParsers parsers = new ConfigValueParsers()
        parsers._parsers.add(mockParser)
        parsers._parsers.add(mockParser)

        when:
        parsers.findParser(parserName)

        then:
        thrown(GeneralException)

        where:
        parserName  | node
        'IntParser' | ''
    }
}
