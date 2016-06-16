
package ru.runa.wfe.webservice;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the ru.runa.wfe.webservice package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetCriteriaByName_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getCriteriaByName");
    private final static QName _UpdateCriteria_QNAME = new QName("http://impl.service.wfe.runa.ru/", "updateCriteria");
    private final static QName _GetSubstitutionsByCriteria_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getSubstitutionsByCriteria");
    private final static QName _DeleteSubstitutions_QNAME = new QName("http://impl.service.wfe.runa.ru/", "deleteSubstitutions");
    private final static QName _UpdateCriteriaResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "updateCriteriaResponse");
    private final static QName _GetCriteriaResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getCriteriaResponse");
    private final static QName _DeleteCriteria_QNAME = new QName("http://impl.service.wfe.runa.ru/", "deleteCriteria");
    private final static QName _GetSubstitution_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getSubstitution");
    private final static QName _GetAllCriteriasResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getAllCriteriasResponse");
    private final static QName _GetSubstitutionsByCriteriaResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getSubstitutionsByCriteriaResponse");
    private final static QName _GetSubstitutionsResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getSubstitutionsResponse");
    private final static QName _CreateSubstitution_QNAME = new QName("http://impl.service.wfe.runa.ru/", "createSubstitution");
    private final static QName _DeleteCriterias_QNAME = new QName("http://impl.service.wfe.runa.ru/", "deleteCriterias");
    private final static QName _GetSubstitutions_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getSubstitutions");
    private final static QName _DeleteCriteriaResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "deleteCriteriaResponse");
    private final static QName _UpdateSubstitution_QNAME = new QName("http://impl.service.wfe.runa.ru/", "updateSubstitution");
    private final static QName _CreateCriteria_QNAME = new QName("http://impl.service.wfe.runa.ru/", "createCriteria");
    private final static QName _DeleteSubstitutionsResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "deleteSubstitutionsResponse");
    private final static QName _GetSubstitutionResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getSubstitutionResponse");
    private final static QName _DeleteCriteriasResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "deleteCriteriasResponse");
    private final static QName _GetCriteriaByNameResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getCriteriaByNameResponse");
    private final static QName _GetCriteria_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getCriteria");
    private final static QName _CreateSubstitutionResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "createSubstitutionResponse");
    private final static QName _UpdateSubstitutionResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "updateSubstitutionResponse");
    private final static QName _GetAllCriterias_QNAME = new QName("http://impl.service.wfe.runa.ru/", "getAllCriterias");
    private final static QName _CreateCriteriaResponse_QNAME = new QName("http://impl.service.wfe.runa.ru/", "createCriteriaResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: ru.runa.wfe.webservice
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DeleteCriteria }
     * 
     */
    public DeleteCriteria createDeleteCriteria() {
        return new DeleteCriteria();
    }

    /**
     * Create an instance of {@link CreateCriteria }
     * 
     */
    public CreateCriteria createCreateCriteria() {
        return new CreateCriteria();
    }

    /**
     * Create an instance of {@link UpdateSubstitutionResponse }
     * 
     */
    public UpdateSubstitutionResponse createUpdateSubstitutionResponse() {
        return new UpdateSubstitutionResponse();
    }

    /**
     * Create an instance of {@link GetSubstitutionsByCriteria }
     * 
     */
    public GetSubstitutionsByCriteria createGetSubstitutionsByCriteria() {
        return new GetSubstitutionsByCriteria();
    }

    /**
     * Create an instance of {@link UpdateCriteria }
     * 
     */
    public UpdateCriteria createUpdateCriteria() {
        return new UpdateCriteria();
    }

    /**
     * Create an instance of {@link DeleteSubstitutionsResponse }
     * 
     */
    public DeleteSubstitutionsResponse createDeleteSubstitutionsResponse() {
        return new DeleteSubstitutionsResponse();
    }

    /**
     * Create an instance of {@link Actor }
     * 
     */
    public Actor createActor() {
        return new Actor();
    }

    /**
     * Create an instance of {@link GetSubstitutions }
     * 
     */
    public GetSubstitutions createGetSubstitutions() {
        return new GetSubstitutions();
    }

    /**
     * Create an instance of {@link Substitution }
     * 
     */
    public Substitution createSubstitution() {
        return new Substitution();
    }

    /**
     * Create an instance of {@link GetCriteriaResponse }
     * 
     */
    public GetCriteriaResponse createGetCriteriaResponse() {
        return new GetCriteriaResponse();
    }

    /**
     * Create an instance of {@link CreateSubstitutionResponse }
     * 
     */
    public CreateSubstitutionResponse createCreateSubstitutionResponse() {
        return new CreateSubstitutionResponse();
    }

    /**
     * Create an instance of {@link GetCriteriaByName }
     * 
     */
    public GetCriteriaByName createGetCriteriaByName() {
        return new GetCriteriaByName();
    }

    /**
     * Create an instance of {@link GetSubstitutionsResponse }
     * 
     */
    public GetSubstitutionsResponse createGetSubstitutionsResponse() {
        return new GetSubstitutionsResponse();
    }

    /**
     * Create an instance of {@link GetAllCriteriasResponse }
     * 
     */
    public GetAllCriteriasResponse createGetAllCriteriasResponse() {
        return new GetAllCriteriasResponse();
    }

    /**
     * Create an instance of {@link DeleteSubstitutions }
     * 
     */
    public DeleteSubstitutions createDeleteSubstitutions() {
        return new DeleteSubstitutions();
    }

    /**
     * Create an instance of {@link GetCriteriaByNameResponse }
     * 
     */
    public GetCriteriaByNameResponse createGetCriteriaByNameResponse() {
        return new GetCriteriaByNameResponse();
    }

    /**
     * Create an instance of {@link DeleteCriteriasResponse }
     * 
     */
    public DeleteCriteriasResponse createDeleteCriteriasResponse() {
        return new DeleteCriteriasResponse();
    }

    /**
     * Create an instance of {@link CreateSubstitution }
     * 
     */
    public CreateSubstitution createCreateSubstitution() {
        return new CreateSubstitution();
    }

    /**
     * Create an instance of {@link WfExecutor }
     * 
     */
    public WfExecutor createWfExecutor() {
        return new WfExecutor();
    }

    /**
     * Create an instance of {@link GetSubstitutionsByCriteriaResponse }
     * 
     */
    public GetSubstitutionsByCriteriaResponse createGetSubstitutionsByCriteriaResponse() {
        return new GetSubstitutionsByCriteriaResponse();
    }

    /**
     * Create an instance of {@link UpdateCriteriaResponse }
     * 
     */
    public UpdateCriteriaResponse createUpdateCriteriaResponse() {
        return new UpdateCriteriaResponse();
    }

    /**
     * Create an instance of {@link UpdateSubstitution }
     * 
     */
    public UpdateSubstitution createUpdateSubstitution() {
        return new UpdateSubstitution();
    }

    /**
     * Create an instance of {@link GetSubstitutionResponse }
     * 
     */
    public GetSubstitutionResponse createGetSubstitutionResponse() {
        return new GetSubstitutionResponse();
    }

    /**
     * Create an instance of {@link GetAllCriterias }
     * 
     */
    public GetAllCriterias createGetAllCriterias() {
        return new GetAllCriterias();
    }

    /**
     * Create an instance of {@link DeleteCriteriaResponse }
     * 
     */
    public DeleteCriteriaResponse createDeleteCriteriaResponse() {
        return new DeleteCriteriaResponse();
    }

    /**
     * Create an instance of {@link GetCriteria }
     * 
     */
    public GetCriteria createGetCriteria() {
        return new GetCriteria();
    }

    /**
     * Create an instance of {@link SubstitutionCriteria }
     * 
     */
    public SubstitutionCriteria createSubstitutionCriteria() {
        return new SubstitutionCriteria();
    }

    /**
     * Create an instance of {@link CreateCriteriaResponse }
     * 
     */
    public CreateCriteriaResponse createCreateCriteriaResponse() {
        return new CreateCriteriaResponse();
    }

    /**
     * Create an instance of {@link GetSubstitution }
     * 
     */
    public GetSubstitution createGetSubstitution() {
        return new GetSubstitution();
    }

    /**
     * Create an instance of {@link DeleteCriterias }
     * 
     */
    public DeleteCriterias createDeleteCriterias() {
        return new DeleteCriterias();
    }

    /**
     * Create an instance of {@link User }
     * 
     */
    public User createUser() {
        return new User();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCriteriaByName }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getCriteriaByName")
    public JAXBElement<GetCriteriaByName> createGetCriteriaByName(GetCriteriaByName value) {
        return new JAXBElement<GetCriteriaByName>(_GetCriteriaByName_QNAME, GetCriteriaByName.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateCriteria }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "updateCriteria")
    public JAXBElement<UpdateCriteria> createUpdateCriteria(UpdateCriteria value) {
        return new JAXBElement<UpdateCriteria>(_UpdateCriteria_QNAME, UpdateCriteria.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubstitutionsByCriteria }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getSubstitutionsByCriteria")
    public JAXBElement<GetSubstitutionsByCriteria> createGetSubstitutionsByCriteria(GetSubstitutionsByCriteria value) {
        return new JAXBElement<GetSubstitutionsByCriteria>(_GetSubstitutionsByCriteria_QNAME, GetSubstitutionsByCriteria.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteSubstitutions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "deleteSubstitutions")
    public JAXBElement<DeleteSubstitutions> createDeleteSubstitutions(DeleteSubstitutions value) {
        return new JAXBElement<DeleteSubstitutions>(_DeleteSubstitutions_QNAME, DeleteSubstitutions.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateCriteriaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "updateCriteriaResponse")
    public JAXBElement<UpdateCriteriaResponse> createUpdateCriteriaResponse(UpdateCriteriaResponse value) {
        return new JAXBElement<UpdateCriteriaResponse>(_UpdateCriteriaResponse_QNAME, UpdateCriteriaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCriteriaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getCriteriaResponse")
    public JAXBElement<GetCriteriaResponse> createGetCriteriaResponse(GetCriteriaResponse value) {
        return new JAXBElement<GetCriteriaResponse>(_GetCriteriaResponse_QNAME, GetCriteriaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteCriteria }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "deleteCriteria")
    public JAXBElement<DeleteCriteria> createDeleteCriteria(DeleteCriteria value) {
        return new JAXBElement<DeleteCriteria>(_DeleteCriteria_QNAME, DeleteCriteria.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubstitution }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getSubstitution")
    public JAXBElement<GetSubstitution> createGetSubstitution(GetSubstitution value) {
        return new JAXBElement<GetSubstitution>(_GetSubstitution_QNAME, GetSubstitution.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllCriteriasResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getAllCriteriasResponse")
    public JAXBElement<GetAllCriteriasResponse> createGetAllCriteriasResponse(GetAllCriteriasResponse value) {
        return new JAXBElement<GetAllCriteriasResponse>(_GetAllCriteriasResponse_QNAME, GetAllCriteriasResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubstitutionsByCriteriaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getSubstitutionsByCriteriaResponse")
    public JAXBElement<GetSubstitutionsByCriteriaResponse> createGetSubstitutionsByCriteriaResponse(GetSubstitutionsByCriteriaResponse value) {
        return new JAXBElement<GetSubstitutionsByCriteriaResponse>(_GetSubstitutionsByCriteriaResponse_QNAME, GetSubstitutionsByCriteriaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubstitutionsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getSubstitutionsResponse")
    public JAXBElement<GetSubstitutionsResponse> createGetSubstitutionsResponse(GetSubstitutionsResponse value) {
        return new JAXBElement<GetSubstitutionsResponse>(_GetSubstitutionsResponse_QNAME, GetSubstitutionsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateSubstitution }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "createSubstitution")
    public JAXBElement<CreateSubstitution> createCreateSubstitution(CreateSubstitution value) {
        return new JAXBElement<CreateSubstitution>(_CreateSubstitution_QNAME, CreateSubstitution.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteCriterias }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "deleteCriterias")
    public JAXBElement<DeleteCriterias> createDeleteCriterias(DeleteCriterias value) {
        return new JAXBElement<DeleteCriterias>(_DeleteCriterias_QNAME, DeleteCriterias.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubstitutions }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getSubstitutions")
    public JAXBElement<GetSubstitutions> createGetSubstitutions(GetSubstitutions value) {
        return new JAXBElement<GetSubstitutions>(_GetSubstitutions_QNAME, GetSubstitutions.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteCriteriaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "deleteCriteriaResponse")
    public JAXBElement<DeleteCriteriaResponse> createDeleteCriteriaResponse(DeleteCriteriaResponse value) {
        return new JAXBElement<DeleteCriteriaResponse>(_DeleteCriteriaResponse_QNAME, DeleteCriteriaResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateSubstitution }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "updateSubstitution")
    public JAXBElement<UpdateSubstitution> createUpdateSubstitution(UpdateSubstitution value) {
        return new JAXBElement<UpdateSubstitution>(_UpdateSubstitution_QNAME, UpdateSubstitution.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateCriteria }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "createCriteria")
    public JAXBElement<CreateCriteria> createCreateCriteria(CreateCriteria value) {
        return new JAXBElement<CreateCriteria>(_CreateCriteria_QNAME, CreateCriteria.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteSubstitutionsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "deleteSubstitutionsResponse")
    public JAXBElement<DeleteSubstitutionsResponse> createDeleteSubstitutionsResponse(DeleteSubstitutionsResponse value) {
        return new JAXBElement<DeleteSubstitutionsResponse>(_DeleteSubstitutionsResponse_QNAME, DeleteSubstitutionsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetSubstitutionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getSubstitutionResponse")
    public JAXBElement<GetSubstitutionResponse> createGetSubstitutionResponse(GetSubstitutionResponse value) {
        return new JAXBElement<GetSubstitutionResponse>(_GetSubstitutionResponse_QNAME, GetSubstitutionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteCriteriasResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "deleteCriteriasResponse")
    public JAXBElement<DeleteCriteriasResponse> createDeleteCriteriasResponse(DeleteCriteriasResponse value) {
        return new JAXBElement<DeleteCriteriasResponse>(_DeleteCriteriasResponse_QNAME, DeleteCriteriasResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCriteriaByNameResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getCriteriaByNameResponse")
    public JAXBElement<GetCriteriaByNameResponse> createGetCriteriaByNameResponse(GetCriteriaByNameResponse value) {
        return new JAXBElement<GetCriteriaByNameResponse>(_GetCriteriaByNameResponse_QNAME, GetCriteriaByNameResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetCriteria }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getCriteria")
    public JAXBElement<GetCriteria> createGetCriteria(GetCriteria value) {
        return new JAXBElement<GetCriteria>(_GetCriteria_QNAME, GetCriteria.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateSubstitutionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "createSubstitutionResponse")
    public JAXBElement<CreateSubstitutionResponse> createCreateSubstitutionResponse(CreateSubstitutionResponse value) {
        return new JAXBElement<CreateSubstitutionResponse>(_CreateSubstitutionResponse_QNAME, CreateSubstitutionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link UpdateSubstitutionResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "updateSubstitutionResponse")
    public JAXBElement<UpdateSubstitutionResponse> createUpdateSubstitutionResponse(UpdateSubstitutionResponse value) {
        return new JAXBElement<UpdateSubstitutionResponse>(_UpdateSubstitutionResponse_QNAME, UpdateSubstitutionResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetAllCriterias }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "getAllCriterias")
    public JAXBElement<GetAllCriterias> createGetAllCriterias(GetAllCriterias value) {
        return new JAXBElement<GetAllCriterias>(_GetAllCriterias_QNAME, GetAllCriterias.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link CreateCriteriaResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://impl.service.wfe.runa.ru/", name = "createCriteriaResponse")
    public JAXBElement<CreateCriteriaResponse> createCreateCriteriaResponse(CreateCriteriaResponse value) {
        return new JAXBElement<CreateCriteriaResponse>(_CreateCriteriaResponse_QNAME, CreateCriteriaResponse.class, null, value);
    }

}
