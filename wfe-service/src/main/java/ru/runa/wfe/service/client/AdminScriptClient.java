/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.service.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.IOCommons;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.TransactionScopeDto;
import ru.runa.wfe.script.common.TransactionScopeType;
import ru.runa.wfe.script.common.WorkflowScriptDto;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;

/**
 * Created on 12.12.2005
 * 
 */
public class AdminScriptClient {
    private static final String DEPLOY_PROCESS_DEFINITION_TAG_NAME = "deployProcessDefinition";
    private static final String FILE_ATTRIBUTE_NAME = "file";

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: AdminScriptRunner <scriptpath> <username> <password>");
            System.out.println("Example: AdminScriptRunner /home/foo/wfescript.xml foo $eCreTw0rd");
            System.exit(-1);
        }
        File file = new File(args[0]);
        if (!file.exists()) {
            System.out.println("Config file " + args[0] + " does not exist");
            System.exit(-1);
        }
        try {
            byte[] scriptBytes = Files.toByteArray(file);
            User user = Delegates.getAuthenticationService().authenticateByLoginPassword(args[1], args[2]);
            run(user, scriptBytes, new Handler() {

                @Override
                public void onTransactionException(Exception e) {
                    System.out.println(e.getMessage());
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void run(User user, byte[] scriptBytes, Handler handler) throws IOException, JAXBException {

        Unmarshaller unmarshaller = JAXBContext.newInstance(WorkflowScriptDto.class).createUnmarshaller();
        WorkflowScriptDto data = (WorkflowScriptDto) unmarshaller.unmarshal(new ByteArrayInputStream(scriptBytes));
        data.validate(true);
        List<List<ScriptOperation>> splitScriptToTransactions = splitScriptToTransactions(data);
        if (!data.transactionScopes.isEmpty()) {
            System.out.println("multiple docs [by <transactionScope>]: " + splitScriptToTransactions.size());
        } else {
            System.out.println("multiple docs [by defaultTransactionScope]: " + splitScriptToTransactions.size());
        }
        Map<String, byte[]> externalResources = readExternalResources(data);
        for (List<ScriptOperation> operations : splitScriptToTransactions) {
            WorkflowScriptDto scriptPart = new WorkflowScriptDto();
            scriptPart.identitySets = data.identitySets;
            scriptPart.operations = operations;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JAXBContext.newInstance(WorkflowScriptDto.class).createMarshaller().marshal(scriptPart, outputStream);
            byte[] scriptPartData = outputStream.toByteArray();
            try {
                handler.onStartTransaction(scriptPartData);
                Delegates.getScriptingService().executeAdminScript(user, scriptPartData, externalResources);
                handler.onEndTransaction();
            } catch (Exception e) {
                handler.onTransactionException(e);
            }
        }
    }

    private static List<List<ScriptOperation>> splitScriptToTransactions(WorkflowScriptDto script) {
        if (!script.operations.isEmpty()) {
            TransactionScopeType transactionType = script.defaultTransactionScope == null ? TransactionScopeType.TRANSACTION_PER_OPERATION
                    : script.defaultTransactionScope;
            return splitScopeToTransactions(script.operations, transactionType);
        }
        List<List<ScriptOperation>> result = Lists.newArrayList();
        for (TransactionScopeDto transactionScope : script.transactionScopes) {
            TransactionScopeType transactionType = transactionScope.transactionScope;
            if (transactionType == null) {
                transactionType = script.defaultTransactionScope;
            }
            if (transactionType == null) {
                transactionType = TransactionScopeType.TRANSACTION_PER_SCOPE;
            }
            result.addAll(splitScopeToTransactions(transactionScope.operations, transactionType));
        }
        return result;
    }

    private static List<List<ScriptOperation>> splitScopeToTransactions(List<ScriptOperation> scopeOperations, TransactionScopeType transactionType) {
        List<List<ScriptOperation>> result = Lists.newArrayList();
        if (transactionType == TransactionScopeType.TRANSACTION_PER_SCOPE) {
            result.add(scopeOperations);
        } else {
            for (ScriptOperation op : scopeOperations) {
                result.add(Lists.newArrayList(op));
            }
        }
        return result;
    }

    private static Map<String, byte[]> readExternalResources(WorkflowScriptDto script) throws IOException {
        List<String> externalResourceNames = script.getExternalResourceNames();
        Map<String, byte[]> result = Maps.newHashMap();
        for (String resource : externalResourceNames) {
            File processFile = new File(resource);
            if (!processFile.exists()) {
                result.put(resource, ByteStreams.toByteArray(ClassLoaderUtil.getAsStreamNotNull(resource, Bot.class)));
            } else if (processFile.isFile()) {
                result.put(resource, Files.toByteArray(new File(resource)));
            } else {
                result.put(resource, IOCommons.jarToBytesArray(processFile));
            }
        }
        return result;
    }

    public static class Handler {

        public void onStartTransaction(byte[] script) {
        }

        public void onEndTransaction() {
        }

        public void onTransactionException(Exception e) {
        }
    }
}
