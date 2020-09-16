/*
 * Copyright (c) 2020. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.ActionIdentify
import uapi.behavior.ActionType
import uapi.behavior.IAction
import uapi.behavior.IActionMeta
import uapi.service.IRegistry
import uapi.service.ServiceType

class ActionRepositoryTest extends Specification {

    def 'Test create instance'() {
        when:
        def repo = new ActionRepository()

        then:
        noExceptionThrown()
        repo.actionCount() == 0
        repo.cachedActionCount() == 0
    }

    def 'Test add action'() {
        given:
        def repo = new ActionRepository()
        def actionMeta = Mock(IActionMeta) {
            actionId() >> new ActionIdentify(aid, ActionType.ACTION)
        }

        when:
        repo.addAction(actionMeta)

        then:
        noExceptionThrown()
        repo.actionCount() == 1
        repo.cachedActionCount() == 0

        where:
        aid | aa
        "1" | null
    }

    def 'Test inject action'() {
        given:
        def repo = new ActionRepository()
        def actionMeta = Mock(IActionMeta) {
            actionId() >> new ActionIdentify(aid, ActionType.ACTION)
        }

        when:
        repo.injectAction(actionMeta)

        then:
        noExceptionThrown()
        repo.actionCount() == 1
        repo.cachedActionCount() == 0

        where:
        aid | aa
        "1" | null
    }

    def 'Test get action'() {
        given:
        def repo = new ActionRepository()
        def actionMeta = Mock(IActionMeta) {
            actionId() >> new ActionIdentify(aid, ActionType.ACTION)
            serviceType() >> ServiceType.Singleton
        }
        repo._svcReg = Mock(IRegistry) {
            1 * findService(_, _) >> Mock(IAction) {
                getId() >> new ActionIdentify(aid, ActionType.ACTION)
            }
        }

        when:
        repo.addAction(actionMeta)

        then:
        noExceptionThrown()
        repo.actionCount() == 1
        repo.get(new ActionIdentify(aid, ActionType.ACTION)) != null
        repo.cachedActionCount() == 1

        where:
        aid | aa
        "1" | null
    }

    def 'Test get prototype action'() {
        given:
        def repo = new ActionRepository()
        def actionMeta = Mock(IActionMeta) {
            actionId() >> new ActionIdentify(aid, ActionType.ACTION)
            serviceType() >> ServiceType.Prototype
        }
        repo._svcReg = Mock(IRegistry) {
            1 * findService(_, _) >> Mock(IAction) {
                getId() >> new ActionIdentify(aid, ActionType.ACTION)
            }
        }

        when:
        repo.addAction(actionMeta)

        then:
        noExceptionThrown()
        repo.actionCount() == 1
        repo.get(new ActionIdentify(aid, ActionType.ACTION)) != null
        repo.cachedActionCount() == 0

        where:
        aid | aa
        "1" | null
    }
}
