package ru.runa.wfe.execution.process.check;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.execution.dto.WfFrozenToken;

public interface FrozenProcessSeeker {

    String getNameId();

    String getNameLabel();

    List<WfFrozenToken> seek(Integer timeThreshold, Map<FrozenProcessFilter, String> filters);

}
