package ru.runa.wfe.ss;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * Represents a special type of the substitution rule.
 * 
 * It can be used to specify tasks which should not be propagated to
 * substitutors.
 */
@Entity
@DiscriminatorValue(value = "Y")
public class TerminatorSubstitution extends Substitution {
    private static final long serialVersionUID = 1L;

}
