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
 * Adjusts object_type and permission values for rm660, see https://rm.processtech.ru/attachments/download/1210.
 */
public class RefactorPermissionsStep3 extends DbMigration {

    /**
     * Implementation was moved to RefactorPermissionsBack.executeDML_step3() and edited.
     * See #1586, #1586-10.
     */
    @Override
    public void executeDML(Session session) {
    }
}
