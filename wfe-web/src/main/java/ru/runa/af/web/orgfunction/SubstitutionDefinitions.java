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
package ru.runa.af.web.orgfunction;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Preconditions;

@SuppressWarnings("unchecked")
public class SubstitutionDefinitions {
    private static final Log log = LogFactory.getLog(SubstitutionDefinitions.class);
    private static final String CONFIG = "substitutions.xml";
    private static final List<FunctionDef> definitions = new ArrayList<FunctionDef>();

    static {
        registerDefinitions(ClassLoaderUtil.getAsStream(CONFIG, SubstitutionDefinitions.class));
        try {
            String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + SystemProperties.RESOURCE_EXTENSION_PREFIX + CONFIG;
            Resource[] resources = ClassLoaderUtil.getResourcePatternResolver().getResources(pattern);
            for (Resource resource : resources) {
                registerDefinitions(resource.getInputStream());
            }
        } catch (IOException e) {
            log.error("unable load wfe.custom substitution definitions", e);
        }
    }

    private static void registerDefinitions(InputStream inputStream) {
        try {
            Preconditions.checkNotNull(inputStream);
            Document document = XmlUtils.parseWithoutValidation(inputStream);
            List<Element> oElements = document.getRootElement().elements("function");
            for (Element oElement : oElements) {
                String className = oElement.attributeValue("class");
                String label = Delegates.getSystemService().getLocalized(className);
                FunctionDef fDef = new FunctionDef(className, label);
                List<Element> pElements = oElement.elements("param");
                for (Element pElement : pElements) {
                    String rendererClassName = pElement.attributeValue("renderer");
                    if (rendererClassName == null) {
                        rendererClassName = StringRenderer.class.getName();
                    }
                    ParamRenderer renderer = ClassLoaderUtil.instantiate(rendererClassName);
                    ParamDef pDef = new ParamDef(pElement.attributeValue("messageKey"), pElement.attributeValue("message"), renderer);
                    fDef.addParam(pDef);
                }
                definitions.add(fDef);
            }
            inputStream.close();
        } catch (Exception e) {
            log.error("unable load substitution definitions", e);
        }
    }

    public static List<FunctionDef> getAll() {
        return definitions;
    }

    public static FunctionDef getByClassNameNotNull(String className) {
        for (FunctionDef definition : getAll()) {
            if (definition.getClassName().equals(className)) {
                return definition;
            }
        }
        throw new InternalApplicationException("No substitution definition found by name '" + className + "'");
    }
}
