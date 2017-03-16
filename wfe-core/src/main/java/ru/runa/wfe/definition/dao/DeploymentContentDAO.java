package ru.runa.wfe.definition.dao;

import com.google.common.base.Objects;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.DeploymentContent;

import java.util.List;

/**
 *
 * DAO for {@link DeploymentContent}
 *
 * @author Egor Litvinenko
 * @since 13.03.17
 */
public class DeploymentContentDAO extends GenericDAO<DeploymentContent> {


    @Override
    protected void checkNotNull(DeploymentContent entity, Object identity) {
        if (entity == null) {
            throw new DefinitionDoesNotExistException(String.valueOf(identity));
        }
    }

    public Long nextVersion(DeploymentContent deployment, DeploymentContent previousLatestVersion) {
        return null == previousLatestVersion ? 1l : previousLatestVersion.getVersion() + 1;
    }

    public void deploy(DeploymentContent deployment, DeploymentContent previousLatestVersion) {
        // if there is a current latest process definition
        final Long version = nextVersion(deployment, previousLatestVersion);
        if (previousLatestVersion != null) {
            DeploymentContent latestDeployment = findLatestDeployment(previousLatestVersion.getName());
            if (!Objects.equal(latestDeployment.getId(), previousLatestVersion.getId())) {
                throw new InternalApplicationException("Last deployed version of process definition '" + latestDeployment.getName() + "' is '"
                        + latestDeployment.getVersion() + "'. You were provided process definition id for version '"
                        + previousLatestVersion.getVersion() + "'");
            }
            // take the next version number
            deployment.setVersion(version);
        } else {
            // start from 1
            deployment.setVersion(version);
        }
        create(deployment);
    }


    /**
     * queries the database for the latest version of a process definition with the given name.
     */
    public DeploymentContent findLatestDeployment(String name) {
        DeploymentContent deployment = findFirstOrNull("from DeploymentContent where name=? order by version desc", name);
        if (deployment == null) {
            throw new DefinitionDoesNotExistException(name);
        }
        return deployment;
    }

    /**
     * queries the database for all versions of process definitions with the given name, ordered by version (descending).
     */
    public List<DeploymentContent> findAllDeploymentVersions(String name) {
        return getHibernateTemplate().find("from DeploymentContent where name=? order by version desc", name);
    }


}
