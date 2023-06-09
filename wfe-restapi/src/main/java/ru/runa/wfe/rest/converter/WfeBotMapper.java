package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.rest.dto.WfeBot;

@Mapper(uses = WfeBotStationMapper.class)
public interface WfeBotMapper {
    WfeBot map(Bot bot);

    List<WfeBot> map(List<Bot> bots);

    Bot map(WfeBot bot);
}
