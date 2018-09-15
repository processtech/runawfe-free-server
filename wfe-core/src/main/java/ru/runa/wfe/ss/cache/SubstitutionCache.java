/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.ss.cache;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.user.Actor;

/**
 * Interface for substitution cache components.
 */
public interface SubstitutionCache {
    /**
     * Returns for specified inactive {@link Actor} {@link Map} from substitution rule to {@link Set} of substitutors. If {@link Actor} is active then
     * empty result is returned.
     * 
     * @param actor
     *            Actor, which substitution rules will be returned.
     * @param loadIfRequired
     *            Flag, equals true if substitution rules may be loaded from database if cache is empty and false to return null in this case.
     * @return {@link Map} from substitution rule to {@link Set} of substitutor id's.
     */
    TreeMap<Substitution, Set<Long>> getSubstitutors(Actor actor, boolean loadIfRequired);

    /**
     * Try to get substitutors for actor. If cache is not initialized or substitutors not found this method will not query database - it returns null
     * instead.
     * 
     * @param actor
     *            Actor, to get substitutors.
     * @return Substitutors for actor or null, if substitutors not initialized for actor.
     */
    TreeMap<Substitution, Set<Long>> tryToGetSubstitutors(Actor actor);

    /**
     * Returns all inactive {@link Actor}'s, which has at least one substitution rule with specified actor as substitutor.
     * 
     * @param actor
     *            {@link Actor}, which substituted actors will be returned.
     * @return All inactive {@link Actor} id's, which has at least one substitution rule with specified actor as substitutor.
     */
    Set<Long> getSubstituted(Actor actor);
}
