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
package ru.runa.af.web.system;

import com.google.common.io.Closer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.jar.JarInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.io.FilenameUtils;
import ru.runa.wf.logic.bot.BotStationResources;
import ru.runa.wfe.commons.AppServer;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.IoCommons;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.extension.TaskHandler;

/**
 * User: stan79
 * 
 * @since 3.0
 */
@CommonsLog
public class TaskHandlerClassesInformation {
    private static final SortedSet<String> taskHandlerImplementationClasses = new TreeSet<>();

    static {
        init();
    }

    private static void init() {
        String deploymentDirPath = IoCommons.getDeploymentDirPath();
        String earFilePath = deploymentDirPath + "/" + SystemProperties.getEARFileName();
        Closer closer = Closer.create();
        try {
            ZipInputStream earInputStream = closer.register(new ZipInputStream(new FileInputStream(earFilePath)));
            ZipEntry entry;
            while ((entry = earInputStream.getNextEntry()) != null) {
                if (entry.getName().endsWith(".jar")) {
                    searchInJar(entry.getName(), new JarInputStream(earInputStream));
                }
            }
            if (IoCommons.getAppServer() == AppServer.JBOSS4) {
                File deploymentDirectory = new File(deploymentDirPath);
                log.debug("Searching in deployment directory: " + deploymentDirectory);
                for (File file : IoCommons.getJarFiles(deploymentDirectory)) {
                    JarInputStream jarInputStream = closer.register(new JarInputStream(new FileInputStream(file)));
                    searchInJar(file.getName(), jarInputStream);
                }
            }
            File extensionDirectory = new File(IoCommons.getExtensionDirPath());
            if (extensionDirectory.exists() && extensionDirectory.isDirectory()) {
                log.debug("Searching in extension directory: " + extensionDirectory);
                for (File file : IoCommons.getJarFiles(extensionDirectory)) {
                    JarInputStream jarInputStream = closer.register(new JarInputStream(new FileInputStream(file)));
                    searchInJar(file.getName(), jarInputStream);
                }
            } else {
                log.debug("No extension directory found: " + extensionDirectory);
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                closer.close();
            } catch (IOException e) {
                log.warn(e);
            }
        }
    }

    private static void searchInJar(String jarName, JarInputStream jarInputStream) throws IOException {
        boolean matches = false;
        for (String patternFileName : BotStationResources.getTaskHandlerJarNames()) {
            if (FilenameUtils.wildcardMatch(jarName, patternFileName)) {
                matches = true;
                break;
            }
        }
        if (!matches) {
            log.debug("Ignored " + jarName);
            return;
        }
        log.info("Searching in " + jarName);
        ZipEntry entry;
        while ((entry = jarInputStream.getNextEntry()) != null) {
            if (entry.getName().endsWith(".class")) {
                try {
                    String className = entry.getName();
                    int lastIndexOfDotSymbol = className.lastIndexOf('.');
                    className = className.substring(0, lastIndexOfDotSymbol).replace('/', '.');
                    // If we can't load class - just move to next class.
                    Class<?> someClass = ClassLoaderUtil.loadClass(className);
                    if (TaskHandler.class.isAssignableFrom(someClass) && !Modifier.isAbstract(someClass.getModifiers())) {
                        taskHandlerImplementationClasses.add(someClass.getCanonicalName());
                    }
                } catch (Throwable e) {
                    log.warn("Error on loading task handler for " + e.getMessage());
                }
            }
        }
    }

    public static SortedSet<String> getClassNames() {
        return Collections.unmodifiableSortedSet(taskHandlerImplementationClasses);
    }

    public static boolean isValid(String taskHandlerClassName) {
        return taskHandlerImplementationClasses.contains(taskHandlerClassName);
    }
}
