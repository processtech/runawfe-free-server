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

/**
 * Describes left join, applied to SQL query.
 */
class LeftJoinDescription {

    /**
     * Left join expression. 
     */
    public String leftJoinExpression;

    /**
     * Table name, used as root for left join (BEFORE statement left join; to which we joins).
     */
    public String rootTableName;

    /**
     * Creates left join description.
     * @param leftJoin Left join expression. 
     * @param rootTableName Table name, used as root for left join (BEFORE statement left join; to which we joins).
     */
    public LeftJoinDescription(String leftJoin, String rootTableName) {
        leftJoinExpression = leftJoin;
        this.rootTableName = rootTableName;
    }
}
