package ru.runa.wfe.bot.dao;

import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.commons.dao.GenericDAO;

/**
 * DAO level interface for managing bot stations.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 * @since 2.0
 */
public class BotStationDAO extends GenericDAO<BotStation> {

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
        return findFirstOrNull("from BotStation where name=?", name);
    }

    /**
     * Load {@linkplain BotStation} from database by name.
     * 
     * @return loaded {@linkplain BotStation}, not <code>null</code>
     */
    public BotStation getNotNull(String name) {
        BotStation botStation = findFirstOrNull("from BotStation where name=?", name);
        checkNotNull(botStation, name);
        return botStation;
    }

}
