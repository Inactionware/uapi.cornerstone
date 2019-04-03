/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal

import spock.lang.Specification
import uapi.behavior.ActionOutputMeta

class ActionOutputHolderTest extends Specification {

    def 'Test create instance'() {
        when:
        def outMetas = new ActionOutputMeta[1]
        outMetas[0] = new ActionOutputMeta(String.class, 'name')
        def outData = [ 'aaa' ] as String[]
        def holder = new ActionOutputHolder(outMetas, outData)

        then:
        holder != null
        holder.getData('name') == 'aaa'
        holder.getData(0) == 'aaa'
    }
}
