package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.rest.dto.WfeBotTask;

@Mapper(uses = WfeBotMapper.class)
public interface WfeBotTaskMapper {
    WfeBotTask map(BotTask task);

    List<WfeBotTask> map(List<BotTask> tasks);

    BotTask map(WfeBotTask task);
}
