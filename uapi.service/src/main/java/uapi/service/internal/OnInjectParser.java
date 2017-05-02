package uapi.service.internal;

import uapi.codegen.ClassMeta;
import uapi.codegen.IBuilderContext;
import uapi.rx.Looper;

import javax.lang.model.element.Element;
import java.util.Set;

/**
 * The parser is used to parse OnInject annotation
 */
public class OnInjectParser {

    public void parse(
            final IBuilderContext builderContext,
            final Set<? extends Element> elements
    ) {

    }

    public void addInjectMethodIfAbsent(
            final IBuilderContext builderCtx,
            final Set<? extends Element> elements
    ) {
        Looper.on(elements).foreach(element -> {
            Element classElemt = element.getEnclosingElement();
            ClassMeta.Builder clsBuilder = builderCtx.findClassBuilder(classElemt);
//            this._helper.addActivateMethod(builderCtx, clsBuilder);
        });
    }
}
