package ru.runa.alfresco;

public interface ISynchronizable<T> {
    
    public String getBusinessDataDiff(T another);
    
}
