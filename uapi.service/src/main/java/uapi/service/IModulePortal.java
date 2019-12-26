/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.service;

import uapi.GeneralException;
import uapi.rx.Looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * The interface is used to load services from current module.
 */
public interface IModulePortal {

    String SERVICE_FILE_NAME = "META-INF/services";

    /**
     * Load all service from this module
     *
     * @return  All services
     */
    default IService[] loadService() throws IOException {
        File svcFile = new File(SERVICE_FILE_NAME);
        if (! svcFile.exists()) {
            return new IService[0];
        }
        ArrayList<String> svcNames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SERVICE_FILE_NAME))) {
            svcNames.add(br.readLine());
        }
        if (svcNames.size() == 0) {
            return new IService[0];
        }
        Module module = this.getClass().getModule();
        return (IService[]) Looper.on(svcNames).map(svcName -> {
            Class<?> svcType = Class.forName(module, svcName);
            Object svcObj = null;
            try {
                Constructor<?> svcCons = svcType.getConstructor();
                svcObj = svcCons.newInstance();
            } catch (NoSuchMethodException ex) {
                throw new GeneralException(ex, "The service has no default constructor defined - {}", svcName);
            } catch (Exception ex) {
                throw new GeneralException(ex, "Create instance of service failed - {}", svcName);
            }
            if (! (svcObj instanceof IService)) {
                throw new GeneralException("The service {} is not instance of IService", svcName);
            }
            return (IService) svcObj;
        }).toArray(); // Todo: enhance toArray method to support return specific array type
    }
}
