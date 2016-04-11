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
package ru.runa.wfe.definition.par;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.Deployment;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class ProcessArchive {
    private final Deployment deployment;
    public static final List<String> UNSECURED_FILE_NAMES = Lists.newArrayList();
    static {
        UNSECURED_FILE_NAMES.add(IFileDataProvider.START_IMAGE_FILE_NAME);
        UNSECURED_FILE_NAMES.add(IFileDataProvider.START_DISABLED_IMAGE_FILE_NAME);
        UNSECURED_FILE_NAMES.add(IFileDataProvider.BOTS_XML_FILE);
    }

    static List<ProcessArchiveParser> processArchiveParsers = new ArrayList<ProcessArchiveParser>();
    static {
        processArchiveParsers.add(ApplicationContextFactory.autowireBean(new FileArchiveParser()));
        processArchiveParsers.add(ApplicationContextFactory.autowireBean(new ProcessDefinitionParser()));
        processArchiveParsers.add(ApplicationContextFactory.autowireBean(new VariableDefinitionParser()));
        processArchiveParsers.add(ApplicationContextFactory.autowireBean(new InteractionsParser()));
        processArchiveParsers.add(ApplicationContextFactory.autowireBean(new TaskSubsitutionParser()));
        processArchiveParsers.add(ApplicationContextFactory.autowireBean(new GraphXmlParser()));
    }
    private static final Pattern SUBPROCESS_DEFINITION_PATTERN = Pattern.compile(IFileDataProvider.SUBPROCESS_DEFINITION_PREFIX + "(\\d*)."
            + IFileDataProvider.PROCESSDEFINITION_XML_FILE_NAME);

    private final Map<String, byte[]> fileData = Maps.newHashMap();

    public ProcessArchive(Deployment deployment) {
        try {
            this.deployment = deployment;
            ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(deployment.getContent()));
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String entryName = zipEntry.getName();
                byte[] bytes = ByteStreams.toByteArray(zis);
                if (bytes != null) {
                    fileData.put(entryName, bytes);
                }
                zipEntry = zis.getNextEntry();
            }
            zis.close();
        } catch (IOException e) {
            throw new DefinitionArchiveFormatException(e);
        }
    }

    public ProcessDefinition parseProcessDefinition() {
        ProcessDefinition processDefinition = new ProcessDefinition(deployment);
        for (ProcessArchiveParser processArchiveParser : processArchiveParsers) {
            processArchiveParser.readFromArchive(this, processDefinition);
        }
        for (Map.Entry<String, byte[]> entry : processDefinition.getProcessFiles().entrySet()) {
            Matcher matcher = SUBPROCESS_DEFINITION_PATTERN.matcher(entry.getKey());
            if (matcher.matches()) {
                int subprocessIndex = Integer.parseInt(matcher.group(1));
                SubprocessDefinition subprocessDefinition = new SubprocessDefinition(processDefinition);
                subprocessDefinition.setNodeId(IFileDataProvider.SUBPROCESS_DEFINITION_PREFIX + subprocessIndex);
                for (ProcessArchiveParser processArchiveParser : processArchiveParsers) {
                    if (processArchiveParser.isApplicableToEmbeddedSubprocess()) {
                        processArchiveParser.readFromArchive(this, subprocessDefinition);
                    }
                }
                processDefinition.addEmbeddedSubprocess(subprocessDefinition);
            }
        }
        processDefinition.mergeWithEmbeddedSubprocesses();
        return processDefinition;
    }

    public Map<String, byte[]> getFileData() {
        return fileData;
    }

}
