package uapi.behavior;

/**
 * A constructor which is used to construct responsible
 */
public interface IResponsibleConstructor {

    /**
     * Return the name of responsible which is this constructor will build
     *
     * @return  The name of responsible
     */
    String name();

    /**
     * Construct specific responsible
     *
     * @param   responsible
     *          The responsible which will be build
     */
    void construct(IResponsible responsible);
}
