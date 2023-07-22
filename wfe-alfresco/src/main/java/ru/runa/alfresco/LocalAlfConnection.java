package ru.runa.alfresco;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.alfresco.search.Search;
import ru.runa.alfresco.search.Search.Sorting;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

/**
 * Connection implementation using Alfresco Java API (used in same JVM).
 * 
 * @author dofs
 */
@SuppressWarnings("unchecked")
public class LocalAlfConnection implements AlfConnection {
    private static Log log = LogFactory.getLog(LocalAlfConnection.class);

    private final ServiceRegistry registry;

    public LocalAlfConnection(ServiceRegistry registry) {
        this.registry = registry;
    }

    public ServiceRegistry getRegistry() {
        return registry;
    }

    private NodeRef getNodeRef(AlfObject object) {
        return new NodeRef(object.getUuidRef());
    }

    @Override
    public void addAspect(AlfObject object, QName aspectTypeName) {
        AlfTypeDesc typeDesc = Mappings.getMapping(aspectTypeName.toString(), this);
        Map<QName, Serializable> props = new LocalAlfObjectAccessor(typeDesc, object).getProperties(true, false);
        registry.getNodeService().addAspect(getNodeRef(object), aspectTypeName, props);
    }

    public boolean hasAspect(AlfObject object, QName aspectTypeName) {
        return registry.getNodeService().getAspects(getNodeRef(object)).contains(aspectTypeName);
    }

