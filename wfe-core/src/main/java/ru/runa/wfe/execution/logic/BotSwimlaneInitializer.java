package ru.runa.wfe.execution.logic;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class BotSwimlaneInitializer extends SwimlaneInitializer {
    private static final String BEGIN = "#";
    private String botName;

    @Autowired
    private ExecutorDao executorDao;

    public static boolean isValid(String initializer) {
        return initializer != null && initializer.startsWith(BEGIN);
    }

    @Override
    public void parse(String initializer) {
        Preconditions.checkArgument(isValid(initializer), "Invalid configuration");
        botName = initializer.substring(BEGIN.length());
    }

    public String getBotName() {
        return botName;
    }

    @Override
    public List<? extends Executor> evaluate(IVariableProvider variableProvider) {
        return Lists.newArrayList(executorDao.getExecutor(botName));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("botName", botName).toString();
    }

}
