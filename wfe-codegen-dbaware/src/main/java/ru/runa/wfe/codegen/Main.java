package ru.runa.wfe.codegen;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Analyzes PostgreSQL database structure and generates wfe-core/src/main/java/ru/runa/wfe/commons/dbpatch/DbPatch0.java.
 *
 * @author Dmitry Grigoriev aka dimgel; based on my private Scala project "lib.ql" source code.
 */
public class Main {

    public static int main(String args[]) throws Exception {
        System.out.println("Code generator started.");

        if (args.length != 1 || !Pattern.matches("^jdbc:postgresql://.*$", args[0])) {
            throw new Exception("Usage: build-n-run.sh jdbc:postgresql://localhost:5432/wfe?user=yyy&password=zzz");
        }
        String jdbcUrl = args[0];

        File patch0File = new File("../wfe-core/src/main/java/ru/runa/wfe/commons/dbpatch/DbPatch0.java");
        if (!patch0File.isFile()) {
            throw new Exception("File \"" + patch0File + "\" does not exist");
        }

        DbStructureAnalyzer.Structure st = DbStructureAnalyzer.analyze(jdbcUrl);

        DbPatch0Generator.generate(st, patch0File);

        System.out.println("Done.");
        return 0;
    }
}
