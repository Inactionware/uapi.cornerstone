package uapi.behavior;

import uapi.codegen.IHandlerHelper;

import javax.lang.model.element.Element;

public interface IActionHandlerHelper extends IHandlerHelper {

    String name = "ActionHelper";

    @Override
    default String getName() {
        return name;
    }

    ActionMethodMeta parseActionMethod(final Element classElement);

    final class ActionMethodMeta {

        private final String _inType;
        private final String _outType;
        private final String _methodName;
        private final boolean _needContext;

        public ActionMethodMeta(
                final String inType,
                final String outType,
                final String methodName,
                final boolean needContext
        ) {
            this._inType = inType;
            this._outType = outType;
            this._methodName = methodName;
            this._needContext = needContext;
        }

        public String inputType() {
            return this._inType;
        }

        public String outputType() {
            return this._outType;
        }

        public String methodName() {
            return this._methodName;
        }

        public boolean needContext() {
            return this._needContext;
        }
    }
}
