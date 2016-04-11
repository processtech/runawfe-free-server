package ru.runa.alfresco;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.namespace.QName;

import ru.runa.alfresco.search.Search;

/**
 * Alfresco repository connection interface.
 * 
 * @author dofs
 */
public interface AlfConnection {

    public void createObject(AlfObject object);

    public void addAspect(AlfObject object, QName aspectTypeName);

    /**
     * Recommended way of working with associations is fill collections and
     * execute updateAssociations().
     */
    public void addChildAssociation(AlfObject source, AlfObject target, QName associationTypeName);

    /**
     * Initializes type definition if not initialized yet. Initialization means
     * that some attributes about property and associations will be requested
     * from Alfresco.
     * 
     * @param typeDesc
     */
    public void initializeTypeDefinition(AlfTypeDesc typeDesc);

    /**
     * Load object from Alfresco repository.
     * 
     * @param uuidRef
     *            UUID reference with space store
     *            (workspace://SpacesStore/05b9ec4c
     *            -74f7-4d52-9015-5697374a9b6a).
     * @return loaded object or <code>null</code>
     */
    public <T extends AlfObject> T loadObject(String uuidRef);

    /**
     * Load object from Alfresco repository or throws exception.
     * 
     * @param uuidRef
     *            UUID reference with space store
     *            (workspace://SpacesStore/05b9ec4c
     *            -74f7-4d52-9015-5697374a9b6a).
     * @return loaded object
     */
    public <T extends AlfObject> T loadObjectNotNull(String uuidRef);

    /**
     * Loads association from Alfresco repository.
     * 
     * @param uuidRef
     *            UUID reference with space store
     *            (workspace://SpacesStore/05b9ec4c
     *            -74f7-4d52-9015-5697374a9b6a).
     * @param collection
     *            container for association objects.
     * @param desc
     *            descriptor.
     */
    @SuppressWarnings("rawtypes")
    public void loadAssociation(String uuidRef, Collection collection, AlfPropertyDesc desc);

    /**
     * Finds object in Alfresco repository. TODO don't use this method and
     * refactor usage.
     * 
     * @param <T>
     *            result type
     * @param search
     *            valid Lucene query in object presentation.
     * @return found object or <code>null</code>
     */
    public <T extends AlfObject> T findFirstObject(Search search);

    /**
     * Finds object in Alfresco repository. Throws exception if found more than
     * 1 object by specified search.
     * 
     * @param <T>
     *            result type
     * @param search
     *            valid Lucene query in object presentation.
     * @return found object or <code>null</code>
     */
    public <T extends AlfObject> T findUniqueObject(Search search);

    /**
     * Finds object in Alfresco repository. Throws exception if found more than
     * 1 object by specified search.
     * 
     * @param <T>
     *            result type
     * @param search
     *            valid Lucene query in object presentation.
     * @return found object or throws Exception
     */
    public <T extends AlfObject> T findUniqueObjectNotNull(Search search);

    /**
     * Finds objects in Alfresco repository.
     * 
     * @param <T>
     *            result type
     * @param search
     *            valid Lucene query in object presentation.
     * @return list of found objects
     */
    public <T extends AlfObject> List<T> findObjects(Search search);

    /**
     * Sets binary content to alfresco object.
     */
    public void setContent(AlfObject object, String content, String mimetype);

    /**
     * Sets binary content to alfresco object.
     */
    public void setContent(AlfObject object, byte[] data, String mimetype);

    /**
     * Updates object in Alfresco without creation new version.
     * 
     * @param object
     * @param force
     *            all properties
     */
    public boolean updateObject(AlfObject object, boolean force);

    /**
     * Updates object in Alfresco with creation new version.
     * 
     * @param object
     * @param force
     *            all properties
     * @param comment
     *            version comment
     */
    public boolean updateObject(AlfObject object, boolean force, String comment);

    /**
     * Updates object associations in Alfresco.
     * 
     * @param object
     */
    public boolean updateObjectAssociations(AlfObject object);

    /**
     * Deletes object from Alfresco.
     * 
     * @param object
     */
    public void deleteObject(AlfObject object);
}
