/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.example.service.basic;

import uapi.service.IServiceFactory;
import uapi.service.annotation.Service;
import uapi.service.annotation.Tag;

import static uapi.example.service.basic.HelloFactoryAppLifecycle.APP_NAME;

/**
 * Hello service factory
 */
@Service
@Tag(APP_NAME)
public class HelloFactory implements IServiceFactory<IHello> {

    @Override
    public IHello createService(Object serveFor) {
        return new Hello();
    }
}
