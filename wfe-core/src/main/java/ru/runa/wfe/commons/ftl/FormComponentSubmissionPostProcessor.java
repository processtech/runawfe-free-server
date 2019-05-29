package ru.runa.wfe.commons.ftl;

/**
 * Interface allows form components have custom transformation of user input.
 *
 * @author dofs
 */
public interface FormComponentSubmissionPostProcessor {

    /**
     * Transforms input value
     *
     * @param input
     *            parsed input
     * @return transformed value
     * @throws Exception
     *             if any error occurs; message will be displayed to user
     */
    public Object postProcessValue(Object input) throws Exception;

}
