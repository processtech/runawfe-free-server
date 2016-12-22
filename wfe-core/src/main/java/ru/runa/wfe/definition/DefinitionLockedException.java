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
package ru.runa.wfe.definition;

import java.util.Date;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.user.Actor;

/**
 * Signals that process definition is locked.
 */
public class DefinitionLockedException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final Actor actor;
    private final Date date;
    private final Boolean forAll;

    public DefinitionLockedException(Deployment deployment) {
        super("Definition " + deployment.getName() + " locked by " + deployment.getLockActor().getName() + " at "
                + CalendarUtil.formatDateTime(deployment.getLockDate()) + (deployment.getLockForAll() ? " for all." : "."));
        this.name = deployment.getName();
        this.actor = deployment.getLockActor();
        this.date = deployment.getLockDate();
        this.forAll = deployment.getLockForAll();
    }

    public String getName() {
        return name;
    }

    public Actor getActor() {
        return actor;
    }

    public Date getDate() {
        return date;
    }

    public boolean isForAll() {
        return forAll == Boolean.TRUE;
    }
}
