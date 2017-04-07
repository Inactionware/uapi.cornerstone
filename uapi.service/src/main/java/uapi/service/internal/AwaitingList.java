/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import uapi.GeneralException;
import uapi.common.IAwaiting;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An awaiting list
 */
public class AwaitingList<T> implements IAwaiting {

    private final int _sizeLimitation;
    private final List<T> _list;

    private final Lock _lock = new ReentrantLock();
    private final Condition _condition = this._lock.newCondition();

    public AwaitingList(int maxSize) {
        this._sizeLimitation = maxSize;
        this._list = new LinkedList<>();
    }

    public boolean put(T item) {
        this._lock.lock();
        try {
            if (this._list.size() >= this._sizeLimitation) {
                return false;
            }
            this._list.add(item);
            return true;
        } finally {
            this._lock.unlock();
        }
    }

    public T get(int index) {
        return this._list.get(index);
    }

    public void remove(T item) {
        this._lock.lock();
        try {
            this._list.remove(item);
            if (this._list.size() >= this._sizeLimitation) {
                this._condition.signalAll();
            }
        } finally {
            this._lock.unlock();
        }
    }

    @Override
    public boolean await(long waitTime) {
        this._lock.lock();
        try {
            if (this._list.size() < this._sizeLimitation) {
                return true;
            } else {
                return this._condition.await(waitTime, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException ex) {
            throw new GeneralException(ex);
        } finally {
            this._lock.unlock();
        }
    }

    public Iterator<T> iterator() {
        return this._list.iterator();
    }
}
