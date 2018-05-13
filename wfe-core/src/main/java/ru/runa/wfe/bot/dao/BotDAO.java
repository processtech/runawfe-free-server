/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.bot.dao;

import java.util.List;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.dao.GenericDAO;
import ru.runa.wfe.user.User;

/**
 * DAO level interface for managing bots.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 * @since 2.0
 */
@SuppressWarnings("unchecked")
public class BotDAO extends GenericDAO<Bot> {

    @Override
    protected void checkNotNull(Bot entity, Object identity) {
        if (entity == null) {
            throw new BotDoesNotExistException(String.valueOf(identity));
        }
    }

    /**
     * Load {@linkplain Bot} from database by bot station and userName.
     * 
     * @return loaded {@linkplain Bot} or <code>null</code> if no bot found
     */
    public Bot get(BotStation botStation, String username) {
        return findFirstOrNull("from Bot where botStation=? and username=?", botStation, username);
    }

    /**
     * Load {@linkplain Bot} from database by userName (botstation is not
     * specified so there can be more that 1 bot).
     * 
     * @return loaded {@linkplain Bot} or <code>null</code> if no bot found
     */
    public Bot get(String username) {
        return findFirstOrNull("from Bot where username=?", username);
    }

    public boolean isBot(User u) {
        return sessionFactory.getCurrentSession()
                .createQuery("select 0 from Bot where username=?")
                .setParameter(0, u.getName())
                .setMaxResults(1).uniqueResult() != null;
    }

    /**
     * Load {@linkplain Bot} from database by bot station and userName.
     * 
     * @return loaded {@linkplain Bot}
     */
    public Bot getNotNull(BotStation botStation, String username) {
        Bot bot = get(botStation, username);
        checkNotNull(bot, username);
        return bot;
    }

    /**
     * Load all {@linkplain Bot}s defined for {@linkplain BotStation}.
     */
    public List<Bot> getAll(final BotStation botStation) {
        return (List<Bot>) getHibernateTemplate().find("from Bot where botStation=?", botStation);
    }
}
