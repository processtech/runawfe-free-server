package ru.runa.wfe.execution.process.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.FrozenTokenDao;
import ru.runa.wfe.execution.dto.WfFrozenToken;
import ru.runa.wfe.lang.NodeType;

@Component
public class FrozenProcessBySignalTimeExceededSeeker implements FrozenProcessSeeker {

    private final String nameId = FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getName();
    private final String nameLabel = FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED.getNameLabel();

    @Autowired
    private FrozenTokenDao frozenTokenDao;

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
        List<WfFrozenToken> frozenTokens = new ArrayList<>();
        List<CurrentToken> receiveMessageTokens;

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1 * timeThreshold);

        int pageNumber = 0;

        do {
            receiveMessageTokens = frozenTokenDao.findByExecutionStatusIsActiveAndNodeTypesInAndNodeEnterDateLessThanAndFilter(
                    Arrays.asList(NodeType.RECEIVE_MESSAGE),
                    calendar.getTime(), pageNumber, FrozenTokenDao.PAGE_SIZE, filters);
            frozenTokens.addAll(receiveMessageTokens.stream()
                .map(new Function<Token, WfFrozenToken>() {
                    @Override
                    public WfFrozenToken apply(Token token) {
                        return new WfFrozenToken(token, getNameLabel());
                    }
                    }).collect(Collectors.toList()));
            pageNumber++;
        } while (receiveMessageTokens.size() == FrozenTokenDao.PAGE_SIZE);
    return frozenTokens;
    }
}