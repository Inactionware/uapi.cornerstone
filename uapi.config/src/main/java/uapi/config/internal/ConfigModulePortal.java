/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.config.internal;

import uapi.service.IService;
import uapi.service.IServiceModulePortal;

import java.lang.reflect.Constructor;

public class ConfigModulePortal implements IServiceModulePortal {

//    @Override
//    public IService loadService(String svcName) throws Exception {
//        Module module = this.getClass().getModule();
//        Class<?> svcType = module.getClassLoader().loadClass(svcName);
//        Constructor<?> svcCons = svcType.getConstructor();
//        Object svcObj = svcCons.newInstance();
//        return (IService) svcObj;
//    }
}
