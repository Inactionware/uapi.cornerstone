/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal

import java.util.concurrent.TimeUnit

import static org.awaitility.Awaitility.*
import spock.lang.Specification
import uapi.app.IApplicationLifecycle
import uapi.log.ILogger
import uapi.service.IRegistry

/**
 * Test case for Launch
 */
class ApplicationTest extends Specification {

    def Application app

    def 'Test create instance'() {
        when:
        def app = new Application()

        then:
        noExceptionThrown()
        app.state() == Application.AppState.STOPPED
    }

    def 'Test start application'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry)
        def appLife = Mock(IApplicationLifecycle) {
            getApplicationName() >> 'appName'
        }

        when:
        def app = new Application()
        app._logger = logger
        app._registry = registry
        app._appName = 'appName'
        app._lifecycles.add(appLife)
        new Thread({ -> app.startup(System.currentTimeMillis()) }).start()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STARTED })
        app.shutdown()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STOPPED })

        then:
        noExceptionThrown()
        1 * appLife.onStarted()
        1 * appLife.onStopped()
    }

    def 'Test start no named application'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry)
        def appLife = Mock(IApplicationLifecycle) {
            getApplicationName() >> 'appName'
        }

        when:
        def app = new Application()
        app._logger = logger
        app._registry = registry
        app._lifecycles.add(appLife)
        new Thread({ -> app.startup(System.currentTimeMillis()) }).start()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STARTED })
        app.shutdown()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STOPPED })

        then:
        noExceptionThrown()
        0 * appLife.onStarted()
        0 * appLife.onStopped()
    }

    def 'Test start application with no matched application lifecycle'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry)
        def appLife = Mock(IApplicationLifecycle) {
            getApplicationName() >> 'appName'
        }

        when:
        def app = new Application()
        app._logger = logger
        app._registry = registry
        app._lifecycles.add(appLife)
        app._appName = 'test'
        new Thread({ -> app.startup(System.currentTimeMillis()) }).start()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STARTED })
        app.shutdown()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STOPPED })

        then:
        noExceptionThrown()
        0 * appLife.onStarted()
        0 * appLife.onStopped()
    }

    def 'Test shutdown application by InterruptedException'() {
        given:
        def logger = Mock(ILogger)
        def registry = Mock(IRegistry)
        def appLife = Mock(IApplicationLifecycle) {
            getApplicationName() >> 'appName'
        }

        when:
        def app = new Application()
        app._logger = logger
        app._registry = registry
        app._lifecycles.add(appLife)
        app._appName = 'appName'
        def thread = new Thread({ -> app.startup(System.currentTimeMillis()) })
        thread.start()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STARTED })
        thread.interrupt()
        await().atMost(5, TimeUnit.SECONDS).until({ -> app.state() == Application.AppState.STOPPED })

        then:
        noExceptionThrown()
        1 * appLife.onStarted()
        1 * appLife.onStopped()
    }
}
