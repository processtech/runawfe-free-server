package ru.runa.wfe.rest.dto;

import org.mapstruct.factory.Mappers;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.dto.WfVariable;

public class MapStructTest {

    public static void main(String[] args) {
        WfTaskMapper mapper = Mappers.getMapper(WfTaskMapper.class);
        WfTask task = new WfTask();
        Executor owner = new Actor("name", "desc");
        owner.setId(1L);
        task.setOwner(owner);
        task.setReadOnly(true);
        task.addVariable(new WfVariable("name1", "value1"));
        task.addVariable(new WfVariable("name2", "value2"));
        WfTaskDto dto = mapper.map(task);
        System.out.println(dto);
    }
}
