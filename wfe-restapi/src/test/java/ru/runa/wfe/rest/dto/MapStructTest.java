package ru.runa.wfe.rest.dto;

import org.mapstruct.factory.Mappers;
import ru.runa.wfe.rest.converter.WfeTaskMapper;
import ru.runa.wfe.rest.converter.WfeVariableDefinitionMapper;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.StringFormat;

public class MapStructTest {

    public static void main(String[] args) {
        // mapVariableDefinition();
        mapTask();
    }

    private static void mapTask() {
        WfeTaskMapper mapper = Mappers.getMapper(WfeTaskMapper.class);
        WfTask task = new WfTask();
        Executor owner = new Actor("name", "desc");
        owner.setId(1L);
        task.setOwner(owner);
        task.setReadOnly(true);
        task.addVariable(new WfVariable("name1", "value1"));
        task.addVariable(new WfVariable("name2", "value2"));
        WfeTask dto = mapper.map(task);
        System.out.println(dto);
    }

    private static void mapVariableDefinition() {
        WfeVariableDefinitionMapper mapper = Mappers.getMapper(WfeVariableDefinitionMapper.class);
        VariableDefinition variableDefinition = new VariableDefinition("x", "y");
        UserType userType = new UserType("xx");
        userType.addAttribute(new VariableDefinition("z", "y"));
        variableDefinition.setFormat(StringFormat.class.getName());
        variableDefinition.setUserType(userType);
        WfeVariableDefinition dto = mapper.map(variableDefinition);
        System.out.println(dto);
    }
}
