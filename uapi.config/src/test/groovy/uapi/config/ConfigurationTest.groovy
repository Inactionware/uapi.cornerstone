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
import uapi.service.IServiceReference
import uapi.service.QualifiedServiceId

/**
 * Test case for Configuration
 */
class ConfigurationTest extends Specification {

    def "Test get value by path"() {
        given:
        Configuration config = new Configuration()

        when:
        config.setChild("a", "value a")

        then:
        config.getKey() == Configuration.ROOT_KEY
        config.getValue("a") == "value a"
        config.getValueType() == null
        config.getChild('a').getValueType() == String.class
    }

    def "Test get value by deeper path"() {
        given:
        Configuration root = new Configuration()

        when:
        def child = root.setChild("a")
        child = child.setChild("b")
        child = child.setChild("c", "value c");

        then:
        root.getValue("a.b.c") == "value c"
        child.getValue() == "value c"
    }

    def "Test Get value by multiple values"() {
        given:
        Configuration root = new Configuration()

        when:
        def child = root.setChild("a1")
        child.setChild("b1")
        child.setChild("b2", "value b2");

        then:
        root.getValue("a1.b2") == "value b2"
        root.getValue("a1.b1") == null
    }

    def "Test Get children"() {
        given:
        Configuration root = new Configuration()

        when:
        def child = root.setChild("a1")
        child.setChild("b1", "value b1")
        child.setChild("b2", "value b2");

        then:
        root.getValue("a1") != null
        root.getValue("a1").size() == 2
        root.getValue("a1").get("b1").getValue() == "value b1"
        root.getValue("a1").get("b2").getValue() == "value b2"
    }

    def 'Test set map value'() {
        given:
        def root = new Configuration()

        when:
        root.setValue(['a': 'b', 'c': 'd', 'e': ['f': 'g']])

        then:
        root.getChild('a').getValue() == 'b'
        root.getChild('c').getValue() == 'd'
        root.getChild('e').getChild('f').getValue() == 'g'
        root.getValue('e.f') == 'g'
    }

    def 'Test bind configurable service'() {
        given:
        def root = new Configuration()
        def configurable = Mock(IConfigurable)
        def svcRef = Mock(IServiceReference) {
            getQualifiedId() >> Mock(QualifiedServiceId)
            getService() >> configurable
        }

        when:
        root.setValue('a', 'b')
        def config = root.getChild('a')
        def result = config.bindConfigurable(svcRef)

        then:
        result
        1 * configurable.config('a', 'b')
    }

    def 'Test double bind configurable service'() {
        given:
        def root = new Configuration()
        def configurable = Mock(IConfigurable)
        def svcRef = Mock(IServiceReference) {
            getQualifiedId() >> Mock(QualifiedServiceId)
            getService() >> configurable
        }

        when:
        root.setValue('a', 'b')
        def config = root.getChild('a')
        config.bindConfigurable(svcRef)
        def result = config.bindConfigurable(svcRef)

        then:
        result
        1 * configurable.config('a', 'b')
    }

    def 'Test double bind configurable service on empty value'() {
        given:
        def root = new Configuration()
        def configurable = Mock(IConfigurable) {
            isOptionalConfig('a') >> true
        }
        def svcRef = Mock(IServiceReference) {
            getQualifiedId() >> Mock(QualifiedServiceId)
            getService() >> configurable
        }

        when:
        root.setValue('a.b', 'c')
        def config = root.getChild('a')
        config.bindConfigurable(svcRef)
        def result = config.bindConfigurable(svcRef)

        then:
        result
    }
}
