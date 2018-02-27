package ru.runa.wfe.extension.function;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class RoundDownLong extends Function<Long> {

    @Override
    protected Long doExecute(Object... parameters) {
        return (long) parameters[0];
    }

}
