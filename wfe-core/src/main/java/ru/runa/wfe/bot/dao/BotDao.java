package ru.runa.wfe.bot.dao;

import java.util.List;
import lombok.val;
import org.springframework.stereotype.Component;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.QBot;
import ru.runa.wfe.commons.dao.GenericDao;
import ru.runa.wfe.user.User;

/**
 * DAO level interface for managing bots.
 * 
 * @author Konstantinov Aleksey 25.02.2012
 * @since 2.0
 */
@Component
public class BotDao extends GenericDao<Bot> {

    public BotDao() {
        super(Bot.class);
    }

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
        val b = QBot.bot;
        return queryFactory.selectFrom(b).where(b.botStation.eq(botStation).and(b.username.eq(username))).fetchFirst();
    }

    /**
     * Load {@linkplain Bot} from database by userName (botstation is not
     * specified so there can be more that 1 bot).
     * 
     * @return loaded {@linkplain Bot} or <code>null</code> if no bot found
     */
    public Bot get(String username) {
        val b = QBot.bot;
        return queryFactory.selectFrom(b).where(b.username.eq(username)).fetchFirst();
    }

    public boolean isBot(User u) {
        val b = QBot.bot;
        // TODO Should be select(Expressions.constant(1)), but: https://github.com/querydsl/querydsl/issues/455
        //      May be this is fixed in Hibernate 5? If yes, search for all ".fetchFirst() != null" and replace.
        return queryFactory.select(b.id).from(b).where(b.username.eq(u.getName())).fetchFirst() != null;
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
    public List<Bot> getAll(Long botStationId) {
        val b = QBot.bot;
        return queryFactory.selectFrom(b).where(b.botStation.id.eq(botStationId)).fetch();
    }
}
