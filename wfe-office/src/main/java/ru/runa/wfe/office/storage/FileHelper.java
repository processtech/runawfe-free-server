package ru.runa.wfe.office.storage;

import java.io.File;

public class FileHelper {

    public static void checkPath(String path) {
        File f = new File(path);
        if (f.exists() && f.isDirectory()) {
            return;
        }
        f.mkdirs();
    }
}
