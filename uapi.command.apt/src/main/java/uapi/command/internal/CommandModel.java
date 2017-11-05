package uapi.command.internal;

import java.util.ArrayList;
import java.util.List;

public class CommandModel {

    public String name;
    public String namespace;
    public String parentPath;
    public String description;

    public String userCommandClassName;
    public String executorClassName;
    public List<ParamModel> parameters = new ArrayList<>();
    public List<OptionModel> options = new ArrayList<>();

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getParentPath() {
        return this.parentPath;
    }

    public String getDescription() {
        return this.description;
    }

    public String getUserCommandClassName() {
        return this.userCommandClassName;
    }

    public String getExecutorClassName() {
        return this.executorClassName;
    }

    public List<ParamModel> getParameters() {
        return this.parameters;
    }

    public List<OptionModel> getOptions() {
        return this.options;
    }
}
