package uapi.behavior.annotation.handler;

import uapi.codegen.IBuilderContext;
import uapi.codegen.IHandlerHelper;

import javax.lang.model.element.Element;

public interface IActionHandlerHelper extends IHandlerHelper {

    String name = "ActionHelper";

    @Override
    default String getName() {
        return name;
    }

    ActionMethodMeta parseActionMethod(final IBuilderContext builderContext, final Element classElement);

    final class ActionMethodMeta {

        private final String _methodName;
        private final ParameterMeta[] _paramMetas;

        public ActionMethodMeta(
                final String methodName,
                final ParameterMeta[] parameterMetas
        ) {
            this._methodName = methodName;
            this._paramMetas = parameterMetas;
        }

        public String methodName() {
            return this._methodName;
        }

        public ParameterMeta[] parameterMetas() {
            return this._paramMetas;
        }
    }

    final class ParameterMeta {

        private final ParameterType _type;
        private final int _idx;
        private final String _className;
        private final String _name;

        public static ParameterMeta newInputMeta(
                final int index,
                final String className
        ) {
            return new ParameterMeta(ParameterType.INPUT, index, className, null);
        }

        public static ParameterMeta newOutputMeta(
                final int index,
                final String name,
                final String className
        ) {
            return new ParameterMeta(ParameterType.OUTPUT, index, className, name);
        }

        public static ParameterMeta newContextMeta() {
            return new ParameterMeta(ParameterType.CONTEXT, 0, null, null);
        }

        private ParameterMeta(
                ParameterType type,
                int index,
                String className,
                String name) {
            this._type = type;
            this._idx = index;
            this._className = className;
            this._name = name;
        }

        public ParameterType getType() {
            return this._type;
        }

        public int getIndex() {
            return this._idx;
        }

        public String getClassName() {
            return this._className;
        }

        public String getName() {
            return this._name;
        }
    }

    enum ParameterType {
        INPUT, OUTPUT, CONTEXT
    }
}
