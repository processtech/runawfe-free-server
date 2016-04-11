package ru.runa.wfe.var;

/**
 * Option for multi-choice. toString must return value of the option.
 * 
 * @author dofs
 */
public interface ISelectable {

    String getLabel();

    String getValue();
}
