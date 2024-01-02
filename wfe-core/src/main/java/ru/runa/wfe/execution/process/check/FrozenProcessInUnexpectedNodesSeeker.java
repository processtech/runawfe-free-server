package ru.runa.wfe.execution.process.check;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.FrozenTokenDao;
import ru.runa.wfe.execution.dto.WfFrozenToken;
import ru.runa.wfe.lang.NodeType;

@Component
public class FrozenProcessInUnexpectedNodesSeeker implements FrozenProcessSeeker {

    private final String nameId = FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.getName();
    private final String nameLabel = FrozenProcessesSearchParameter.SEARCH_FROZEN_IN_UNEXPECTED_NODES.getNameLabel();

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
        Collection<NodeType> allowToStopTypes = Arrays.asList(
                // @formatter:off
                NodeType.PARALLEL_GATEWAY,
                NodeType.TASK_STATE,
                NodeType.MULTI_TASK_STATE,
                NodeType.TIMER,
                NodeType.RECEIVE_MESSAGE,
                NodeType.MULTI_SUBPROCESS,
                NodeType.WAIT_STATE,
                NodeType.SUBPROCESS);
        // @formatter:on

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -1);

        return frozenTokenDao.findByExecutionStatusIsActiveAndNodeTypesNotInAndNodeEnterDateLessThanAndFilter(allowToStopTypes, calendar.getTime(),
                filters).stream()
                .map(new Function<Token, WfFrozenToken>() {
                    @Override
                    public WfFrozenToken apply(Token token) {
                        return new WfFrozenToken(token, getNameLabel());
                    }
                }).collect(Collectors.toList());
    }
}