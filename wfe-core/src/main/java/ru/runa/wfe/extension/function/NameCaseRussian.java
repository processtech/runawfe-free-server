package ru.runa.wfe.extension.function;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class NameCaseRussian extends AbstractNameCaseRussian {

    public NameCaseRussian() {
        super(Param.required(String.class), Param.required(Integer.class), Param.required(String.class));
    }

    @Override
    protected String doExecute(Object... parameters) {
        String fio = (String) parameters[0];
        int caseNumber = (int) parameters[1];
        String mode = (String) parameters[2];
        boolean male = isMale(fio);
        return nameCaseRussian(fio, caseNumber, male, mode);
    }

    @Override
    public String getName() {
        return "FIO_case_ru";
    }

}
