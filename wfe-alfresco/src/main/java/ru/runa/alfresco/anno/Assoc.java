package ru.runa.alfresco.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Java field marked by this annotation represents association in Alfresco.
 * 
 * @author dofs
 */
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Assoc {
    /**
     * Association name as defined in model without namespace. For exceptions
     * see {@link Property#name()}
     */
    String name();

}
