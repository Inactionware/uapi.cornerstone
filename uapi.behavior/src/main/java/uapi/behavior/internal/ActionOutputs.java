/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior.internal;

import uapi.behavior.ActionOutput;
import uapi.common.ArgumentChecker;
import uapi.common.Attributed;
import uapi.rx.Looper;

public class ActionOutputs extends Attributed {

    public ActionOutputs(final ActionOutput[] outputs) {
        ArgumentChecker.required(outputs, "outputs");
        Looper.on(outputs).foreach(output -> set(output.meta().name(), output.get()));
    }
}
