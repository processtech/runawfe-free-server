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
package ru.runa.wfe.bot.logic;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.bot.dao.BotStationDao;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.logic.CommonLogic;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;

public class BotLogic extends CommonLogic {
    @Autowired
    private BotStationDao botStationDao;

    public List<BotStation> getBotStations() {
        return botStationDao.getAll();
    }

    public BotStation createBotStation(User user, BotStation botStation) throws BotStationAlreadyExistsException {
        checkPermission(user, Permission.UPDATE);
        if (botStationDao.get(botStation.getName()) != null) {
            throw new BotStationAlreadyExistsException(botStation.getName());
        }
        return botStationDao.create(botStation);
    }

    public void updateBotStation(User user, BotStation botStation) throws BotStationAlreadyExistsException {
        checkPermission(user, Permission.UPDATE);
        BotStation botStationToCheck = getBotStation(botStation.getName());
        if (botStationToCheck != null && !Objects.equal(botStationToCheck.getId(), botStation.getId())) {
            throw new BotStationAlreadyExistsException(botStation.getName());
        }
        botStationDao.update(botStation);
    }

    public BotStation getBotStationNotNull(Long id) throws BotStationDoesNotExistException {
        return botStationDao.getNotNull(id);
    }

    public BotStation getBotStation(String name) {
        return botStationDao.get(name);
    }

    public BotStation getBotStationNotNull(String name) throws BotStationDoesNotExistException {
        return botStationDao.getNotNull(name);
    }

    public void removeBotStation(User user, Long id) throws BotStationDoesNotExistException {
        checkPermission(user, Permission.UPDATE);
        List<Bot> bots = getBots(user, id);
        for (Bot bot : bots) {
            removeBot(user, bot.getId());
        }
        botStationDao.delete(id);
    }

    public Bot createBot(User user, Bot bot) throws BotAlreadyExistsException {
        checkPermission(user, Permission.UPDATE);
        Preconditions.checkNotNull(bot.getBotStation());
        if (getBot(user, bot.getBotStation().getId(), bot.getUsername()) != null) {
            throw new BotAlreadyExistsException(bot.getUsername());
        }
        if (executorDao.isExecutorExist(bot.getUsername()) && executorDao.isExecutorExist(SystemProperties.getBotsGroupName())) {
            Actor botActor = executorDao.getActor(bot.getUsername());
            Group botsGroup = executorDao.getGroup(SystemProperties.getBotsGroupName());
            executorDao.addExecutorToGroup(botActor, botsGroup);
        }
        bot = botDao.create(bot);
        sessionFactory.getCurrentSession().flush();
        incrementBotStationVersion(bot);
        return bot;
    }

    public List<Bot> getBots(User user, Long botStationId) {
        checkPermission(user, Permission.READ);
        return botDao.getAll(botStationId);
    }

    public Bot getBotNotNull(User user, Long id) {
        checkPermission(user, Permission.READ);
        return botDao.getNotNull(id);
    }

    public Bot getBot(User user, Long botStationId, String name) {
        checkPermission(user, Permission.READ);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDao.get(botStation, name);
    }

    public Bot getBotNotNull(User user, Long botStationId, String name) {
        checkPermission(user, Permission.READ);
        BotStation botStation = getBotStationNotNull(botStationId);
        return botDao.getNotNull(botStation, name);
    }

    public Bot updateBot(User user, Bot bot, boolean incrementBotStationVersion) throws BotAlreadyExistsException {
        checkPermission(user, Permission.UPDATE);
        Preconditions.checkNotNull(bot.getBotStation());
        Bot botToCheck = getBot(user, bot.getBotStation().getId(), bot.getUsername());
        if (botToCheck != null && !Objects.equal(botToCheck.getId(), bot.getId())) {
            throw new BotAlreadyExistsException(bot.getUsername());
        }
        bot = botDao.update(bot);
        sessionFactory.getCurrentSession().flush();   // see #1303-6
        if (incrementBotStationVersion) {
            incrementBotStationVersion(bot);
        }
        return bot;
    }

