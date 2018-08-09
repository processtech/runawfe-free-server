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

import com.google.common.base.Preconditions;
import com.querydsl.core.Tuple;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentWithVersion;
import ru.runa.wfe.definition.QDeployment;
import ru.runa.wfe.definition.QDeploymentVersion;

/**
 * DAO for {@link Deployment}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class DeploymentDAO extends GenericDAO<Deployment> {

    @Override
    protected void checkNotNull(Deployment entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * @return Not null.
     * @throws DefinitionDoesNotExistException If not found
     */
    public DeploymentWithVersion findLatestDeployment(@NonNull String deploymentName) {
        QDeployment d = QDeployment.deployment;
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.deployment, d)
                .where(d.name.eq(deploymentName))
                .orderBy(dv.version.desc())
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(deploymentName);
        }
        return new DeploymentWithVersion(t.get(d), t.get(dv));
    }

    /**
     * @return Not null.
     * @throws DefinitionDoesNotExistException If not found
     */
    public DeploymentWithVersion findLatestDeployment(long deploymentId) {
        QDeployment d = QDeployment.deployment;
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.deployment, d)
                .where(d.id.eq(deploymentId))
                .orderBy(dv.version.desc())
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException("deploymentId = " + deploymentId);
        }
        return new DeploymentWithVersion(t.get(d), t.get(dv));
    }

    public DeploymentWithVersion findDeployment(@NonNull String name, long version) {
        QDeployment d = QDeployment.deployment;
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;
        Tuple t = queryFactory.select(d, dv).from(dv).innerJoin(dv.deployment, d).where(d.name.eq(name).and(dv.version.eq(version))).fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(name + " v" + version);
        }
        return new DeploymentWithVersion(t.get(d), t.get(dv));
    }

    /**
     * Eager load both Deployment and DeploymentVersion. Probably this could be done "more Hibernate way" (like marking @ManyToOne field
     * DeploymentVersion.deployment as eager loaded if that's possible), but this implementation is a step closer to getting rid of Hibernate.
     */
    public DeploymentWithVersion findDeployment(long deploymentVersionId) {
        QDeployment d = QDeployment.deployment;
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;
        Tuple t = queryFactory.select(d, dv).from(dv).innerJoin(dv.deployment, d).where(dv.id.eq(deploymentVersionId)).fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException("deploymentVersionId = " + deploymentVersionId);
        }
        return new DeploymentWithVersion(t.get(d), t.get(dv));
    }

    /**
     * queries the database for definition names.
     */
    public List<String> findDeploymentNames() {
        QDeployment d = QDeployment.deployment;
        return queryFactory.selectDistinct(d.name).from(d).orderBy(d.name.desc()).fetch();
    }

    /**
     * Returns ids of all DeploymentVersion-s which belong to same Deployment as deploymentVersionId, ordered by version.
     */
    public List<Long> findAllDeploymentVersionIds(long deploymentVersionId, boolean ascending) {
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;

        // TODO This can be implemented as subquery (hopefully in Hibernate):
        Long deploymentId = queryFactory.select(dv.deployment.id).from(dv).where(dv.id.eq(deploymentVersionId)).fetchFirst();
        Preconditions.checkNotNull(deploymentId);

        return queryFactory.select(dv.id)
                .from(dv)
                .where(dv.deployment.id.eq(deploymentId))
                .orderBy(ascending ? dv.version.asc() : dv.version.desc())
                .fetch();
    }

    public List<Long> findDeploymentVersionIds(String name, Long from, Long to) {
        QDeployment d = QDeployment.deployment;
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;
        return queryFactory.select(dv.id).from(dv).innerJoin(dv.deployment, d)
                .where(d.name.eq(name).and(dv.version.between(from, to))).orderBy(dv.version.asc()).fetch();
    }

    public Long findDeploymentVersionIdLatestVersionLessThan(long deploymentId, long version) {
        QDeploymentVersion dv = QDeploymentVersion.deploymentVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .where(dv.deployment.id.eq(deploymentId).and(dv.version.lt(version)))
                .orderBy(dv.version.desc())
                .fetchFirst();
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
