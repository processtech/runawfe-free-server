package ru.runa.wfe.extension.function;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class RoundLong extends Function<Long> {

    public RoundLong() {
        super(Param.required(Long.class));
    }

    @Override
    protected Long doExecute(Object... parameters) {
        return Math.round((double) parameters[0]);
    }

}
