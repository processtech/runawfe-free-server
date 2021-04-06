package ru.runa.wfe.service;

import java.util.List;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotAlreadyExistsException;
import ru.runa.wfe.bot.BotDoesNotExistException;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationAlreadyExistsException;
import ru.runa.wfe.bot.BotStationDoesNotExistException;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.BotTaskAlreadyExistsException;
import ru.runa.wfe.bot.BotTaskDoesNotExistException;
import ru.runa.wfe.user.User;

/**
 * Service for operations with {@link BotStation}, {@link Bot}, {@link BotTask}.
 * 
 * @author dofs
 * @since 3.0
 */
public interface BotService {

    /**
     * Creates bot station.
     * 
     * @return created bot station
     * @throws BotStationAlreadyExistsException
     *             if name collision occurs
     */
    BotStation createBotStation(User user, BotStation botStation) throws BotStationAlreadyExistsException;

    /**
     * Gets all bot stations.
     * 
     * @return list, not <code>null</code>
     */
    List<BotStation> getBotStations();

    /**
     * Finds bot station by id.
     * 
     * @return bot station, not <code>null</code>
     */
    BotStation getBotStation(Long id) throws BotStationDoesNotExistException;;

    /**
     * Finds bot station by name.
     * 
     * @return bot station or <code>null</code>
     */
    BotStation getBotStationByName(String name);

    /**
     * Updates bot station data.
     * 
     * @throws BotStationAlreadyExistsException
     *             if name collision occurs
     */
    void updateBotStation(User user, BotStation botStation) throws BotStationAlreadyExistsException;

    /**
     * Removes bot station with all bots and bot tasks by id.
     */
    void removeBotStation(User user, Long id) throws BotStationDoesNotExistException;

    /**
     * Imports bot station with all bots and bot tasks from archive.
     * 
     * @param replace
     *            override existing entities
     */
    void importBotStation(User user, byte[] archive, boolean replace);

    /**
     * Exports bot station to archive
     * 
     * @return archive
     */
    byte[] exportBotStation(User user, BotStation station) throws BotStationDoesNotExistException;

    /**
     * Creates new bot.
     * 
     * @return created bot
     * @throws BotAlreadyExistsException
     *             if name collision occurs
     */
    Bot createBot(User user, Bot bot) throws BotAlreadyExistsException;

    /**
     * Loads all bots for bot station
     * 
     * @return list, not <code>null</code>
     */
    List<Bot> getBots(User user, Long botStationId);

    /**
     * Gets bot by id.
     * 
     * @return bot, not <code>null</code>
     */
    Bot getBot(User user, Long id) throws BotDoesNotExistException;

    /**
     * Updates bot data.
     * 
     * @throws BotAlreadyExistsException
     *             if name collision occurs
     */
    Bot updateBot(User user, Bot bot, boolean incrementBotStationVersion) throws BotAlreadyExistsException;

    /**
     * Removes bot with all bot tasks by id.
     */
    void removeBot(User user, Long id) throws BotDoesNotExistException;

    /**
     * Exports bot to archive.
     * 
     * @return archive
     */
    byte[] exportBot(User user, Bot bot) throws BotDoesNotExistException;

    /**
     * Imports bot from archive.
     * 
     * @param replace
     *            override existing entities
     */
    void importBot(User user, BotStation station, byte[] archive, boolean replace) throws BotStationDoesNotExistException;

    /**
     * Creates bot task.
     * 
     * @return created task
     * @throws BotTaskAlreadyExistsException
     *             if name collision occurs
     */
    BotTask createBotTask(User user, BotTask task) throws BotTaskAlreadyExistsException;

    /**
     * Loads all bot tasks by bot id.
     * 
     * @return list, not <code>null</code>
     */
    List<BotTask> getBotTasks(User user, Long id);

    /**
     * Loads bot task by id.
     * 
     * @return task, not <code>null</code>
     */
    BotTask getBotTask(User user, Long id) throws BotTaskDoesNotExistException;

    /**
     * Updates bot task.
     * 
     * @throws BotTaskAlreadyExistsException
     *             if name collision occurs
     */
    void updateBotTask(User user, BotTask task) throws BotTaskAlreadyExistsException;

    /**
     * Removes bot task by id.
     */
    void removeBotTask(User user, Long id) throws BotTaskDoesNotExistException;

    /**
     * Exports bot task to archive.
     * 
     * @return archive
     */
    byte[] exportBotTask(User user, Bot bot, String botTaskName) throws BotDoesNotExistException;
}
