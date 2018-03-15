package ru.runa.wfe.extension.function;

public class NameCaseRussianWithFixedSex extends AbstractNameCaseRussian{

    public NameCaseRussianWithFixedSex() {
        super(Param.required(String.class), Param.required(Integer.class), Param.required(Boolean.class), Param.required(String.class));
    }

    @Override
    protected String doExecute(Object... parameters) {
        String fio = (String) parameters[0];
        int caseNumber = (int) parameters[1];
        boolean male = (boolean) parameters[2];
        String mode = (String) parameters[3];
        return nameCaseRussian(fio, caseNumber, male, mode);
    }
    
    @Override
    public String getName() {
        return "FIO_case_ru_with_fixed_sex";
    }
}
