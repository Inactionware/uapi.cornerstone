/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal

import spock.lang.Specification
import uapi.rx.Looper
import uapi.service.IService

import java.util.stream.Collectors

/**
 * Performance test for Registry
 */
class RegistryPerformanceTest extends Specification {

    def Registry registry
    def static writer

    def setupSpec() {
        def file = new File('performance.txt')
        if (file.exists()) {
            file.delete()
        }
        writer = file.newPrintWriter()
    }

    def cleanupSpec() {
        if (writer != null) {
            writer.flush()
            writer.close()
        }
    }

    def 'Test register and find out simple service by single thread'() {
        given:
        def registry = new Registry()
        def services = [] as List<IService>
        Looper.on(uapi.common.Range.from(svcIdFrom).to(svcIdTo))
                .foreach({id -> services.add(Mock(IService) {
            getIds() >> [ "svc_${id}" ]
        })})

        when:
        def startTime = System.currentTimeMillis()
        Looper.on(services).foreach({service -> registry.register(service)})

        then:
        Looper.on(uapi.common.Range.from(svcIdFrom).to(svcIdTo))
                .map({id -> registry.findService("svc_${id}")})
                .filter({svc -> svc != null})
                .toList().size() == svcIdTo - svcIdFrom + 1
        def endTime = System.currentTimeMillis()
        writer.println("${endTime - startTime}ms")
        System.out.println("Time usage: ${endTime - startTime}ms")

        where:
        svcIdFrom   | svcIdTo
        1           | 1000
    }

    def 'Test register and find out simple service by multiple thread'() {
        given:
        def registry = new Registry()
        def services = [] as List<IService>
        def svcIds = []
        Looper.on(uapi.common.Range.from(svcIdFrom).to(svcIdTo))
                .next({id -> svcIds.add(id)})
                .foreach({id -> services.add(Mock(IService) {
            getIds() >> [ "svc_${id}" ]
        })})


        when:
        def startTime = System.currentTimeMillis()
        services.parallelStream()
                .forEach({service -> registry.register(service)})

        then:
        svcIds.parallelStream()
                .map({id -> registry.findService("svc_${id}")})
                .filter({svc -> svc != null})
                .collect(Collectors.toList()).size() == svcIdTo - svcIdFrom + 1
        def endTime = System.currentTimeMillis()
        writer.println("${endTime - startTime}ms")
        System.out.println("Time usage: ${endTime - startTime}ms")

        where:
        svcIdFrom   | svcIdTo
        1           | 1000
    }
}
