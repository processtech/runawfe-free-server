package ru.runa.wfe.service.delegate;

import java.util.List;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.service.BotService;
import ru.runa.wfe.user.User;

public class BotServiceDelegate extends EJB3Delegate implements BotService {

    public BotServiceDelegate() {
        super(BotService.class);
    }

    private BotService getBotService() {
        return (BotService) getService();
    }

    @Override
    public BotStation createBotStation(User user, BotStation bs) {
        try {
            return getBotService().createBotStation(user, bs);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public BotStation getBotStation(Long id) {
        try {
            return getBotService().getBotStation(id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public BotStation getBotStationByName(String name) {
        try {
            return getBotService().getBotStationByName(name);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Bot getBot(User user, Long id) {
        try {
            return getBotService().getBot(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<BotStation> getBotStations() {
        try {
            return getBotService().getBotStations();
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeBotStation(User user, Long id) {
        try {
            getBotService().removeBotStation(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeBot(User user, Long id) {
        try {
            getBotService().removeBot(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void updateBotStation(User user, BotStation bs) {
        try {
            getBotService().updateBotStation(user, bs);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<Bot> getBots(User user, Long botStationId) {
        try {
            return getBotService().getBots(user, botStationId);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public Bot createBot(User user, Bot bot) {
        try {
            return getBotService().createBot(user, bot);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void updateBot(User user, Bot bot) {
        try {
            getBotService().updateBot(user, bot);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public List<BotTask> getBotTasks(User user, Long id) {
        try {
            return getBotService().getBotTasks(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public BotTask createBotTask(User user, BotTask task) {
        try {
            return getBotService().createBotTask(user, task);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void updateBotTask(User user, BotTask task) {
        try {
            getBotService().updateBotTask(user, task);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void removeBotTask(User user, Long id) {
        try {
            getBotService().removeBotTask(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public BotTask getBotTask(User user, Long id) {
        try {
            return getBotService().getBotTask(user, id);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] exportBot(User user, Bot bot) {
        try {
            return getBotService().exportBot(user, bot);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] exportBotStation(User user, BotStation station) {
        try {
            return getBotService().exportBotStation(user, station);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public byte[] exportBotTask(User user, Bot bot, String botTaskName) {
        try {
            return getBotService().exportBotTask(user, bot, botTaskName);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void importBot(User user, BotStation station, byte[] archive, boolean replace) {
        try {
            getBotService().importBot(user, station, archive, replace);
        } catch (Exception e) {
            throw handleException(e);
        }
    }

    @Override
    public void importBotStation(User user, byte[] archive, boolean replace) {
        try {
            getBotService().importBotStation(user, archive, replace);
        } catch (Exception e) {
            throw handleException(e);
        }
    }
}
