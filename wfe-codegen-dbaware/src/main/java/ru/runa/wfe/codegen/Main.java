package ru.runa.wfe.codegen;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Analyzes PostgreSQL database structure and generates wfe-core/src/main/java/ru/runa/wfe/commons/dbmigration/DbMigration0.java.
 *
 * @author Dmitry Grigoriev (dimgel.ru, dimgel.me). Based on my private project's source code.
 *         That means, I preserve the right to use same approach & code in any other projects, open or closed source, under any license.
 */
public class Main {

    public static int main(String args[]) throws Exception {
        System.out.println("Code generator started.");

        if (args.length != 1 || !Pattern.matches("^jdbc:postgresql://.*$", args[0])) {
            throw new Exception("Usage: build-n-run.sh jdbc:postgresql://localhost:5432/wfe?user=yyy&password=zzz");
        }
        String jdbcUrl = args[0];

        File patch0File = new File("../wfe-core/src/main/java/ru/runa/wfe/commons/dbmigration/DbMigration0.java");
        if (!patch0File.isFile()) {
            throw new Exception("File \"" + patch0File + "\" does not exist");
        }

        DbStructureAnalyzer.Structure st = DbStructureAnalyzer.analyze(jdbcUrl);
        if (st.tables.isEmpty()) {
            throw new Exception("Database is empty.");
        }

        DbMigration0Generator.generate(st, patch0File);

        System.out.println("Done.");
        return 0;
    }
}
