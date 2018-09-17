package ru.runa.wfe.codegen;

import java.io.File;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.val;

/**
 * Analyzes PostgreSQL database structure and generates wfe-core/src/main/java/ru/runa/wfe/commons/dbpatch/DbPatch0.java.
 *
 * @author Dmitry Grigoriev aka dimgel; based on my private Scala project "lib.ql" source code.
 */
public class Main {

    public static int main(String args[]) throws Exception {
        System.out.println("Code generator started.");

//        for (int i = 0;  i < args.length;  i++) {
//            System.out.println("Arg[" + i + "] = " + args[i]);
//        }

        int argi = 0;
        boolean verbose = false;
        if (args.length > argi && Objects.equals(args[argi], "--verbose")) {
            verbose = true;
            argi++;
        }

        if (args.length != (argi + 1) || !Pattern.matches("^jdbc:postgresql://.*$", args[argi])) {
            throw new Exception("Usage: build-n-run.sh [--verbose] jdbc:postgresql://localhost:5432/wfe?user=yyy&password=zzz");
        }
        String jdbcUrl = args[argi];

        File patch0File = new File("../wfe-core/src/main/java/ru/runa/wfe/commons/dbpatch/DbPatch0.java");
        if (!patch0File.isFile()) {
            throw new Exception("File \"" + patch0File + "\" does not exist");
        }

        DbStructureAnalyzer.Structure st = DbStructureAnalyzer.analyze(jdbcUrl);
        if (verbose) {
            System.out.println("Database structure is parsed:");
            System.out.println("    Tables:");
            for (val t : st.tables) {
                System.out.println("        " + t.name);
                for (val c : t.columns) {
                    System.out.println("            " + c.name + " " + c.type +
                            (c.typeLength != null ? "(" + c.typeLength + ")" : "") +
                            (c.isNotNull ? " not null" : ""));
                }
            }
        }

        DbPatch0Generator.generate(st, patch0File);

        System.out.println("Done.");
        return 0;
    }
}
