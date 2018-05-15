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
import java.util.Date;
import java.util.List;
import org.springframework.stereotype.Component;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.QDeployment;

/**
 * DAO for {@link Deployment}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class DeploymentDAO extends GenericDAO<Deployment> {

    public void deploy(Deployment deployment, Deployment previousLatestVersion) {
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

    @Override
    protected void checkNotNull(Deployment entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * queries the database for the latest version of a process definition with the given name.
     */
    public Deployment findLatestDeployment(String name) {
        QDeployment d = QDeployment.deployment;
        Deployment deployment = queryFactory.selectFrom(d).where(d.name.eq(name)).orderBy(d.version.desc()).fetchFirst();
        if (deployment == null) {
            throw new DefinitionDoesNotExistException(name);
        }
        return deployment;
    }

    public Deployment findDeployment(String name, Long version) {
        QDeployment d = QDeployment.deployment;
        Deployment deployment = queryFactory.selectFrom(d).where(d.name.eq(name).and(d.version.eq(version))).fetchFirst();
        if (deployment == null) {
            throw new DefinitionDoesNotExistException(name + " v" + version);
        }
        return deployment;
    }

    /**
     * queries the database for definition names.
     */
    public List<String> findDeploymentNames() {
        QDeployment d = QDeployment.deployment;
        return queryFactory.selectDistinct(d.name).from(d).orderBy(d.name.desc()).fetch();
    }

    /**
     * queries the database for all version ids of process definitions with the given name, ordered by version.
     */
    public List<Long> findAllDeploymentVersionIds(String name, boolean ascending) {
        QDeployment d = QDeployment.deployment;
        return queryFactory.select(d.id).from(d).where(d.name.eq(name)).orderBy(ascending ? d.version.asc() : d.version.desc()).fetch();
    }

    public List<Long> findDeploymentVersionIds(String name, Long from, Long to) {
        QDeployment d = QDeployment.deployment;
        // Parameters "from" and "to" are flipped in between(). It was that way previously in HQL: version <= from && version >= to:
        return queryFactory.select(d.id).from(d).where(d.name.eq(name).and(d.version.between(to, from))).orderBy(d.version.asc()).fetch();
    }

    public Long findDeploymentIdLatestVersionLessThan(String name, Long version) {
        QDeployment d = QDeployment.deployment;
        return queryFactory.select(d.id).from(d).where(d.name.eq(name).and(d.version.lt(version))).orderBy(d.version.desc()).fetchFirst();
    }

    public Long findDeploymentIdLatestVersionBeforeDate(String name, Date date) {
        QDeployment d = QDeployment.deployment;
        return queryFactory.select(d.id).from(d).where(d.name.eq(name).and(d.createDate.lt(date))).orderBy(d.version.desc()).fetchFirst();
    }

    /**
     * queries the database for all versions of process definitions with the given name, ordered by version (descending).
     * 
     * @deprecated use findAllDeploymentVersionIds
     */
    @Deprecated
    public List<Deployment> findAllDeploymentVersions(String name) {
        QDeployment d = QDeployment.deployment;
        return queryFactory.selectFrom(d).where(d.name.eq(name)).orderBy(d.version.desc()).fetch();
    }

}
