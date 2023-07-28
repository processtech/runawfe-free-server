package ru.runa.wfe.restapi;

import ru.runa.wfe.restapi.client.AuthControllerApi;
import ru.runa.wfe.restapi.client.DefinitionControllerApi;
import ru.runa.wfe.restapi.model.WfeCredentials;
import ru.runa.wfe.restapi.model.WfePagedListFilter;
import ru.runa.wfe.restapi.model.WfePagedListOfWfeProcessDefinition;

public class Test1 {

    public static void main(String[] args) {
        ApiClient apiClient = new ApiClient();
        AuthControllerApi authControllerApi = new AuthControllerApi(apiClient);
        WfeCredentials wfeCredentials = new WfeCredentials();
        wfeCredentials.setLogin("Administrator");
        wfeCredentials.setPassword("wf");
        String token = authControllerApi.basicUsingPOST(wfeCredentials);
        System.out.println("Token obtained: " + token);
        apiClient.setBearerToken(token);
        DefinitionControllerApi definitionControllerApi = new DefinitionControllerApi(apiClient);
        WfePagedListFilter filter = new WfePagedListFilter();
        WfePagedListOfWfeProcessDefinition listOfWfeProcessDefinition = definitionControllerApi.getProcessDefinitionsUsingPOST(filter);
        System.out.println(listOfWfeProcessDefinition.getTotal());
        if (listOfWfeProcessDefinition.getTotal() > 0) {
            System.out.println(listOfWfeProcessDefinition.getData().get(0));
        }
    }

}
