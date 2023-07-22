package ru.runa.wfe.office.doc;

public class IfOperation extends Operation {

    @Override
    public String getName() {
        return "if";
    }
    
    @Override
    public boolean isValid() {
        return false;
    }

}
