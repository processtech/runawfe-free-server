package ru.runa.wfe.bot.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.QBotStation;
import ru.runa.wfe.commons.dao.GenericDao;

/**
 * DAO level interface for managing bot stations.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 * @since 2.0
 */
@Component
public class BotStationDao extends GenericDao<BotStation> {

    @Override
    protected void checkNotNull(BotStation entity, Object identity) {
        if (entity == null) {
            throw new BotStationDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * Load {@linkplain BotStation} from database by name.
     * 
     * @return loaded {@linkplain BotStation} or <code>null</code> if no bot
     *         station found
     */
    public BotStation get(String name) {
        QBotStation bs = QBotStation.botStation;
        return queryFactory.selectFrom(bs).where(bs.name.eq(name)).fetchFirst();
    }

    /**
     * Load {@linkplain BotStation} from database by name.
     * 
     * @return loaded {@linkplain BotStation}, not <code>null</code>
     */
    public BotStation getNotNull(String name) {
        BotStation botStation = get(name);
        checkNotNull(botStation, name);
        return botStation;
    }
}