    public void removeBot(User user, Long id) throws BotDoesNotExistException {
        checkPermission(user, Permission.UPDATE);
        List<BotTask> tasks = getBotTasks(user, id);
        for (BotTask botTask : tasks) {
            removeBotTask(user, botTask.getId());
        }
        Bot bot = getBotNotNull(user, id);
        if (executorDao.isExecutorExist(bot.getUsername()) && executorDao.isExecutorExist(SystemProperties.getBotsGroupName())) {
            Actor botActor = executorDao.getActor(bot.getUsername());
            Group botsGroup = executorDao.getGroup(SystemProperties.getBotsGroupName());
            executorDao.removeExecutorFromGroup(botActor, botsGroup);
        }
        botDao.delete(id);
    }

    public BotTask createBotTask(User user, BotTask botTask) throws BotTaskAlreadyExistsException {
        checkPermission(user, Permission.UPDATE);
        Preconditions.checkNotNull(botTask.getBot());
        if (getBotTask(user, botTask.getBot().getId(), botTask.getName()) != null) {
            throw new BotTaskAlreadyExistsException(botTask.getName());
        }
        botTask = botTaskDao.create(botTask);
        incrementBotStationVersion(botTask);
        return botTask;
    }

    public List<BotTask> getBotTasks(User user, Long id) {
        checkPermission(user, Permission.READ);
        Bot bot = getBotNotNull(user, id);
        return botTaskDao.getAll(bot);
    }

    public BotTask getBotTaskNotNull(User user, Long id) {
        checkPermission(user, Permission.READ);
        return botTaskDao.getNotNull(id);
    }

    public BotTask getBotTask(User user, Long botId, String name) {
        checkPermission(user, Permission.READ);
        Bot bot = getBotNotNull(user, botId);
        return botTaskDao.get(bot, name);
    }

    public BotTask getBotTaskNotNull(User user, Long botId, String name) {
        checkPermission(user, Permission.READ);
        Bot bot = getBotNotNull(user, botId);
        return botTaskDao.getNotNull(bot, name);
    }

    public void updateBotTask(User user, BotTask botTask) throws BotTaskAlreadyExistsException {
        checkPermission(user, Permission.UPDATE);
        Preconditions.checkNotNull(botTask.getBot());
        BotTask botTaskToCheck = getBotTask(user, botTask.getBot().getId(), botTask.getName());
        if (botTaskToCheck != null && !Objects.equal(botTaskToCheck.getId(), botTask.getId())) {
            throw new BotTaskAlreadyExistsException(botTask.getName());
        }
        if (botTask.getConfiguration() != null && botTask.getConfiguration().length == 0) {
            BotTask botTaskFromDB = getBotTaskNotNull(user, botTask.getId());
            botTask.setConfiguration(botTaskFromDB.getConfiguration());
        }
        botTask = botTaskDao.update(botTask);
        incrementBotStationVersion(botTask);
    }

    public void removeBotTask(User user, Long id) throws BotTaskDoesNotExistException {
        checkPermission(user, Permission.UPDATE);
        BotTask botTask = getBotTaskNotNull(user, id);
        botTaskDao.delete(id);
        incrementBotStationVersion(botTask);
    }

    private void checkPermission(User user, Permission p) {
        // Bot can read botstation. UPD: Since rm660 there's only ALL permission on BOTSTATIONS.
        if (!botDao.isBot(user)) {
            permissionDao.checkAllowed(user, p, SecuredSingleton.BOTSTATIONS);
        }
    }

    private void incrementBotStationVersion(Object entity) {
        BotStation botStation;
        if (entity instanceof BotStation) {
            botStation = (BotStation) entity;
        } else if (entity instanceof Bot) {
            botStation = ((Bot) entity).getBotStation();
        } else if (entity instanceof BotTask) {
            botStation = ((BotTask) entity).getBot().getBotStation();
        } else {
            throw new InternalApplicationException("Unexpected entity class " + entity);
        }
        botStation.setVersion(botStation.getVersion() + 1);
        botStationDao.update(botStation);
    }

}
