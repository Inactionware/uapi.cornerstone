/*
 * Copyright (c) 2019. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product.
 */

package uapi.service;

import uapi.IModulePortal;
import uapi.rx.Looper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;

/**
 * The interface is used to load services from current module.
 */
public interface IServiceModulePortal extends IModulePortal {

    String SERVICE_FILE_NAME = "META-INF/uapi/services";

    /**
     * Load all service from this module
     *
     * @return  All services
     */
    default IService[] loadService() {
        File svcFile = new File(SERVICE_FILE_NAME);
        if (! svcFile.exists()) {
            return new IService[0];
        }
        ArrayList<String> svcNames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(SERVICE_FILE_NAME))) {
            String line = br.readLine();
            while (line != null) {
                svcNames.add(br.readLine());
                line = br.readLine();
            }
        } catch (IOException ex) {
            throw ServiceException.builder()
                    .errorCode(ServiceErrors.LOAD_MODULE_SERVICE_FILE_FAILED)
                    .variables(new ServiceErrors.LoadModuleServiceFileFailed()
                            .moduleName(moduleName()))
                    .build();
        }
        if (svcNames.size() == 0) {
            return new IService[0];
        }
        Module module = this.getClass().getModule();
        return Looper.on(svcNames).map(svcName -> {
            Class<?> svcType = Class.forName(module, svcName);
            Object svcObj;
            try {
                Constructor<?> svcCons = svcType.getConstructor();
                svcObj = svcCons.newInstance();
            } catch (NoSuchMethodException ex) {
                throw ServiceException.builder()
                        .cause(ex)
                        .errorCode(ServiceErrors.NO_DEFAULT_CONSTRUCTOR_IN_SERVICE)
                        .variables(new ServiceErrors.NoDefaultConstructorInService()
                                .serviceName(svcName)
                                .moduleName(module.getName()))
                        .build();
            } catch (Exception ex) {
                throw ServiceException.builder()
                        .cause(ex)
                        .errorCode(ServiceErrors.CREATE_SERVICE_FAILED)
                        .variables(new ServiceErrors.CreateServiceFailed()
                                .serviceName(svcName)
                                .moduleName(module.getName()))
                        .build();
            }
            if (! (svcObj instanceof IService)) {
                throw ServiceException.builder()
                        .errorCode(ServiceErrors.SERVICE_IS_NOT_ISERVICE_TYPE)
                        .variables(new ServiceErrors.ServiceIsNotIServiceType()
                                .serviceName(svcName)
                                .moduleName(module.getName()))
                        .build();
            }
            return (IService) svcObj;
        }).toArray(new IService[0]);
    }
}
