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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.ProcessDefinition;
import ru.runa.wfe.definition.ProcessDefinitionWithVersion;
import ru.runa.wfe.definition.QProcessDefinition;
import ru.runa.wfe.definition.QProcessDefinitionVersion;

/**
 * DAO for {@link ProcessDefinition}.
 * 
 * @author dofs
 * @since 4.0
 */
@Component
public class DeploymentDao extends GenericDao<ProcessDefinition> {

    @Override
    protected void checkNotNull(ProcessDefinition entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * @return Not null.
     * @throws DefinitionDoesNotExistException If not found
     */
    public ProcessDefinitionWithVersion findLatestDeployment(@NonNull String deploymentName) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.processDefinition, d)
                .where(d.name.eq(deploymentName))
                .orderBy(dv.version.desc())
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(deploymentName);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * @return Not null.
     * @throws DefinitionDoesNotExistException If not found
     */
    public ProcessDefinitionWithVersion findLatestDeployment(long deploymentId) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.processDefinition, d)
                .where(d.id.eq(deploymentId))
                .orderBy(dv.version.desc())
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException("deploymentId = " + deploymentId);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    public ProcessDefinitionWithVersion findDeployment(@NonNull String name, long version) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.processDefinition, d)
                .where(d.name.eq(name).and(dv.version.eq(version)))
                .fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException(name + " v" + version);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * Eager load both ProcessDefinition and ProcessDefinitionVersion. Probably this could be done "more Hibernate way" (like marking @ManyToOne field
     * ProcessDefinitionVersion.deployment as eager loaded if that's possible), but this implementation is a step closer to getting rid of Hibernate.
     */
    public ProcessDefinitionWithVersion findDeployment(long processDefinitionVersionId) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        Tuple t = queryFactory.select(d, dv).from(dv).innerJoin(dv.processDefinition, d).where(dv.id.eq(processDefinitionVersionId)).fetchFirst();
        if (t == null) {
            throw new DefinitionDoesNotExistException("processDefinitionVersionId = " + processDefinitionVersionId);
        }
        return new ProcessDefinitionWithVersion(t.get(d), t.get(dv));
    }

    /**
     * queries the database for definition names.
     */
    public List<String> findDeploymentNames() {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        return queryFactory.selectDistinct(d.name).from(d).orderBy(d.name.desc()).fetch();
    }

    /**
     * Returns ids of all ProcessDefinitionVersion-s which belong to same ProcessDefinition as processDefinitionVersionId, ordered by version.
     */
    public List<Long> findAllDeploymentVersionIds(long processDefinitionVersionId, boolean ascending) {
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;

        // TODO This can be implemented as subquery (hopefully in Hibernate):
        Long deploymentId = queryFactory.select(dv.processDefinition.id).from(dv).where(dv.id.eq(processDefinitionVersionId)).fetchFirst();
        Preconditions.checkNotNull(deploymentId);

        return queryFactory.select(dv.id)
                .from(dv)
                .where(dv.processDefinition.id.eq(deploymentId))
                .orderBy(ascending ? dv.version.asc() : dv.version.desc())
                .fetch();
    }

    public List<Long> findDeploymentVersionIds(String name, Long from, Long to) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .innerJoin(dv.processDefinition, d)
                .where(d.name.eq(name).and(dv.version.between(from, to)))
                .orderBy(dv.version.asc())
                .fetch();
    }

    public Long findDeploymentVersionIdLatestVersionLessThan(long deploymentId, long version) {
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .where(dv.processDefinition.id.eq(deploymentId).and(dv.version.lt(version)))
                .orderBy(dv.version.desc())
                .fetchFirst();
    }

    public Long findDeploymentVersionIdLatestVersionBeforeDate(String name, Date date) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        return queryFactory.select(dv.id)
                .from(dv)
                .innerJoin(dv.processDefinition, d)
                .where(d.name.eq(name).and(dv.createDate.lt(date))).orderBy(dv.version.desc()).fetchFirst();
    }

    /**
     * queries the database for all versions of process definitions with the given name, ordered by version (descending).
     * 
     * @deprecated use findAllDeploymentVersionIds
     */
    @Deprecated
    public List<ProcessDefinitionWithVersion> findAllDeploymentVersions(String name) {
        QProcessDefinition d = QProcessDefinition.processDefinition;
        QProcessDefinitionVersion dv = QProcessDefinitionVersion.processDefinitionVersion;
        List<Tuple> tt = queryFactory.select(d, dv)
                .from(dv)
                .innerJoin(dv.processDefinition, d)
                .where(d.name.eq(name))
                .orderBy(dv.version.desc())
                .fetch();

        // TODO After migrating to Spring5, use stream() with lambdas.
        val result = new ArrayList<ProcessDefinitionWithVersion>(tt.size());
        for (Tuple t : tt) {
            result.add(new ProcessDefinitionWithVersion(t.get(d), t.get(dv)));
        }
        return result;
    }
}
