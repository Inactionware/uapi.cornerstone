/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.behavior;

import uapi.common.ArgumentChecker;
import uapi.common.Attributed;

public class ActionResult extends Attributed {

    private static final String KEY_SUCCESS = "SUCCESS";
    private static final String KEY_CAUSE   = "CAUSE";

    public ActionResult() {
        super();
        super.set(KEY_SUCCESS, true);
    }

    public ActionResult(final boolean success) {
        super();
        super.set(KEY_SUCCESS, success);
    }

    /**
     * Create failed action result with cause.
     *
     * @param   cause
     *          The exception which cause the failed action
     */
    public ActionResult(final Exception cause) {
        this(false);
        ArgumentChecker.required(cause, "cause");
        super.set(KEY_CAUSE, cause);
    }

    @Override
    public Object set(
            final Object key,
            final Object value
    ) {
        if (KEY_SUCCESS.equals(key)) {
            throw BehaviorException.builder()
                    .errorCode(BehaviorErrors.RESERVED_RESULT_KEY)
                    .variables(new BehaviorErrors.ReservedResultKey()
                            .key((String) key))
                    .build();
        }
        return super.set(key, value);
    }

    public boolean successful() {
        return get(KEY_SUCCESS);
    }

    public boolean failed() {
        return ! (boolean) get(KEY_SUCCESS);
    }

    public Exception cause() {
        return get(KEY_CAUSE);
    }
}
