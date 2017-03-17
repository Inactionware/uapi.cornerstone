/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package perf.registry

import spock.lang.Ignore
import spock.lang.Specification
import uapi.rx.Looper
import uapi.service.IService
import uapi.service.internal.Registry

/**
 * Performance test on simple service by single thread
 */
@Ignore
class SimpleServiceBySingleThread extends Specification {

    def static writer

    def setupSpec() {
        def file = new File('perf/simpleServiceBySingleThread.txt')
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
        when:
        System.out.println("start")

        then:
        Looper.on(uapi.common.Range.from(1).to(loopCount))
                .foreach({time ->
            def registry = new Registry()
            def services = [] as List<IService>
            Looper.on(uapi.common.Range.from(svcIdFrom).to(svcIdTo))
                    .foreach({id -> services.add(Mock(IService) {
                getIds() >> [ "svc_${id}" ]
            })})

            def startTime = System.currentTimeMillis()
            Looper.on(services).foreach({service -> registry.register(service)})

            Looper.on(uapi.common.Range.from(svcIdFrom).to(svcIdTo))
                    .map({id -> registry.findService("svc_${id}")})
                    .filter({svc -> svc != null})
                    .toList().size() == svcIdTo - svcIdFrom + 1
            def endTime = System.currentTimeMillis()
            writer.println("${endTime - startTime}ms")
            System.out.println("${endTime - startTime}")
        })


        where:
        svcIdFrom   | svcIdTo   | loopCount
        1           | 1000      | 10
    }
}
