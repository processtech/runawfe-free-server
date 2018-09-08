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
package ru.runa.wfe.presentation.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import ru.runa.wfe.InternalApplicationException;

/**
 * Helper class to parse place holders positions from HQL and replace
 * '?' with place holders names in SQL.  
 */
public final class HibernateCompilerPlaceholdersHelper {

    /**
     * Parse HQL query and returns query place holders in correct order (as in query).
     * @param hqlQuery HQL query to search place holders in.
     * @param placeholders Map from HQL positional parameter name to parameter value.
     * @return HQL parameters name in correct order.
     */
    public static final List<String> getPlaceholdersFromHQL(String hqlQuery, QueryParametersMap placeholders) {
        List<String> result = new ArrayList<>();
        for (int idx = searchInString(hqlQuery, ':', 0); idx != -1; idx = searchInString(hqlQuery, ':', idx + 1)) {
            for (String placeholder : placeholders.getNames()) {
                if (hqlQuery.startsWith(placeholder, idx + 1) && !Character.isLetterOrDigit(hqlQuery.charAt(idx + 1 + placeholder.length()))) {
                    result.add(placeholder);
                }
            }
        }
        return result;
    }

    /**
     * Replaces all '?' parameters in SQL with named parameters from HQL.
     * @param sqlQuery Query to replace positional parameters with named parameters.
     * @param hqlPlaceholders Sequence of named parameters in HQL.
     */
    public static final void restorePlaceholdersInSQL(StringBuilder sqlQuery, List<String> hqlPlaceholders) {
        Iterator<String> placeholders = hqlPlaceholders.iterator();
        for (int idx = searchInString(sqlQuery, '?', 0); idx != -1; idx = searchInString(sqlQuery, '?', idx + 1)) {
            if (placeholders.hasNext()) {
                sqlQuery.replace(idx, idx + 1, ":" + placeholders.next());
            } else {
                throw new InternalApplicationException("Can't compile batchPresentation. SQL has more named parameter when HQL.");
            }
        }
    }

    /**
     * Searches in specified string first occurrence of character. 
     * Quoted characters is skipped.    
     * @param string String, to search character in.
     * @param searchCharacter Character to search.
     * @param startIndex Index in string, to start search from.
     * @return The index of the first occurrence of the character in the string; or -1 if character not found.
     */
    private static final int searchInString(CharSequence string, final char searchCharacter, final int startIndex) {
        boolean inQuot = false;
        boolean inDoubleQuot = false;
        for (int idx = startIndex; idx < string.length(); ++idx) {
            char charAt = string.charAt(idx);
            if (charAt == '\'' && !inDoubleQuot) {
                inQuot = !inQuot;
            }
            if (charAt == '\"' && !inQuot) {
                inDoubleQuot = !inDoubleQuot;
            }
            if (charAt == searchCharacter && !inQuot && !inDoubleQuot) {
                return idx;
            }
        }
        return -1;
    }
}
