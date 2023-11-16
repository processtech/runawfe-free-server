package ru.runa.af.web.orgfunction;

import com.google.common.base.Function;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.service.delegate.Delegates;

@CommonsLog
public class SubstitutionCriteriaDefinitions {

    private List<FunctionDef> definitions = new ArrayList<>();
    private static SubstitutionCriteriaDefinitions instance;

    private SubstitutionCriteriaDefinitions() {
    }

    public static synchronized SubstitutionCriteriaDefinitions getInstance() {
        if (instance == null) {
            instance = new SubstitutionCriteriaDefinitions();
        }
        return instance;
    }

    public void parseConfiguration() {
        ClassLoaderUtil.withExtensionResources("substitution.criterias.xml", new Function<InputStream, Object>() {

            @Override
            public Object apply(InputStream input) {
                try (InputStream inputStream = input) {
                    Document document = XmlUtils.parseWithoutValidation(inputStream);
                    List<Element> oElements = document.getRootElement().elements("type");
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

    public List<FunctionDef> getAll() {
        return definitions;
    }

    public FunctionDef getByClassName(String className) {
        for (FunctionDef definition : getAll()) {
            if (definition.getClassName().equals(className)) {
                return definition;
            }
        }
        return null;
    }
}
