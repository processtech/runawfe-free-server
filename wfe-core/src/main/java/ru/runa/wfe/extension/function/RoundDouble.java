package ru.runa.wfe.extension.function;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class RoundDouble extends Function<Double> {

    @Override
    protected Double doExecute(Object... parameters) {
        double d = (double) parameters[0];
        int num = (int) parameters[1];
        long st = 1;
        while (num-- > 0) {
            st *= 10;
        }
        return (double) Math.round(d * st) / st;
    }

}
