package ru.runa.wfe.commons;

import com.google.common.base.Preconditions;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import javax.annotation.PostConstruct;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * For low-level JDBC logic.
 */
@Component
public class ManualTransactionManager {

    public static abstract class TxBase {

        /**
         * Helper for subclasses.
         */
        protected final int executeUpdates(Connection conn, String... sqls) throws SQLException {
            try (Statement stmt = conn.createStatement()) {
                int result = 0;
                for (String sql : sqls) {
                    result = stmt.executeUpdate(sql);
                }
                return result;
            }
        }
    }

    public static abstract class TxCallable<R> extends TxBase {
        public abstract R call(Connection conn) throws Exception;
    }

    public static abstract class TxRunnable extends TxBase {
        public abstract void run(Connection conn) throws Exception;
    }


    @Autowired
    private PlatformTransactionManager txManager;
    @Autowired
    private SessionFactory sessionFactory;

    private DefaultTransactionDefinition txDefinition;


    @PostConstruct
    public void init() {
        Preconditions.checkNotNull(txManager);
        txDefinition = new DefaultTransactionDefinition();
        txDefinition.setName("WfeManual");
    }


    /**
     * @param noCommit Against Oracle error "ORA-02089: COMMIT is not allowed in a subordinate session", see #706.
     */
    private <R> R impl(TxCallable<R> callable, boolean noCommit) throws Exception {
        R result;
        TransactionStatus txStatus = txManager.getTransaction(txDefinition);
        try {
            result = callable.call(sessionFactory.getCurrentSession().connection());
        } catch (Throwable e) {
            txManager.rollback(txStatus);
            throw e;
        }
        if (!noCommit) {
            txManager.commit(txStatus);
        }
        return result;
    }

    public <R> R callInTransaction(TxCallable<R> callable) throws Exception {
        return impl(callable, false);
    }

    public void runInTransaction(TxRunnable runnable) throws Exception {
        impl(new TxCallable<Object>() {
            @Override
            public Object call(Connection conn) throws Exception {
                runnable.run(conn);
                return null;
            }
        }, false);
    }

    /**
     * Against Oracle error "ORA-02089: COMMIT is not allowed in a subordinate session", see #706.
     */
    public void runOneDDLInTransaction(String sql) throws Exception {
        boolean noCommit = ApplicationContextFactory.getDbType() == DbType.ORACLE;
        impl(new TxCallable<Object>() {
            @Override
            public Object call(Connection conn) throws Exception {
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate(sql);
                }
                return null;
            }
        }, noCommit);
    }
}
