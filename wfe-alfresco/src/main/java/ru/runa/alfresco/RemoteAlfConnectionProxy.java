package ru.runa.alfresco;

import java.util.Collection;
import java.util.List;

import org.alfresco.service.namespace.QName;

import ru.runa.alfresco.search.Search;

@SuppressWarnings("unchecked")
public class RemoteAlfConnectionProxy implements AlfConnection {

    @Override
    public void initializeTypeDefinition(final AlfTypeDesc typeDesc) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.initializeTypeDefinition(typeDesc);
                return null;
            }

        }.runInSession();
    }

    @Override
    public void createObject(final AlfObject object) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.createObject(object);
                return null;
            }

        }.runInSession();
    }

    @Override
    public void addAspect(final AlfObject object, final QName aspectTypeName) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.addAspect(object, aspectTypeName);
                return null;
            }

        }.runInSession();
    }

    @Override
    public void addChildAssociation(final AlfObject source, final AlfObject target, final QName associationTypeName) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.addChildAssociation(source, target, associationTypeName);
                return null;
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T loadObject(final String uuidRef) {
        return (T) new RemoteAlfConnector<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return alfConnection.loadObject(uuidRef);
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T loadObjectNotNull(final String uuidRef) {
        return (T) new RemoteAlfConnector<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return alfConnection.loadObjectNotNull(uuidRef);
            }

        }.runInSession();
    }

    @Override
    public void loadAssociation(final String uuidRef, @SuppressWarnings("rawtypes") final Collection collection, final AlfPropertyDesc desc) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.loadAssociation(uuidRef, collection, desc);
                return null;
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T findFirstObject(final Search search) {
        return (T) new RemoteAlfConnector<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return alfConnection.findFirstObject(search);
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> List<T> findObjects(final Search search) {
        return (List<T>) new RemoteAlfConnector<List<AlfObject>>() {

            @Override
            protected List<AlfObject> code() throws Exception {
                return alfConnection.findObjects(search);
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T findUniqueObject(final Search search) {
        return (T) new RemoteAlfConnector<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return alfConnection.findUniqueObject(search);
            }

        }.runInSession();
    }

    @Override
    public <T extends AlfObject> T findUniqueObjectNotNull(final Search search) {
        return (T) new RemoteAlfConnector<AlfObject>() {

            @Override
            protected AlfObject code() throws Exception {
                return alfConnection.findUniqueObjectNotNull(search);
            }

        }.runInSession();
    }

    @Override
    public void setContent(final AlfObject object, final byte[] data, final String mimetype) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.setContent(object, data, mimetype);
                return null;
            }

        }.runInSession();
    }

    @Override
    public void setContent(final AlfObject object, final String content, final String mimetype) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.setContent(object, content, mimetype);
                return null;
            }

        }.runInSession();
    }

    @Override
    public boolean updateObject(final AlfObject object, final boolean force) {
        return new RemoteAlfConnector<Boolean>() {

            @Override
            protected Boolean code() throws Exception {
                return alfConnection.updateObject(object, force);
            }

        }.runInSession();
    }

    @Override
    public boolean updateObject(final AlfObject object, final boolean force, final String comment) {
        return new RemoteAlfConnector<Boolean>() {

            @Override
            protected Boolean code() throws Exception {
                return alfConnection.updateObject(object, force, comment);
            }

        }.runInSession();
    }

    @Override
    public boolean updateObjectAssociations(final AlfObject object) {
        return new RemoteAlfConnector<Boolean>() {

            @Override
            protected Boolean code() throws Exception {
                return alfConnection.updateObjectAssociations(object);
            }

        }.runInSession();
    }

    @Override
    public void deleteObject(final AlfObject object) {
        new RemoteAlfConnector<Object>() {

            @Override
            protected Object code() throws Exception {
                alfConnection.deleteObject(object);
                return null;
            }

        }.runInSession();
    }

}
