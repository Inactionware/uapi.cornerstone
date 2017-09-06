package uapi.command;

/**
 * The meta information of command option
 */
public interface IOptionMeta {

    /**
     * The option name also known as long option name.
     *
     * @return  The option name
     */
    String name();

    /**
     * The short name of the option.
     *
     * @return  The short option name
     */
    String shortName();

    /**
     * The type of the option.
     *
     * @return  The option type
     */
    OptionType type();

    /**
     * The argument of the option if the option is string type.
     * It should be null if the option type is non-string type.
     *
     * @return  The option argument
     */
    String argument();

    /**
     * Description of the option.
     *
     * @return  Description of the option
     */
    String description();
}
