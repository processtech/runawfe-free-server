package ru.runa.wfe.office.doc;

import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.base.Strings;

public class InsertImageOperation extends ReplaceOperation {
    private IFileVariable fileVariable;
    private int imageType;
    private int width;
    private int height;

    public InsertImageOperation(String placeholder, IFileVariable fileVariable) {
        this.placeholder = placeholder;
        this.placeholderRead = true;
        this.fileVariable = fileVariable;
    }

    public IFileVariable getFileVariable() {
        return fileVariable;
    }

    public int getImageType() {
        return imageType;
    }

    public void setImageType(int imageType) {
        this.imageType = imageType;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean isValid() {
        return !Strings.isNullOrEmpty(placeholder);
    }
}
