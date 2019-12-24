package uapi.service.internal;

import com.google.auto.service.AutoService;
import uapi.GeneralException;
import uapi.codegen.AnnotationsHandler;
import uapi.codegen.IAnnotationsHandler;
import uapi.codegen.IBuilderContext;
import uapi.service.annotation.ModuleServiceLoader;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

@AutoService(IAnnotationsHandler.class)
public class ModuleServiceLoaderHandler extends AnnotationsHandler {
    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return new Class[] { ModuleServiceLoader.class };
    }

    @Override
    protected void handleAnnotatedElements(
            IBuilderContext builderContext,
            Class<? extends Annotation> annotationType,
            Set<? extends Element> elements) throws GeneralException {
        if (annotationType != ModuleServiceLoader.class) {
            throw new GeneralException("Unsupported annotation - {}", annotationType);
        }
    }
}
