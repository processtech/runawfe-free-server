This application analyzes PostgreSQL database structure and generates file wfe-core/src/main/java/ru/runa/wfe/commons/dbmigration/DbMigration0.java.
Re-run it when number of migrations since last run grows large and empty database creation grows slow:

1. Create EMPTY PostgreSQL database.
2. Start WFE on it. WFE will: (a) populate it using current DbMigration0; (b) apply migrations created since DbMigration0 was generated.
3. Stop WFE.
4. Run ./build-n-run.sh <JDBC-URL-of-this-database>. It will regenerate DbMigration0 with current database structure and applied migration list.

NOTE 1. Running on EMPTY database in critical, since it gives deterministic result. Particularly, I edited Hibernate-generated random FK names
        in initial version of DbMigration0, and regenerating from some other non-empty DB will loose those edits.

NOTE 2. PostgreSQL's JDBC URL may contain username and password: jdbc:postgresql://localhost:5432/wfe?user=yyy&password=zzz
