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
package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Types ACTOR and GROUP are merged into EXECUTOR for rm718
 */
public class RefactorPermissionsStep4 extends DbMigration {

    @Override
    public void executeDML(Session session) {
        // Replace ACTOR and GROUP types with EXECUTOR in permission_mapping
        // Delete ACTOR and GROUP from priveleged_mapping
        {
            session.createSQLQuery("update permission_mapping set object_type = 'EXECUTOR' where object_type = 'ACTOR' or object_type = 'GROUP'")
                    .executeUpdate();
            session.createSQLQuery("delete from priveleged_mapping where type = 'EXECUTOR'").executeUpdate();
            session.createSQLQuery("delete from priveleged_mapping where type = 'GROUP'").executeUpdate();
            session.createSQLQuery("update priveleged_mapping set type = 'EXECUTOR' where type = 'ACTOR'").executeUpdate();
        }
    }
}
