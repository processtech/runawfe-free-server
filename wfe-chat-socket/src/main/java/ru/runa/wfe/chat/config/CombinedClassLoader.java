package ru.runa.wfe.chat.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

public class CombinedClassLoader extends ClassLoader {
    private final ClassLoader delegate;

    public CombinedClassLoader(ClassLoader delegate) {
        super(CombinedClassLoader.class.getClassLoader());
        this.delegate = delegate;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try {
            return super.loadClass(name);
        } catch (ClassNotFoundException ignored) {
            return delegate.loadClass(name);
        }
    }

    @Override
    public URL getResource(String name) {
        final URL resource = super.getResource(name);
        return resource != null ? resource : delegate.getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        try {
            return super.getResources(name);
        } catch (IOException ignored) {
            return delegate.getResources(name);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        final InputStream resource = super.getResourceAsStream(name);
        return resource != null ? resource : delegate.getResourceAsStream(name);
    }

    @Override
    public void setDefaultAssertionStatus(boolean enabled) {
        super.setDefaultAssertionStatus(enabled);
        delegate.setDefaultAssertionStatus(enabled);
    }

    @Override
    public void setPackageAssertionStatus(String packageName, boolean enabled) {
        super.setPackageAssertionStatus(packageName, enabled);
        delegate.setPackageAssertionStatus(packageName, enabled);
    }

    @Override
    public void setClassAssertionStatus(String className, boolean enabled) {
        super.setClassAssertionStatus(className, enabled);
        delegate.setClassAssertionStatus(className, enabled);
    }

    @Override
    public void clearAssertionStatus() {
        super.clearAssertionStatus();
        delegate.clearAssertionStatus();
    }
}
