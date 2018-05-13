package ru.runa.wf.web.datafile;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;
import org.dom4j.Document;
import ru.runa.wf.web.datafile.builder.BotDataFileBuilder;
import ru.runa.wf.web.datafile.builder.DataFileBuilder;
import ru.runa.wf.web.datafile.builder.DefinitionDataFileBuilder;
import ru.runa.wf.web.datafile.builder.ExecutorDataFileBuilder;
import ru.runa.wf.web.datafile.builder.PermissionsDataFileBuilder;
import ru.runa.wf.web.datafile.builder.RelationDataFileBuilder;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.user.User;

/**
 * Populate zip archive.
 * 
 * @author riven
 * 
 */
public class DataFileCreator {
    private final ZipOutputStream zos;
    private final Document script;
    private final List<DataFileBuilder> builders = new ArrayList<>();

    public DataFileCreator(ZipOutputStream zos, Document script, User user) {
        this.zos = zos;
        this.script = script;

        builders.add(new ExecutorDataFileBuilder(user));
        builders.add(new DefinitionDataFileBuilder(user));
        builders.add(new BotDataFileBuilder(user));
        builders.add(new RelationDataFileBuilder(user));
        builders.add(new PermissionsDataFileBuilder(user, Lists.newArrayList(SecuredSingleton.SYSTEM), "addPermissionsOnSystem", false));
        builders.add(new PermissionsDataFileBuilder(user, Lists.newArrayList(SecuredSingleton.RELATIONS), "addPermissionsOnRelationGroup", false));
        builders.add(new PermissionsDataFileBuilder(user, Lists.newArrayList(SecuredSingleton.BOTSTATIONS), "addPermissionsOnBotStations", false));
    }

    public void process() throws Exception {
        for (DataFileBuilder builder : builders) {
            builder.build(zos, script);
        }
    }
}
