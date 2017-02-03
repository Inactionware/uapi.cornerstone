/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.behavior.internal;

import uapi.GeneralException;
import uapi.behavior.*;
import uapi.common.Functionals;

/**
 * Created by xquan on 2/3/2017.
 */
public class Behavior<I, O>
        implements IBehavior<I, O>, IBehaviorBuilder {

    private ActionIdentify _actionId;
    private Class<I> _iType;
    private Class<O> _oType;

    private boolean _built = false;

    // ----------------------------------------------------
    // Methods implemented from IIdentifiable interface
    // ----------------------------------------------------

    @Override
    public String getId() {
        ensureBuilt();
        return this._actionId.getId();
    }

    // ----------------------------------------------------
    // Methods implemented from IAction interface
    // ----------------------------------------------------

    @Override
    public Class<I> inputType() {
        ensureBuilt();
        return this._iType;
    }

    @Override
    public Class<O> outputType() {
        ensureBuilt();
        return this._oType;
    }

    @Override
    public O process(I input, IExecutionContext context) {
        ensureBuilt();
        return null;
    }

    // ----------------------------------------------------
    // Methods implemented from IBehaviorBuilder interface
    // ----------------------------------------------------

    @Override
    public IBehaviorBuilder id(ActionIdentify id) {
        ensureNotBuilt();
        return null;
    }

    @Override
    public IBehaviorBuilder traceable(boolean traceable) {
        ensureNotBuilt();
        return null;
    }

    @Override
    public IBehaviorBuilder when(Functionals.Evaluator evaluator) {
        ensureNotBuilt();
        return null;
    }

    @Override
    public IBehaviorBuilder then(ActionIdentify id) {
        ensureNotBuilt();
        return null;
    }

    @Override
    public IBehaviorBuilder then(ActionIdentify id, String label) {
        ensureNotBuilt();
        return null;
    }

    @Override
    public INavigator navigator() {
        ensureNotBuilt();
        return null;
    }

    @Override
    public IBehavior build() {
        ensureNotBuilt();
        this._built = true;
        return this;
    }

    private void ensureBuilt() {
        if (! this._built) {
            throw new GeneralException("The builder is not built - {}", this);
        }
    }

    private void ensureNotBuilt() {
        if (this._built) {
            throw new GeneralException("The builder is already built - {}", this);
        }
    }
}
