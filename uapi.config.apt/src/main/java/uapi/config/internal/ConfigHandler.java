/*
 * Copyright (C) 2017. The UAPI Authors
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at the LICENSE file.
 *
 * You must gained the permission from the authors if you want to
 * use the project into a commercial product
 */

package uapi.config.internal;

import com.google.auto.common.MoreElements;
import com.google.auto.service.AutoService;
import uapi.GeneralException;
import uapi.Type;
import uapi.codegen.*;
import uapi.common.ArgumentChecker;
import uapi.config.IConfigValueParser;
import uapi.config.IConfigurable;
import uapi.config.annotation.Config;
import uapi.rx.Looper;
import uapi.service.annotation.helper.IInjectableHandlerHelper;
import uapi.service.IRegistry;
import uapi.service.QualifiedServiceId;
import uapi.service.annotation.Inject;
import uapi.service.annotation.Service;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import java.lang.annotation.Annotation;
import java.util.*;

/**
 * The handler is used to handle Config related annotation
 */
@AutoService(IAnnotationsHandler.class)
public class ConfigHandler extends AnnotationsHandler {

    private static final String CONFIG_INFOS                = "ConfigInfos";
    private static final String FIELD_SVC_REG               = "FieldServiceRegistry";
    private static final String IS_FIELD_SVC_REG_DEFINED    = "IsFieldServiceRegistryDefined";

    private static final String TEMPLATE_GET_PATHS          = "template/getPaths_method.ftl";
    private static final String TEMPLATE_IS_OPTIONAL_CONFIG = "template/isOptionalConfig_method.ftl";
    private static final String TEMPLATE_CONFIG             = "template/config_method.ftl";

    @SuppressWarnings("unchecked")
    private static final Class<? extends Annotation>[] orderedAnnotations = new Class[] { Config.class };

    @Override
    protected Class<? extends Annotation>[] getOrderedAnnotations() {
        return orderedAnnotations;
    }

    @Override
    protected void handleAnnotatedElements(
            final IBuilderContext builderContext,
            final Class<? extends Annotation> annotationType,
            final Set<? extends Element> elements
    ) throws GeneralException {
        elements.forEach(fieldElement -> {
            if (fieldElement.getKind() != ElementKind.FIELD) {
                throw new GeneralException(
                        "The Config annotation only can be applied on field",
                        fieldElement.getSimpleName().toString());
            }
            builderContext.checkModifiers(fieldElement, Config.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);

            var classElement = fieldElement.getEnclosingElement();
            builderContext.checkModifiers(classElement, Config.class, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL);
            builderContext.checkAnnotations(classElement, Service.class);

            // Get field which is reference IRegistry instance
            var svcRegElem = builderContext.findFieldWith(classElement, IRegistry.class, Inject.class);

            var svcRegFieldName = "_registry";
            var isSvcRegFieldDefined = false;
            if (svcRegElem != null) {
                svcRegFieldName = svcRegElem.getSimpleName().toString();
                isSvcRegFieldDefined = true;
            }

            var classBuilder = builderContext.findClassBuilder(classElement);
            classBuilder.putTransience(FIELD_SVC_REG, svcRegFieldName);
            classBuilder.putTransience(IS_FIELD_SVC_REG_DEFINED, isSvcRegFieldDefined);
            List<ConfigInfo> cfgInfos = classBuilder.getTransience(CONFIG_INFOS);
            if (cfgInfos == null) {
                cfgInfos = new ArrayList<>();
                classBuilder.putTransience(CONFIG_INFOS, cfgInfos);
            }
            var cfgInfo = new ConfigInfo();
            var cfg = fieldElement.getAnnotation(Config.class);
            var cfgMirror = MoreElements.getAnnotationMirror(fieldElement, Config.class).get();
            var parserType = getTypeInAnnotation(cfgMirror, "parser");
            // The parser is set to IConfigValueParser.class means no customized parser was defined
            if (! IConfigValueParser.class.getCanonicalName().equals(parserType)) {
                cfgInfo.parserName = parserType;
            }
            cfgInfo.path = cfg.path();
            cfgInfo.optional = cfg.optional();
            cfgInfo.fieldName = fieldElement.getSimpleName().toString();
            cfgInfo.fieldType = fieldElement.asType().toString();
            cfgInfos.add(cfgInfo);
        });

        var tempGetPaths = builderContext.loadTemplate(Module.name, TEMPLATE_GET_PATHS);
        var tempIsOptionalConfig = builderContext.loadTemplate(Module.name, TEMPLATE_IS_OPTIONAL_CONFIG);
        var tempConfig = builderContext.loadTemplate(Module.name, TEMPLATE_CONFIG);

        Looper.on(builderContext.getBuilders()).foreach(classBuilder -> {
            List<ConfigInfo> configInfos = classBuilder.getTransience(CONFIG_INFOS);
            String fieldSvcReg = classBuilder.getTransience(FIELD_SVC_REG);
            if (configInfos == null) {
                return;
            }
            var tempModel = new HashMap<String, Object>();
            tempModel.put("configInfos", configInfos);
            tempModel.put("fieldSvcReg", fieldSvcReg);

            Boolean isFieldSvcRegDef = classBuilder.getTransience(IS_FIELD_SVC_REG_DEFINED);
            String fieldRegName = classBuilder.getTransience(FIELD_SVC_REG);
            if (! isFieldSvcRegDef) {
                var helper = (IInjectableHandlerHelper) builderContext.getHelper(IInjectableHandlerHelper.name);
                helper.addDependency(
                        builderContext,
                        classBuilder,
                        fieldRegName,
                        IRegistry.class.getCanonicalName(),
                        IRegistry.class.getCanonicalName(),
                        QualifiedServiceId.FROM_LOCAL,
                        false, false, null, true);
            }
            classBuilder
                    .addImplement(IConfigurable.class.getCanonicalName())
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName(IConfigurable.METHOD_GET_PATHS)
                            .setReturnTypeName(Type.STRING_ARRAY)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tempModel)
                                    .setTemplate(tempGetPaths)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName(IConfigurable.METHOD_IS_OPTIONAL_CONFIG)
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName(IConfigurable.PARAM_PATH)
                                    .setType(Type.STRING))
                            .setReturnTypeName(Type.BOOLEAN)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tempModel)
                                    .setTemplate(tempIsOptionalConfig)))
                    .addMethodBuilder(MethodMeta.builder()
                            .addAnnotationBuilder(AnnotationMeta.builder().setName(AnnotationMeta.OVERRIDE))
                            .addModifier(Modifier.PUBLIC)
                            .setName(IConfigurable.METHOD_CONFIG)
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName(IConfigurable.PARAM_PATH)
                                    .setType(Type.STRING))
                            .addParameterBuilder(ParameterMeta.builder()
                                    .setName(IConfigurable.PARAM_CONFIG_OBJECT)
                                    .setType(Type.OBJECT))
                            .setReturnTypeName(Type.VOID)
                            .addCodeBuilder(CodeMeta.builder()
                                    .setModel(tempModel)
                                    .setTemplate(tempConfig))
                    );
        });
    }

    public static class ConfigInfo {

        private String path;
        private String fieldName;
        private String fieldType;
        private boolean optional;
        private String parserName;

        public String getPath() {
            return this.path;
        }

        public String getFieldName() {
            return this.fieldName;
        }

        public String getFieldType() {
            return this.fieldType;
        }

        public boolean getOptional() {
            return this.optional;
        }

        public String getParserName() {
            return this.parserName;
        }

        public boolean hasParser() {
            return ! ArgumentChecker.isEmpty(this.parserName);
        }
    }
}
