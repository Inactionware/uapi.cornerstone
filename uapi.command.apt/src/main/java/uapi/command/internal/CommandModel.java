package uapi.command.internal;

import java.util.List;

public class CommandModel {

    public String name;
    public String namespace;
    public String parentPath;
    public String description;

    public String userCommandClassName;
    public String executorClassName;
    public List<ParamModel> parameters;
    public List<OptionModel> options;
}
