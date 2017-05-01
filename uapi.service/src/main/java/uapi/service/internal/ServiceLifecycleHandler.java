package uapi.service.internal;

import uapi.GeneralException;
import uapi.codegen.AnnotationsHandler;
import uapi.codegen.IBuilderContext;
import uapi.service.annotation.OnInit;
import uapi.service.annotation.OnInject;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Created by min on 2017/5/1.
 */
public class ServiceLifecycleHandler extends AnnotationsHandler {

    @SuppressWarnings("unchecked")
    private final Class<? extends Annotation>[] orderedAnnotations =
            new Class[] { OnInit.class, OnInject.class};

    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return orderedAnnotations;
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext iBuilderContext,
            final Class<? extends Annotation> aClass,
            final Set<? extends Element> set
    ) throws GeneralException {

    }
}
