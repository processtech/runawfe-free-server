package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.rest.dto.WfeBotStation;
import ru.runa.wfe.rest.dto.WfeUser;
import ru.runa.wfe.user.Actor;

@Mapper
public interface WfeBotStationMapper {
    WfeBotStation map(BotStation station);

    List<WfeBotStation> map(List<BotStation> stations);

    BotStation map(WfeBotStation station);
}
