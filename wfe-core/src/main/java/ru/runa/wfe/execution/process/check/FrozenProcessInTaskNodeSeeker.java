package ru.runa.wfe.execution.process.check;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.bot.dao.BotDao;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.FrozenTokenDao;
import ru.runa.wfe.execution.dto.WfFrozenToken;

@Component
public class FrozenProcessInTaskNodeSeeker implements FrozenProcessSeeker {

    private final String nameId = FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getName();
    private final String nameLabel = FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_TASK_NODES.getNameLabel();

    @Autowired
    private FrozenTokenDao frozenTokenDao;
    @Autowired
    private BotDao botDao;

    @Override
    public String getNameId() {
        return nameId;
    }

    @Override
    public String getNameLabel() {
        return nameLabel;
    }

    @Override
    public List<WfFrozenToken> seek(Integer timeThreshold, Map<FrozenProcessFilter, String> filters) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1 * timeThreshold);

        List<CurrentToken> frozenTokens = frozenTokenDao.findByUnassignedTasksInActiveProcessesAndFilter(filters);
        List<String> allBotNames = botDao.getAllNames();
        frozenTokens
                .addAll(frozenTokenDao.findByTaskExecutorNamesAreInAndAssignDateLessThanAndFilter(allBotNames, calendar.getTime(),
                        filters));
        return frozenTokens.stream().map(new Function<Token, WfFrozenToken>() {
            @Override
            public WfFrozenToken apply(Token token) {
                return new WfFrozenToken(token, getNameLabel());
            }
        }).collect(Collectors.toList());
    }
}