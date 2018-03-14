package ru.runa.wfe.extension.function;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class RoundUpLong extends Function<Long> {

    public RoundUpLong() {
        super(Param.required(Long.class));
    }

    @Override
    protected Long doExecute(Object... parameters) {
        double doubleValue = (Double) parameters[0];
        return (long) doubleValue + (doubleValue == (long) doubleValue ? 0 : 1);
    }

}
