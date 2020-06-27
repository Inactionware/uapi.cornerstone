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
import uapi.service.annotation.helper.ITagHandlerHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * Handler used to handle Tag annotation
 */
@AutoService(IAnnotationsHandler.class)
public class TagHandler extends AnnotationsHandler {

    private static final String TEMPLATE_GET_TAGS   = "template/getTags_method.ftl";
    private static final String MODEL_GET_TAGS      = "getTags";
    private static final String VAR_TAGS            = "tags";

    private final TagHandlerHelper _helper = new TagHandlerHelper();

    @SuppressWarnings("unchecked")
    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return new Class[] { Tag.class };
    }

    @Override
    public IHandlerHelper getHelper() {
        return this._helper;
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

            var classBuilder = builderContext.findClassBuilder(classElement);
            var tag = classElement.getAnnotation(Tag.class);
            this._helper.addTags(classBuilder, tag.value());
            constructTagService(builderContext, classBuilder);
        });
    }

    private void constructTagService(IBuilderContext builderContext, ClassMeta.Builder classBuilder) {
        var modelGetTags = classBuilder.createTransienceIfAbsent(MODEL_GET_TAGS, HashMap::new);
        var tempGetIds = builderContext.loadTemplate(Module.name, TEMPLATE_GET_TAGS);
        // Only prototype service needs enable tag
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
    }

    private final class TagHandlerHelper implements ITagHandlerHelper {

        public void addTags(
                final ClassMeta.Builder classBuilder,
                final String... tagNames
        ) {
            var modelGetTags = classBuilder.createTransienceIfAbsent(MODEL_GET_TAGS, HashMap::new);
            var tags = (String[]) modelGetTags.get(VAR_TAGS);
            if (tags == null) {
                tags = tagNames;
            } else {
                List<String> tagLists = new ArrayList<>();
                Looper.on(tagNames).foreach(tagLists::add);
                Looper.on(tags).filter(tag -> ! tagLists.contains(tag)).foreach(tagLists::add);
                tags = tagLists.toArray(new String[0]);
            }
            modelGetTags.put(VAR_TAGS, tags);
        }

        @Override
        public void setTags(
                final IBuilderContext builderCtx,
                final ClassMeta.Builder classBuilder,
                final String... tagNames
        ) {
            addTags(classBuilder, tagNames);
            constructTagService(builderCtx, classBuilder);
        }
    }
}
