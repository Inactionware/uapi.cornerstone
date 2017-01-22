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
import uapi.config.Configuration

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
        config.getValue("a") == "value a"
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
}