    public ResultSet find(Search search) throws InternalApplicationException {
        try {
            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_LUCENE);
            sp.addStore(search.getStore());
            sp.setQuery(search.toString());
            if (search.getLimit() != 0) {
                sp.setLimit(search.getLimit());
                sp.setLimitBy(LimitBy.FINAL_SIZE);
            }
            if (search.hasSorting()) {
                for (Sorting sorting : search.getSortings()) {
                    sp.addSort("@" + sorting.getName().toString(), sorting.isAscending());
                }
            }
            return registry.getSearchService().query(sp);
        } catch (Exception e) {
            log.error("Failed query: " + search);
            throw Throwables.propagate(e);
        }
    }

    public List<NodeRef> findObjectRefs(Search search) throws InternalApplicationException {
        ResultSet resultSet = null;
        try {
            resultSet = find(search);
            List<NodeRef> refs = resultSet.getNodeRefs();
            log.debug("Search " + search + " returns " + refs.size());
            return refs;
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    @Override
    public <T extends AlfObject> List<T> findObjects(Search search) throws InternalApplicationException {
        List<NodeRef> refs = findObjectRefs(search);
        return (List<T>) loadObjects(refs);
    }

    @Override
    public <T extends AlfObject> T findUniqueObject(Search search) throws InternalApplicationException {
        List<T> objects = findObjects(search);
        if (objects.size() > 1) {
            throw new InternalApplicationException("Search " + search + " returned not unique result: " + objects);
        }
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    @Override
    public <T extends AlfObject> T findUniqueObjectNotNull(Search search) {
        T object = (T) findUniqueObject(search);
        if (object == null) {
            throw new InternalApplicationException("Search " + search + " returned empty result");
        }
        return object;
    }

    @Override
    public <T extends AlfObject> T findFirstObject(Search search) {
        search.setLimit(1);
        List<T> objects = findObjects(search);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    public List<AlfObject> loadObjects(List<NodeRef> refs) throws InternalApplicationException {
        List<AlfObject> result = new ArrayList<AlfObject>(refs.size());
        for (NodeRef nodeRef : refs) {
            AlfObject object = loadObject(nodeRef);
            if (object != null) {
                result.add(object);
            }
        }
        return result;
    }

    @Override
    public <T extends AlfObject> T loadObject(String uuidRef) {
        return (T) loadObject(new NodeRef(uuidRef));
    }

    @Override
    public <T extends AlfObject> T loadObjectNotNull(String uuidRef) {
        T object = (T) loadObject(uuidRef);
        if (object == null) {
            throw new InternalApplicationException("Unable to load object by " + uuidRef);
        }
        return object;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void loadAssociation(String uuidRef, Collection collection, AlfPropertyDesc desc) throws InternalApplicationException {
        NodeRef nodeRef = new NodeRef(uuidRef);
        QName assocName = desc.getPropertyQName();
        if (desc.isChildAssociation()) {
            List<ChildAssociationRef> childAssocRefs;
            if (desc.isSourceAssociation()) {
                childAssocRefs = registry.getNodeService().getChildAssocs(nodeRef);
            } else {
                childAssocRefs = registry.getNodeService().getParentAssocs(nodeRef);
            }
            for (ChildAssociationRef assocRef : childAssocRefs) {
                if (assocRef.getTypeQName().equals(assocName)) {
                    collection.add(loadObject(desc.isSourceAssociation() ? assocRef.getChildRef() : assocRef.getParentRef()));
                }
            }
        } else {
            List<AssociationRef> assocRefs;
            if (desc.isSourceAssociation()) {
                assocRefs = registry.getNodeService().getTargetAssocs(nodeRef, assocName);
            } else {
                assocRefs = registry.getNodeService().getSourceAssocs(nodeRef, assocName);
            }
            for (AssociationRef assocRef : assocRefs) {
                if (assocRef.getTypeQName().equals(assocName)) {
                    collection.add(loadObject(desc.isSourceAssociation() ? assocRef.getTargetRef() : assocRef.getSourceRef()));
                }
            }
        }
    }

    @Override
    public boolean updateObjectAssociations(AlfObject object) throws InternalApplicationException {
        boolean updated = false;
        Map<AlfPropertyDesc, List<String>> assocToDelete = object.getAssocToDelete();
        for (Map.Entry<AlfPropertyDesc, List<String>> entry : assocToDelete.entrySet()) {
            for (String uuidRef : entry.getValue()) {
                log.debug("Removing assoc " + uuidRef + " from " + object.getUuidRef());
                if (entry.getKey().isChildAssociation()) {
                    registry.getNodeService().removeChild(getNodeRef(object), new NodeRef(uuidRef));
                } else {
                    registry.getNodeService().removeAssociation(getNodeRef(object), new NodeRef(uuidRef), entry.getKey().getPropertyQName());
                }
            }
            updated = true;
        }
        Map<AlfPropertyDesc, List<String>> assocToCreate = object.getAssocToCreate();
        for (Map.Entry<AlfPropertyDesc, List<String>> entry : assocToCreate.entrySet()) {
            for (String uuidRef : entry.getValue()) {
                log.debug("Adding assoc " + uuidRef + " to " + object.getUuidRef());
                if (entry.getKey().isChildAssociation()) {
                    addChildAssociation(object, uuidRef, entry.getKey().getPropertyQName());
                } else {
                    registry.getNodeService().createAssociation(getNodeRef(object), new NodeRef(uuidRef), entry.getKey().getPropertyQName());
                }
            }
            updated = true;
        }
        object.markCollectionsInitialState();
        return updated;
    }

    @Override
    public void addChildAssociation(AlfObject object, AlfObject target, QName associationTypeName) {
        addChildAssociation(object, target.getUuidRef(), associationTypeName);
    }

    private void addChildAssociation(AlfObject object, String uuidRef, QName associationTypeName) {
        Preconditions.checkState(uuidRef != null, "Save object before adding to association.");
        registry.getNodeService().addChild(getNodeRef(object), new NodeRef(uuidRef), associationTypeName, associationTypeName);
    }

    public <T extends AlfObject> T loadObject(NodeRef nodeRef) throws InternalApplicationException {
        try {
            if (!registry.getNodeService().exists(nodeRef)) {
                log.warn("Node does not exists: " + nodeRef);
                return null;
            }
            Map<QName, Serializable> properties = registry.getNodeService().getProperties(nodeRef);
            return (T) buildObject(nodeRef, registry.getNodeService().getType(nodeRef), properties);
        } catch (InvalidNodeRefException e) {
            // transaction will rolled-back
            log.warn(e);
            return null;
        }
    }

    public AlfObject buildObject(NodeRef nodeRef, QName type, Map<QName, Serializable> properties) throws InternalApplicationException {
        try {
            AlfTypeDesc typeDesc = Mappings.getMapping(type.toString(), this);
            AlfObject object = AlfObjectFactory.create(typeDesc.getJavaClassName(), this, nodeRef.toString());
            LocalAlfObjectAccessor accessor = new LocalAlfObjectAccessor(typeDesc, object);
            accessor.setProperties(properties);
            object.markPropertiesInitialState(typeDesc);
            return object;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public void createObject(AlfObject object) throws InternalApplicationException {
        String folderUUID = Mappings.getFolderUUID(object.getClass());
        NodeRef dirRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, folderUUID);
        createObject(object, dirRef);
    }

    public void createObject(AlfObject object, NodeRef folderRef) throws InternalApplicationException {
        AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass(), this);
        log.debug("Creating new object of type " + object.getClass().getName() + " in " + folderRef);
        if (object.getObjectName() == null) {
            object.updateObjectName();
        }
        object.setObjectName(object.getObjectName());
        Map<QName, Serializable> props = new LocalAlfObjectAccessor(typeDesc, object).getProperties(true, true);
        ChildAssociationRef ref = registry.getNodeService().createNode(folderRef, ContentModel.ASSOC_CONTAINS,
                QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, object.getObjectName()),
                QName.createQName(typeDesc.getAlfrescoTypeNameWithNamespace()), props);
        object.setUuidRef(ref.getChildRef().toString());
        object.markPropertiesInitialState(typeDesc);
        object.setLazyLoader(this);
        log.debug("Created object " + object);
    }

    @Override
    public boolean updateObject(AlfObject object, boolean forceAllProps) throws InternalApplicationException {
        AlfTypeDesc typeDesc = Mappings.getMapping(object.getClass(), this);
        LocalAlfObjectAccessor accessor = new LocalAlfObjectAccessor(typeDesc, object);
        Map<QName, Serializable> props = accessor.getProperties(forceAllProps, false);
        if (props.size() == 0) {
            log.debug("Ignored object " + object + " to update (0) fields");
            return false;
        }
        log.debug("Updating " + object + " (" + props.size() + ") fields");
        if (!registry.getNodeService().hasAspect(getNodeRef(object), ContentModel.ASPECT_VERSIONABLE)) {
            log.debug(object.getDirtyFieldChanges(typeDesc));
        }
        for (QName name : props.keySet()) {
            registry.getNodeService().setProperty(getNodeRef(object), name, props.get(name));
        }
        object.markPropertiesInitialState(typeDesc);
        return true;
    }

    @Override
    public boolean updateObject(AlfObject object, boolean forceAllProps, String comment) throws InternalApplicationException {
        boolean updated = updateObject(object, forceAllProps);
        if (updated) {
            Map<String, Serializable> versionDetails = new HashMap<String, Serializable>(1);
            versionDetails.put(Version.PROP_DESCRIPTION, comment);
            registry.getVersionService().createVersion(getNodeRef(object), versionDetails);
        }
        return updated;
    }

    @Override
    public void deleteObject(AlfObject object) throws InternalApplicationException {
        deleteObject(getNodeRef(object));
    }

    private void deleteObject(NodeRef nodeRef) throws InternalApplicationException {
        if (!registry.getNodeService().exists(nodeRef)) {
            log.warn("No object exists: " + nodeRef);
            return;
        }
        registry.getNodeService().deleteNode(nodeRef);
    }

    @Override
    public void setContent(AlfObject object, String content, String mimetype) {
        setContent(object, content.getBytes(Charsets.UTF_8), mimetype);
    }

    @Override
    public void setContent(AlfObject object, byte[] data, String mimetype) {
        log.info("Setting content to " + object);
        ContentWriter writer = registry.getContentService().getWriter(getNodeRef(object), ContentModel.PROP_CONTENT, true);
        writer.setEncoding(Charsets.UTF_8.name());
        writer.setMimetype(mimetype);
        writer.putContent(new ByteArrayInputStream(data));
    }

    @Override
    public void initializeTypeDefinition(AlfTypeDesc typeDesc) {
        if (typeDesc.isClassDefinitionLoaded()) {
            return;
        }
        log.info("Loading definition for " + typeDesc);
        QName typeName = QName.createQName(typeDesc.getAlfrescoTypeNameWithNamespace());
        ClassDefinition definition;
        if (typeDesc.isAspect()) {
            definition = registry.getDictionaryService().getAspect(typeName);
        } else {
            definition = registry.getDictionaryService().getClass(typeName);
        }
        if (definition == null) {
            throw new NullPointerException("No definition loaded for " + typeName);
        }
        typeDesc.setTitle(definition.getTitle());
        for (AlfPropertyDesc desc : typeDesc.getAllDescs()) {
            if (desc.getProperty() != null) {
                PropertyDefinition propertyDefinition = registry.getDictionaryService().getProperty(desc.getPropertyQName());
                if (propertyDefinition == null) {
                    throw new InternalApplicationException("No property found in Alfresco for " + desc + " of type " + typeDesc);
                }
                desc.setTitle(propertyDefinition.getTitle());
                desc.setPropertyClass(ClassLoaderUtil.loadClass(propertyDefinition.getDataType().getJavaClassName()));
                desc.setDefaultValue(propertyDefinition.getDefaultValue());
            }
            if (desc.getAssoc() != null) {
                AssociationDefinition associationDefinition = registry.getDictionaryService().getAssociation(desc.getPropertyQName());
                if (associationDefinition == null) {
                    throw new InternalApplicationException("No association found in Alfresco for " + desc + " of type " + typeDesc);
                }
                desc.setTitle(associationDefinition.getTitle());
                desc.setChildAssociation(associationDefinition.isChild());
                desc.setSourceAssociation(!Objects.equal(associationDefinition.getTargetClass().getName(), definition.getName()));
            }
        }
        typeDesc.setClassDefinitionLoaded(true);
    }

}
