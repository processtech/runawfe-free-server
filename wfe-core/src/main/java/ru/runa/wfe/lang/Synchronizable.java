package ru.runa.wfe.lang;

public interface Synchronizable {
    public boolean isAsync();

    public void setAsync(boolean async);

    public AsyncCompletionMode getCompletionMode();

    public void setCompletionMode(AsyncCompletionMode completionMode);

}
