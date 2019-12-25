/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.service.internal;

import com.google.auto.service.AutoService;
import freemarker.template.Template;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.rx.Looper;
import uapi.service.ITagged;
import uapi.service.annotation.Tag;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Handler used to handle Tag annotation
 */
@AutoService(IAnnotationsHandler.class)
public class TagHandler extends AnnotationsHandler {

    private static final String TEMPLATE_GET_TAGS   = "template/getTags_method.ftl";
    private static final String VAR_TAGS            = "tags";

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return new Class[] { Tag.class };
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderContext,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        Looper.on(elements).foreach(classElement -> {
            if (classElement.getKind() != ElementKind.CLASS) {
                throw new GeneralException(
                        "The Tag annotation only can be applied on class - {}",
                        classElement.getSimpleName().toString());
            }
            builderContext.checkModifiers(classElement, Tag.class, Modifier.PRIVATE, Modifier.FINAL);

            var tempGetIds = builderContext.loadTemplate(Module.name, TEMPLATE_GET_TAGS);
            var tag = classElement.getAnnotation(Tag.class);
            var modelGetTags = new HashMap<String, String[]>();
            modelGetTags.put(VAR_TAGS, tag.value());

            var classBuilder = builderContext.findClassBuilder(classElement);
            var isPrototype = classBuilder.getTransience(ServiceHandler.VAR_IS_PROTOTYPE, false);
            if (isPrototype) {
                String prototypeClassName = classBuilder.getTransience(ServiceHandler.VAR_PROTOTYPE_CLASS_NAME);
                classBuilder = builderContext.findClassBuilder(prototypeClassName, false);
                if (classBuilder == null) {
                    throw new GeneralException("The prototype of service was not found in context - {}", prototypeClassName);
                }
            }
            classBuilder
                    .addImplement(ITagged.class.getCanonicalName())
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder()
                                    .setName(AnnotationMeta.OVERRIDE))
                            .setName(ITagged.METHOD_GETTAGS)
                            .addModifier(Modifier.PUBLIC)
                            .setReturnTypeName(Type.STRING_ARRAY)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setTemplate(tempGetIds)
                                    .setModel(modelGetTags)));
        });
    }
}
