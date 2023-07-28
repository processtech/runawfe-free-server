package ru.runa.wfe.rest.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
import com.google.common.base.Throwables;

import ru.runa.wfe.user.logic.ExecutorLogic;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.rest.auth.AuthUser;

@RestController
@RequestMapping("/datasource/")
@Transactional
public class DataSourceController {

    @Autowired
    private ExecutorLogic executorLogic;

    @GetMapping("names")
    public List<String> getNames(@AuthenticationPrincipal AuthUser authUser) {
        return DataSourceStorage.getNames();
    }

    @PutMapping()
    public void importDataSource(@AuthenticationPrincipal AuthUser authUser, @RequestBody byte[] archive) {
        Preconditions.checkArgument(executorLogic.isAdministrator(authUser.getUser().getActor()), "not administrator");
        Preconditions.checkArgument(archive != null, "archive");
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(archive), Charsets.UTF_8)) {
            zis.getNextEntry();
            byte[] buf = new byte[1024];
            int n;
            while ((n = zis.read(buf, 0, 1024)) > -1) {
                baos.write(buf, 0, n);
            }
            zis.closeEntry();
            DataSourceStorage.save(baos.toByteArray(), true, !SystemProperties.isDatasourcePasswordExportAllowed());
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    @PostMapping("export")
    public byte[] exportDataSource(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        Preconditions.checkArgument(executorLogic.isAdministrator(authUser.getUser().getActor()), "not administrator");
        Preconditions.checkArgument(name != null, "name");
        byte[] content = SystemProperties.isDatasourcePasswordExportAllowed() ? DataSourceStorage.restore(name)
                : DataSourceStorage.restoreWithoutPassword(name);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos, Charsets.UTF_8)) {
            ZipEntry entry = new ZipEntry(name + ".xml");
            entry.setSize(content.length);
            zos.putNextEntry(entry);
            zos.write(content);
            zos.closeEntry();
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
        return baos.toByteArray();
    }

    @DeleteMapping()
    public void removeDataSource(@AuthenticationPrincipal AuthUser authUser, @RequestParam String name) {
        Preconditions.checkArgument(executorLogic.isAdministrator(authUser.getUser().getActor()), "not administrator");
        Preconditions.checkArgument(name != null, "name");
        DataSourceStorage.moveToHistory(name);
    }

    @GetMapping("info")
    public String getDbServerInfo(@RequestParam String name) {
        JdbcDataSource dataSource = (JdbcDataSource) DataSourceStorage.getDataSource(name);
        return dataSource.serverVersion();
    }
}
