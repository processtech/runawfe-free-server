/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.definition.dao;

import com.google.common.base.Objects;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentWithContent;
import ru.runa.wfe.definition.QDeployment;

/**
 * DAO for {@link DeploymentWithContent}.
 * 
 * @author dofs
 * @since 4.4
 */
@Component
public class DeploymentWithContentDao extends GenericDao<DeploymentWithContent> {

    public void deploy(DeploymentWithContent deployment, Deployment previousLatestVersion) {
        // if there is a current latest process definition
        if (previousLatestVersion != null) {
            Deployment latestDeployment = findLatestDeployment(previousLatestVersion.getName());
            if (!Objects.equal(latestDeployment.getId(), previousLatestVersion.getId())) {
                throw new InternalApplicationException("Last deployed version of process definition '" + latestDeployment.getName() + "' is '"
                        + latestDeployment.getVersion() + "'. You were provided process definition id for version '"
                        + previousLatestVersion.getVersion() + "'");
            }
            // take the next version number
            deployment.setVersion(previousLatestVersion.getVersion() + 1);
        } else {
            // start from 1
            deployment.setVersion(1L);
        }
        create(deployment);
    }

    private Deployment findLatestDeployment(String name) {
        QDeployment d = QDeployment.deployment;
        Deployment deployment = queryFactory.selectFrom(d).where(d.name.eq(name)).orderBy(d.version.desc()).fetchFirst();
        if (deployment == null) {
            throw new DefinitionDoesNotExistException(name);
        }
        return deployment;
    }

    @Override
    protected void checkNotNull(DeploymentWithContent entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

}
