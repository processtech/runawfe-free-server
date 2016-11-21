package ru.runa.wfe.bot.dao;

import java.util.List;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.commons.dao.GenericDAO;

/**
 * DAO level interface for managing bot tasks.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class BotTaskDAO extends GenericDAO<BotTask> {

    @Override
    protected void checkNotNull(BotTask entity, Object identity) {
        if (entity == null) {
            throw new BotTaskDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * Load {@linkplain BotTask} from database by bot and name.
     * 
     * @return loaded {@linkplain BotTask} or <code>null</code>
     */
    public BotTask get(Bot bot, String name) {
        return findFirstOrNull("from BotTask where bot=? and name=?", bot, name);
    }

    /**
     * Load {@linkplain BotTask} from database by bot and name.
     * 
     * @return loaded {@linkplain BotTask}, not <code>null</code>
     */
    public BotTask getNotNull(Bot bot, String name) {
        BotTask botTask = get(bot, name);
        checkNotNull(botTask, name);
        return botTask;
    }

    /**
     * Load all {@linkplain BotTask}s defined for {@linkplain Bot}.
     * 
     * @return list, not <code>null</code>.
     */
    public List<BotTask> getAll(Bot bot) {
        return (List<BotTask>) getHibernateTemplate().find("from BotTask where bot=?", bot);
    }

}
