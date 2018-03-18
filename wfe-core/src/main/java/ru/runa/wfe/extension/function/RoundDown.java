package ru.runa.wfe.extension.function;

/**
 *
 * @author Dmitry Kononov
 * @since 18.03.2018
 *
 */
public class RoundDown extends Function<Object> {

    public RoundDown() {
        super(Param.required(Double.class), Param.optional(Integer.class, null));
    }

    @Override
    protected Object doExecute(Object... parameters) {
        if (parameters[1] == null) {
            return (long) parameters[0];
        } else {
            double d = (double) parameters[0];
            int num = (int) parameters[1];
            long st = 1;
            while (num-- > 0) {
                st *= 10;
            }
            return (double) (d * st) / st;
        }
    }

    @Override
    public String getName() {
        return "round_down";
    }
}
