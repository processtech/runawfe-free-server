package ru.runa.alfresco.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * All persistable in Alfresco objects should have this annotation.
 * @author dofs
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Type {
    /**
     * Type name as defined in alfresco model without namespace.
     */
    String name();
    /**
     * Prefix for Alfresco namespace.
     */
    String prefix();
    /**
     * You can map aspect for java-type.
     */
    boolean aspect() default false;
}
