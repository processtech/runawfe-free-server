package ru.runa.wfe.var;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

@XmlAccessorType(XmlAccessType.FIELD)
public class UserType implements Serializable {
    private static final long serialVersionUID = -1054823598655227725L;
    public static final String DELIM = ".";
    private String name;
    private final List<VariableDefinition> attributes = Lists.newArrayList();
    private final Map<String, VariableDefinition> attributesMap = Maps.newHashMap();

    public UserType() {
    }

    public UserType(String name) {
        this.name = name.intern();
    }

    public String getName() {
        return name;
    }

    public void addAttribute(VariableDefinition variableDefinition) {
        attributes.add(variableDefinition);
        attributesMap.put(variableDefinition.getScriptingName(), variableDefinition);
        attributesMap.put(variableDefinition.getName(), variableDefinition);
    }

    public List<VariableDefinition> getAttributes() {
        return attributes;
    }

    public VariableDefinition getAttribute(String name) {
        int firstDotIndex = name.indexOf(UserType.DELIM);
        if (firstDotIndex != -1) {
            String attributeName = name.substring(0, firstDotIndex);
            VariableDefinition attributeDefinition = getAttribute(attributeName);
            if (attributeDefinition == null) {
                return null;
            }
            if (attributeDefinition.getUserType() == null) {
                throw new InternalApplicationException(String.format("Unable get attribute '%s' from non user type", name));
            }
            String nameRemainder = name.substring(firstDotIndex + 1);
            return attributeDefinition.getUserType().getAttribute(nameRemainder);
        }
        int componentStartIndex = name.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
        String attributeName = componentStartIndex != -1 ? name.substring(0, componentStartIndex) : name;
        VariableDefinition attributeDefinition = attributesMap.get(attributeName);
        if (attributeDefinition != null && componentStartIndex != -1) {
            VariableDefinition componentDefinition = new VariableDefinition(name, null, attributeDefinition.getFormatComponentClassNames()[0],
                    attributeDefinition.getFormatComponentUserTypes()[0]);
            return componentDefinition;
        }
        return attributeDefinition;
    }

    public VariableDefinition getAttributeNotNull(String name) {
        VariableDefinition definition = getAttribute(name);
        if (definition != null) {
            return definition;
        }
        throw new InternalApplicationException("No attribute '" + name + "' found in " + this);
    }

    public VariableDefinition getAttributeExpanded(String name) {
        int firstDotIndex = name.indexOf(UserType.DELIM);
        if (firstDotIndex != -1) {
            String attributeName = name.substring(0, firstDotIndex);
            VariableDefinition attributeDefinition = getAttribute(attributeName);
            if (attributeDefinition == null) {
                return null;
            }
            if (attributeDefinition.getUserType() == null) {
                throw new InternalApplicationException(String.format(String.format("Unable get attribute '%s' from non user type", name)));
            }
            String nameRemainder = name.substring(firstDotIndex + 1);
            VariableDefinition innerAttributeDefinition = attributeDefinition.getUserType().getAttributeExpanded(nameRemainder);
            if (innerAttributeDefinition == null) {
                return null;
            }
            VariableDefinition expandedDefinition = new VariableDefinition(attributeDefinition.getName() + UserType.DELIM
                    + innerAttributeDefinition.getName(), attributeDefinition.getScriptingName() + UserType.DELIM
                    + innerAttributeDefinition.getScriptingName(), innerAttributeDefinition);
            return expandedDefinition;
        }
        int componentStartIndex = name.indexOf(VariableFormatContainer.COMPONENT_QUALIFIER_START);
        String attributeName = componentStartIndex != -1 ? name.substring(0, componentStartIndex) : name;
        VariableDefinition attributeDefinition = attributesMap.get(attributeName);
        if (attributeDefinition != null && componentStartIndex != -1) {
            VariableDefinition componentDefinition = new VariableDefinition(attributeDefinition.getName() + name.substring(componentStartIndex),
                    attributeDefinition.getScriptingName() + name.substring(componentStartIndex),
                    attributeDefinition.getFormatComponentClassNames()[0], attributeDefinition.getFormatComponentUserTypes()[0]);
            return componentDefinition;
        }
        return attributeDefinition;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, attributes);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UserType)) {
            return false;
        }
        UserType type = (UserType) obj;
        return Objects.equal(name, type.name) && Objects.equal(attributes, type.attributes);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(getClass()).add("name", name).add("attributes", attributes).toString();
    }

}
