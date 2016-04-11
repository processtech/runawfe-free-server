package ru.runa.alfresco.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java field marked by this annotation represents property in Alfresco.
 * 
 * @author dofs
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    /**
     * Property name as defined in model without namespace. Namespace defined in
     * class-level assumed to be used. If your property belongs to another
     * namespace you can write it with prefix, for example cm:name.
     */
    String name();

    /**
     * Readonly means that marked property will not be persisted in Alfresco.
     */
    boolean readOnly() default false;

    /**
     * Class is loaded from Alfresco model. You can override it here.
     */
    String className() default "";
}
