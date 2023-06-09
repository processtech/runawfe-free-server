package ru.runa.wfe.rest.impl;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;

import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotTask;
import ru.runa.wfe.bot.logic.BotLogic;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.rest.auth.AuthUser;
import ru.runa.wfe.rest.converter.WfeBotMapper;
import ru.runa.wfe.rest.converter.WfeBotStationMapper;
import ru.runa.wfe.rest.converter.WfeBotTaskMapper;
import ru.runa.wfe.rest.dto.WfeBot;
import ru.runa.wfe.rest.dto.WfeBotStation;
import ru.runa.wfe.rest.dto.WfeBotTask;
import ru.runa.wfe.script.AdminScriptOperationErrorHandler;
import ru.runa.wfe.script.botstation.WfeScriptForBotStations;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.user.User;

@RestController
@RequestMapping("/bot/")
@Transactional
public class BotController {

    @Autowired
    private BotLogic botLogic;

    @PutMapping("station")
    public WfeBotStation createBotStation(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBotStation botStation) {
        WfeBotStationMapper mapper = Mappers.getMapper(WfeBotStationMapper.class);
        return mapper.map(botLogic.createBotStation(authUser.getUser(), mapper.map(botStation)));
    }

    @GetMapping("station/{id}")
    public WfeBotStation getBotStation(@PathVariable Long id) {
        return Mappers.getMapper(WfeBotStationMapper.class).map(botLogic.getBotStationNotNull(id));
    }

    @GetMapping("station")
    public WfeBotStation getBotStationByName(@RequestParam String name) {
        return Mappers.getMapper(WfeBotStationMapper.class).map(botLogic.getBotStation(name));
    }

    @GetMapping("station/list")
    public List<WfeBotStation> getBotStations() {
        return Mappers.getMapper(WfeBotStationMapper.class).map(botLogic.getBotStations());
    }

    @DeleteMapping("station/{id}")
    public void removeBotStation(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        botLogic.removeBot(authUser.getUser(), id);
    }

    @PatchMapping("station")
    public void updateBotStation(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBotStation botStation) {
        botLogic.updateBotStation(authUser.getUser(), Mappers.getMapper(WfeBotStationMapper.class).map(botStation));
    }

