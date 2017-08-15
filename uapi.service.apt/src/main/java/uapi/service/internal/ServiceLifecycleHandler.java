package uapi.service.internal;

import com.google.auto.service.AutoService;
import uapi.GeneralException;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.service.IServiceLifecycleHandlerHelper;
import uapi.service.annotation.OnActivate;
import uapi.service.annotation.OnInject;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * A handler is used for handling service lifecycle related annotations
 */
@AutoService(IAnnotationsHandler.class)
public class ServiceLifecycleHandler extends AnnotationsHandler {

    @SuppressWarnings("unchecked")
    private final Class<? extends Annotation>[] orderedAnnotations =
            new Class[] { OnActivate.class, OnInject.class, uapi.service.annotation.OnDeactivate.class };

    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return orderedAnnotations;
    }

    private OnActivateParser    _onActivateParser;
    private OnInjectParser      _onInjectParser;
    private OnDeactivateParser _onDeactivateParser;

    private ServiceLifecycleHandlerHelper _handlerHelper;

    public ServiceLifecycleHandler() {
        this._onActivateParser  = new OnActivateParser();
        this._onInjectParser    = new OnInjectParser();
        this._onDeactivateParser = new OnDeactivateParser();
        this._handlerHelper     = new ServiceLifecycleHandlerHelper();
    }

    @Override
    public IHandlerHelper getHelper() {
        return this._handlerHelper;
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderContext,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        ArgumentChecker.notNull(annotationType, "annotationType");

        if (annotationType.equals(OnActivate.class)) {
            this._onActivateParser.parse(builderContext, elements);
        } else if (annotationType.equals(OnInject.class)) {
            this._onInjectParser.parse(builderContext, elements);
        } else if (annotationType.equals(uapi.service.annotation.OnDeactivate.class)) {
            this._onDeactivateParser.parse(builderContext, elements);
        } else {
            throw new GeneralException("Unsupported annotation - {}", annotationType.getName());
        }

        // Ensure all classes should implement all methods which are defined in IServiceLifecycle interface
        this._onActivateParser.addOnActivateMethodIfAbsent(builderContext, elements);
        this._onInjectParser.addInjectMethodIfAbsent(builderContext, elements);
        this._onDeactivateParser.addOnDeactivateMethodIfAbsent(builderContext, elements);
    }

    private class ServiceLifecycleHandlerHelper implements IServiceLifecycleHandlerHelper {

        @Override
        public void addActivateMethod(
                final IBuilderContext builderContext,
                final ClassMeta.Builder classBuilder,
                final String... methodNames
        ) {
            ServiceLifecycleHandler.this._onActivateParser.getHelper()
                    .addActivateMethod(builderContext, classBuilder, methodNames);
        }

        @Override
        public void addInjectMethod(
                final IBuilderContext builderContext,
                final ClassMeta.Builder classBuilder,
                final String methodName,
                final String serviceId,
                final String serviceType) {
            ServiceLifecycleHandler.this._onInjectParser.getHelper()
                    .addInjectMethod(builderContext, classBuilder, methodName, serviceId, serviceType);
        }

        @Override
        public void addDeactivateMethod(
                final IBuilderContext builderContext,
                final ClassMeta.Builder classBuilder,
                final String... methodNames) {
            ServiceLifecycleHandler.this._onDeactivateParser.getHelper()
                    .addDeactivateMethod(builderContext, classBuilder, methodNames);
        }
    }
}
