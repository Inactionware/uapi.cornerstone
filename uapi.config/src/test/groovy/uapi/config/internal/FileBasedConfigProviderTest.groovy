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
import uapi.GeneralException
import uapi.config.IConfigFileParser
import uapi.config.IConfigTracer
import uapi.log.ILogger

/**
 * Test case for FileBasedConfigProvider
 */
class FileBasedConfigProviderTest extends Specification {

    def 'Test get path'() {
        given:
        def provider = new FileBasedConfigProvider()

        expect:
        provider.getPaths() == [FileBasedConfigProvider.CFG_FILE_PATH] as String[]
    }

    def 'Test init'() {
        given:
        def provider = new FileBasedConfigProvider()
        def logger = Mock(ILogger)
        provider._logger = logger

        when:
        provider.init()

        then:
        1 * logger.info(_ as String, null)
    }

    def 'Test is optional config'() {
        given:
        def provider = new FileBasedConfigProvider()

        expect:
        ! provider.isOptionalConfig(FileBasedConfigProvider.CFG_FILE_PATH)
    }

    def 'Test is optional config with unsupported config'() {
        given:
        def provider = new FileBasedConfigProvider()

        when:
        provider.isOptionalConfig('abc')

        then:
        thrown(GeneralException)
    }

    def 'Test config with unsupported config'() {
        given:
        FileBasedConfigProvider provider = new FileBasedConfigProvider()

        when:
        provider.config('abc', new Object())

        then:
        thrown(GeneralException)
    }

    def 'Test config when the config file does not exist'() {
        given:
        FileBasedConfigProvider provider = new FileBasedConfigProvider()
        provider._logger = Mock(ILogger)

        when:
        provider.config(FileBasedConfigProvider.CFG_FILE_PATH, 'abc.cfg')

        then:
        thrown(GeneralException)
    }

    def 'Test config when the config file is a directory'() {
        given:
        FileBasedConfigProvider provider = new FileBasedConfigProvider()
        provider._logger = Mock(ILogger)

        when:
        provider.config(FileBasedConfigProvider.CFG_FILE_PATH, 'src/test/resources')

        then:
        thrown(GeneralException)
    }

    def 'Test config when the config file has no extension'() {
        given:
        FileBasedConfigProvider provider = new FileBasedConfigProvider()
        provider._logger = Mock(ILogger)

        when:
        provider.config(FileBasedConfigProvider.CFG_FILE_PATH, 'src/test/resources/config')

        then:
        thrown(GeneralException)
    }

    def 'Test config when no parser to parse it'() {
        given:
        FileBasedConfigProvider provider = new FileBasedConfigProvider()
        provider._logger = Mock(ILogger)

        when:
        provider.config(FileBasedConfigProvider.CFG_FILE_PATH, 'src/test/resources/config.yml')

        then:
        thrown(GeneralException)
    }

    def 'Test config when it is empty file'() {
        given:
        def yamlParser = Mock(IConfigFileParser) {
            isSupport('yml') >> true
            parse(_) >> null
        }
        def logger = Mock(ILogger)
        FileBasedConfigProvider provider = new FileBasedConfigProvider()
        provider._logger = logger
        provider._parsers.add(yamlParser)

        when:
        provider.config(FileBasedConfigProvider.CFG_FILE_PATH, 'src/test/resources/empty_config.yml')

        then:
        noExceptionThrown()
        1 * logger.warn(_ as String, _)
    }

    def 'Test config'() {
        def cfgTracer = Mock(IConfigTracer)
        def yamlParser = Mock(IConfigFileParser) {
            isSupport('yml') >> true
            parse(_) >> ['key': 'value']
        }

        given:
        FileBasedConfigProvider provider = new FileBasedConfigProvider()
        provider._logger = Mock(ILogger)
        provider._cfgTracer = cfgTracer
        provider._parsers.add(yamlParser)

        when:
        provider.config(FileBasedConfigProvider.CFG_FILE_PATH, 'src/test/resources/config.yml')

        then:
        1 * cfgTracer.onChange(['key': 'value'])
    }
}