    @GetMapping("{id}")
    public WfeBot getBot(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeBotMapper.class).map(botLogic.getBotNotNull(authUser.getUser(), id));
    }

    @DeleteMapping("{id}")
    public void removeBot(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        botLogic.removeBot(authUser.getUser(), id);
    }

    @GetMapping("list")
    public List<WfeBot> getBots(@AuthenticationPrincipal AuthUser authUser, @RequestParam Optional<Long> botStationId) {
        if(botStationId.isPresent()) {
            return Mappers.getMapper(WfeBotMapper.class).map(botLogic.getBots(authUser.getUser(), botStationId.get()));
        } else {
            List<BotStation> stations = botLogic.getBotStations();
            List<Bot> bots = Lists.newArrayList();
            for(BotStation station : stations) {
                bots.addAll(botLogic.getBots(authUser.getUser(), station.getId()));
            }
            return Mappers.getMapper(WfeBotMapper.class).map(bots);
        }
    }

    @PutMapping()
    public WfeBot createBot(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBot bot) {
        WfeBotMapper mapper = Mappers.getMapper(WfeBotMapper.class);
        return mapper.map(botLogic.createBot(authUser.getUser(), mapper.map(bot)));
    }

    @PatchMapping()
    public WfeBot updateBot(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBot bot, @RequestParam boolean incrementBotStationVersion) {
        WfeBotMapper mapper = Mappers.getMapper(WfeBotMapper.class);
        return mapper.map(botLogic.updateBot(authUser.getUser(), mapper.map(bot), incrementBotStationVersion));
    }

    @GetMapping("{id}/tasks")
    public List<WfeBotTask> getBotTasks(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeBotTaskMapper.class).map(botLogic.getBotTasks(authUser.getUser(), id));
    }

    @PutMapping("task")
    public WfeBotTask createBotTask(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBotTask botTask) {
        WfeBotTaskMapper mapper = Mappers.getMapper(WfeBotTaskMapper.class);
        return mapper.map(botLogic.createBotTask(authUser.getUser(), mapper.map(botTask)));
    }

    @PatchMapping("task")
    public void updateBotTask(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBotTask botTask) {
        botLogic.updateBotTask(authUser.getUser(), Mappers.getMapper(WfeBotTaskMapper.class).map(botTask));
    }

    @DeleteMapping("task/{id}")
    public void removeBotTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        botLogic.removeBotTask(authUser.getUser(), id);
    }

    @GetMapping("task/{id}")
    public WfeBotTask getBotTask(@AuthenticationPrincipal AuthUser authUser, @PathVariable Long id) {
        return Mappers.getMapper(WfeBotTaskMapper.class).map(botLogic.getBotTaskNotNull(authUser.getUser(), id));
    }

    @PostMapping("export")
    public byte[] exportBot(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBot bot) {
        List<BotTask> tasks = botLogic.getBotTasks(authUser.getUser(), bot.getId());
        return exportBotWithTasks(Mappers.getMapper(WfeBotMapper.class).map(bot), tasks);
    }

    @PostMapping("station/export")
    public byte[] exportBotStation(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBotStation botStation) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(baos);
            zipStream.putNextEntry(new ZipEntry("botstation"));
            zipStream.write(botStation.getName().getBytes(Charsets.UTF_8));
            zipStream.write('\n');
            for (WfeBot bot : getBots(authUser, Optional.of(botStation.getId()))) {
                zipStream.putNextEntry(new ZipEntry(bot.getUsername() + ".bot"));
                byte[] botArchive = exportBot(authUser, bot);
                zipStream.write(botArchive);
            }
            zipStream.close();
            baos.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @PutMapping("import")
    public void importBot(@AuthenticationPrincipal AuthUser authUser, @RequestBody byte[] archive,
            @RequestParam String botStationName, @RequestParam boolean replace) {
        try {
            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(archive));
            Map<String, byte[]> files = new HashMap<>();
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                byte[] bytes = ByteStreams.toByteArray(zin);
                files.put(entry.getName(), bytes);
            }
            byte[] scriptXml = files.remove("script.xml");
            Preconditions.checkNotNull(scriptXml, "Incorrect bot archive: no script.xml inside");
            BotStation botStation = botLogic.getBotStation(botStationName);
            WfeScriptForBotStations wfeScriptForBotStations = new WfeScriptForBotStations(botStation, replace);
            ApplicationContextFactory.autowireBean(wfeScriptForBotStations);
            ScriptExecutionContext context = ScriptExecutionContext.create(authUser.getUser(), files, null);
            wfeScriptForBotStations.runScript(scriptXml, context, new AdminScriptOperationErrorHandler() {
                @Override
                public void handle(Throwable th) {
                    Throwables.propagate(th);
                }
            });
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @PutMapping("station/import")
    public void importBotStation(@AuthenticationPrincipal AuthUser authUser, @RequestBody byte[] archive, @RequestParam boolean replace) {
        try {
            ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(archive));
            ZipEntry entry;
            WfeBotStation station = null;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals("botstation")) {
                    BufferedReader r = new BufferedReader(new InputStreamReader(zin, Charsets.UTF_8));
                    String name = r.readLine();
                    String addr = r.readLine();
                    station = getBotStationByName(name);
                    if (station == null) {
                        station = new WfeBotStation();
                        station.setName(name);
                        station.setVersion(0L);
                        station.setCreateDate(new Date());
                        station = createBotStation(authUser, station);
                    }
                    continue;
                }
                if (station == null) {
                    throw new IOException("Incorrect bot archive");
                }
                importBot(authUser, ByteStreams.toByteArray(zin), station.getName(), replace);
            }
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @PostMapping("task/export")
    public byte[] exportBotTask(@AuthenticationPrincipal AuthUser authUser, @RequestBody WfeBot bot, @RequestParam String botTaskName) {
        List<BotTask> tasks = Lists.newArrayList();
        tasks.add(botLogic.getBotTaskNotNull(authUser.getUser(), bot.getId(), botTaskName));
        return exportBotWithTasks(Mappers.getMapper(WfeBotMapper.class).map(bot), tasks);
    }
    
    private byte[] exportBotWithTasks(Bot bot, List<BotTask> tasks) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipStream = new ZipOutputStream(baos);
            zipStream.putNextEntry(new ZipEntry("script.xml"));
            byte[] script = WfeScriptForBotStations.createScriptForBotLoading(bot, tasks);
            zipStream.write(script);
            for (BotTask task : tasks) {
                byte[] conf = task.getConfiguration();
                if (conf != null && conf.length != 0) {
                    zipStream.putNextEntry(new ZipEntry(task.getName() + ".conf"));
                    zipStream.write(conf);
                }

                byte[] embeddedFile = task.getEmbeddedFile();
                if (embeddedFile != null && !Strings.isNullOrEmpty(task.getEmbeddedFileName())) {
                    zipStream.putNextEntry(new ZipEntry(task.getEmbeddedFileName()));
                    zipStream.write(embeddedFile);
                }
            }
            zipStream.close();
            baos.flush();
            return baos.toByteArray();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
