/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.app.internal;

import java.util.ServiceLoader;

/**
 * The class used to load IService instance.
 * It is easy to mock it to get fake service in unit tests.
 */
public class AppServiceLoader {

    public <T> Iterable<T> load(Class<T> type) {
        return ServiceLoader.load(type);
    }
}
