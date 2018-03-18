package ru.runa.wfe.extension.function;

/**
 *
 * @author Dmitry Kononov
 * @since 18.03.2018
 *
 */
public class Round extends Function<Object> {

    public Round() {
        super(Param.required(Double.class), Param.optional(Integer.class, null));
    }

    @Override
    protected Object doExecute(Object... parameters) {
        double d = (double) parameters[0];
        if (parameters[1] == null) {
            return Math.round((double) parameters[0]);
        } else {
            int num = (int) parameters[1];
            long st = 1;
            while (num-- > 0) {
                st *= 10;
            }
            return (double) Math.round(d * st) / st;
        }
    }

    @Override
    public String getName() {
        return "round";
    }

}
