package ru.runa.wfe.datafile.builder;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.logic.RelationLogic;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;

@Component
public class RelationDataFileBuilder implements DataFileBuilder {

    @Autowired
    private RelationLogic relationLogic;

    @Override
    public void build(ZipOutputStream zos, Document script, User user) {
        List<Relation> relations = relationLogic.getRelations(user, BatchPresentationFactory.RELATIONS.createNonPaged());
        for (Relation relation : relations) {
            if (Strings.isNullOrEmpty(relation.getName())) {
                continue;
            }
            Map<Executor, List<Executor>> map = Maps.newHashMap();
            List<RelationPair> relationPairs = relationLogic.getRelations(user, relation.getName(),
                    BatchPresentationFactory.RELATION_PAIRS.createNonPaged());
            for (RelationPair relationPair : relationPairs) {
                List<Executor> list = map.get(relationPair.getLeft());
                if (list == null) {
                    list = Lists.newArrayList();
                    map.put(relationPair.getLeft(), list);
                }
                list.add(relationPair.getRight());
            }
            for (Map.Entry<Executor, List<Executor>> entry : map.entrySet()) {
                Element relationElement = script.getRootElement().addElement("relation", XmlUtils.RUNA_NAMESPACE);
                relationElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, relation.getName());
                Element leftElement = relationElement.addElement("left", XmlUtils.RUNA_NAMESPACE);
                Element rightElement = relationElement.addElement("right", XmlUtils.RUNA_NAMESPACE);
                populateExecutor(leftElement, entry.getKey());
                for (Executor rightExecutor : entry.getValue()) {
                    populateExecutor(rightElement, rightExecutor);
                }
            }
        }
    }

    private void populateExecutor(Element element, Executor executor) {
        Element executorElement = element.addElement(AdminScriptConstants.EXECUTOR_ELEMENT_NAME, XmlUtils.RUNA_NAMESPACE);
        executorElement.addAttribute(AdminScriptConstants.NAME_ATTRIBUTE_NAME, executor.getName());
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
