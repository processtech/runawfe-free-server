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

import com.google.common.base.Function;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.service.delegate.Delegates;

public class SubstitutionDefinitions {
    private static final Log log = LogFactory.getLog(SubstitutionDefinitions.class);
    private static final List<FunctionDef> definitions = new ArrayList<>();

    static {
        ClassLoaderUtil.withExtensionResources("substitutions.xml", new Function<InputStream, Object>() {

            @Override
            public Object apply(InputStream input) {
                try (InputStream inputStream = input) {
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
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return null;
            }
        });
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
