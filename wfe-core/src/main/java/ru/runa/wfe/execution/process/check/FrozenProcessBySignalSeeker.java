package ru.runa.wfe.execution.process.check;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.FrozenTokenDao;
import ru.runa.wfe.execution.dto.WfFrozenToken;

@Component
public class FrozenProcessBySignalSeeker implements FrozenProcessSeeker {

    private final String nameId = FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.getName();
    private final String nameLabel = FrozenProcessesSearchParameter.SEARCH_FROZEN_BY_SIGNAL.getNameLabel();

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
        return frozenTokenDao.findByExecutionStatusIsActiveAndNodeTypeIsReciveMessageAndHasSignalAndFilter(filters).stream()
                .map(new Function<Token, WfFrozenToken>() {
                    @Override
                    public WfFrozenToken apply(Token token) {
                        return new WfFrozenToken(token, getNameLabel());
                    }
                }).collect(Collectors.toList());
    }
}