/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal;

import uapi.app.IApplicationLifecycle;
import uapi.app.IApplication;
import uapi.common.IntervalTime;
import uapi.common.StringHelper;
import uapi.config.annotation.Config;
import uapi.log.ILogger;
import uapi.rx.Looper;
import uapi.service.IRegistry;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Optional;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * The UAPI application entry point
 */
@Service
public class Application implements IApplication {

    @Inject
    protected IRegistry _registry;

    @Inject
    protected ILogger _logger;

    @Inject
    @Optional
    protected List<IApplicationLifecycle> _lifecycles;

    @Config(path="app.name", optional=true)
    protected String _appName;

    private final Semaphore _semaphore;

    private AppState _state = AppState.STOPPED;

    public Application() {
        this._lifecycles = new ArrayList<>();
        this._semaphore = new Semaphore(0);
    }

    @Override
    public void startup(long startTime) {
        this._state = AppState.STARTING;
        IApplicationLifecycle appLifecycle = null;

        // Set default uncaught exception handler for threads
        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
            Application.this._logger.error(
                    "Found uncaught exception was thrown from thread [id:{}, name:{}] -> {}",
                    t.getId(), t.getName(), e)
        );

        if (StringHelper.isNullOrEmpty(this._appName)) {
            this._logger.error("app.name was not specified");
        } else {
            appLifecycle = Looper.on(this._lifecycles)
                    .filter(lifecycle -> lifecycle.getApplicationName().equals(this._appName))
                    .first(null);
            if (appLifecycle == null) {
                this._logger.warn("No IApplicationLifecycle is named - {}", this._appName);
            } else {
                appLifecycle.onStarted();
            }
        }

        long expend = System.currentTimeMillis() - startTime;
        long expendSecond = expend / IntervalTime.MS_OF_SECOND;
        long expendMs = expend - (expend / IntervalTime.MS_OF_SECOND);

        this._logger.info("System launched, expend {}.{}s", expendSecond, expendMs);
        try {
            Runtime.getRuntime().addShutdownHook(new Thread(new ShutdownHook()));
            this._state = AppState.STARTED;
            this._semaphore.acquire();
        } catch (InterruptedException e) {
            this._logger.warn("Encounter an InterruptedException when acquire the semaphore, system will exit.");
        }

        this._state = AppState.STOPPING;
        if (appLifecycle != null) {
            appLifecycle.onStopped();
        }

        this._logger.info("The system is shutdown.");
        this._state = AppState.STOPPED;
    }

    @Override
    public void shutdown() {
        this._logger.info("The system is going to shutdown by self ...");
        this._semaphore.release();
    }

    AppState state() {
        return this._state;
    }

    private final class ShutdownHook implements Runnable {

        @Override
        public void run() {
            Application.this._logger.info("The system is going to shutdown by user ...");
            Application.this._semaphore.release();
        }
    }

    enum AppState {
        STARTING, STARTED, STOPPING, STOPPED
    }
}