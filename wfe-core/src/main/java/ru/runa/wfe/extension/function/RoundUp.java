package ru.runa.wfe.extension.function;

/**
 *
 * @author Dmitry Kononov
 * @since 18.03.2018
 *
 */
public class RoundUp extends Function<Object> {

    public RoundUp() {
        super(Param.required(Double.class), Param.optional(Integer.class, null));
    }

    @Override
    protected Object doExecute(Object... parameters) {
        if (parameters[1] == null) {
            double doubleValue = (Double) parameters[0];
            return (long) doubleValue + (doubleValue == (long) doubleValue ? 0 : 1);
        } else {
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

    @Override
    public String getName() {
        return "round_up";
    }

}
