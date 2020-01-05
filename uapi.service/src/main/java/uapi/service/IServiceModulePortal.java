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
import uapi.IModulePortal;
import uapi.rx.Looper;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

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
    default Iterable<IService> loadService() {
        List<IService> services = new ArrayList<>();
        Module module = this.getClass().getModule();
//        var path = module.getResourceAsStream(SERVICE_FILE_NAME);
//        if (path == null) {
//            throw new GeneralException(
//                    "The path {} in module {} does not exist.", SERVICE_FILE_NAME, this.getClass().getModule().getName());
//        }
//        File svcFile = new File(path.getFile());
//        if (! svcFile.exists()) {
//            return services;
//        }
        ArrayList<String> svcNames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(module.getResourceAsStream(SERVICE_FILE_NAME)))) {
            String line = br.readLine();
            while (line != null) {
                if (line.isBlank()) {
                    continue;
                }
                svcNames.add(line);
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
            return services;
        }

        return Looper.on(svcNames).map(svcName -> {
            Object svcObj;
            try {
                Class<?> svcType = module.getClassLoader().loadClass(svcName);
                Constructor<?> svcCons = svcType.getConstructor();
                svcObj = svcCons.newInstance();
            } catch (ClassNotFoundException ex) {
                throw new GeneralException(ex);
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
        }).toList(services);
    }
}
