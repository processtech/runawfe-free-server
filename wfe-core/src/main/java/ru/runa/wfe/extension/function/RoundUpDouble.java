package ru.runa.wfe.extension.function;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class RoundUpDouble extends Function<Double> {

    public RoundUpDouble() {
        super(Param.required(Double.class), Param.required(Integer.class));
    }

    @Override
    protected Double doExecute(Object... parameters) {
        double d = (double) parameters[0];
        int num = (int) parameters[1];
        long st = 1;
        while (num-- > 0) {
            st *= 10;
        }
        double roundArg = d * st;
        return (double) (roundArg + (roundArg == (long) roundArg ? 0 : 1)) / st;
    }

}
