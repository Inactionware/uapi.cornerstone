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
import uapi.common.ArgumentChecker;
import uapi.common.IntervalTime;

/**
 * The watcher watch (block current thread) on a specific condition until the condition is satisfied or timed out.
 */
public final class Watcher {

    private static final IntervalTime DEFAULT_TIMEOUT           = IntervalTime.parse("5s");
    private static final IntervalTime DEFAULT_POLLING_INTERVAL  = IntervalTime.parse("100ms");
    private static final IntervalTime DEFAULT_DELAY_INTERVAL    = IntervalTime.parse("0ms");
    private static final int DEFAULT_POLLING_LIMIT              = -1;

    public static Watcher on(WatcherCondition condition) {
        return new Watcher(condition);
    }

    private final WatcherCondition _condition;
    private INotifier _notifier;

    private IntervalTime _timeout           = DEFAULT_TIMEOUT;
    private int _pollingLimit               = DEFAULT_POLLING_LIMIT;
    private IntervalTime _pollingInterval   = DEFAULT_POLLING_INTERVAL;
    private IntervalTime _delayInterval     = DEFAULT_DELAY_INTERVAL;

    private Watcher(WatcherCondition condition) {
        ArgumentChecker.required(condition, "condition");
        this._condition = condition;
    }

    public Watcher notifyBy(final INotifier notifier) {
        ArgumentChecker.required(notifier, "notifier");
        this._notifier = notifier;
        return this;
    }

    public Watcher timeout(String timeout) {
        this._timeout = IntervalTime.parse(timeout);
        return this;
    }

    public Watcher timeout(IntervalTime timeout) {
        ArgumentChecker.required(timeout, "timeout");
        this._timeout = timeout;
        return this;
    }

    public Watcher pollingLimit(int limit) {
        this._pollingLimit = limit;
        return this;
    }

    public Watcher pollingTime(String pollingInterval) {
        this._pollingInterval = IntervalTime.parse(pollingInterval);
        return this;
    }

    public Watcher pollingTime(IntervalTime pollingInterval) {
        ArgumentChecker.required(pollingInterval, "pollingInterval");
        this._pollingInterval = pollingInterval;
        return this;
    }

    public Watcher delayTime(String delayInterval) {
        this._delayInterval = IntervalTime.parse(delayInterval);
        return this;
    }

    public Watcher delayTime(IntervalTime delayInterval) {
        ArgumentChecker.required(delayInterval, "delayInterval");
        this._delayInterval = delayInterval;
        return this;
    }

    public void start() {
        long startTime = System.currentTimeMillis();
        if (this._condition.accept(false)) {
            return;
        }
        if (this._delayInterval.milliseconds() != 0L) {
            try {
                Thread.sleep(this._delayInterval.milliseconds());
            } catch (InterruptedException ex) {
                throw new GeneralException(ex);
            }
            if (this._condition.accept(false)) {
                return;
            }
        }

        if (this._notifier != null) {
            doNotify(startTime);
        } else {
            doPolling(startTime);
        }
    }

    private void doNotify(long startTime) {
        long restTime = System.currentTimeMillis() - startTime;
        while (restTime > 0) {
            boolean notified = this._notifier.await(restTime);
            if (this._condition.accept(notified)) {
                return;
            }
            restTime = System.currentTimeMillis() - startTime;
            if (restTime <= 0) {
                throw new GeneralException("The watcher is timed out");
            }
        }
    }

    private void doPolling(long startTime) {
        int pollingCount = 1;
        check(startTime, pollingCount);
        while (true) {
            if (this._pollingInterval.milliseconds() > 0) {
                try {
                    Thread.sleep(this._pollingInterval.milliseconds());
                } catch (InterruptedException ex) {
                    throw new GeneralException(ex);
                }
            }
            if (this._condition.accept(false)) {
                return;
            }
            pollingCount++;
            check(startTime, pollingCount);
        }
    }

    private void check(long startTime, int pollingCount) {
        long timeout = this._timeout.milliseconds();
        if (System.currentTimeMillis() - startTime >= timeout) {
            throw new GeneralException("The watcher is timed out");
        }
        if (this._pollingLimit <= 0) {
            return;
        }
        if (pollingCount >= this._pollingLimit) {
            throw new GeneralException("The watcher's polling count is reach to limit - {}", this._pollingLimit);
        }
    }

    public interface WatcherCondition {

        boolean accept(boolean isNotified);
    }
}
