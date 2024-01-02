package ru.runa.wfe.execution.process.check;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.execution.dto.WfFrozenToken;

@CommonsLog
@Component
public class FrozenProcessSeekManager {
    
    @Autowired
    private List<FrozenProcessSeeker> seekers;

    public List<WfFrozenToken> seek(Map<String, FrozenProcessSearchData> searchData, Map<FrozenProcessFilter, String> filters) {

        List<WfFrozenToken> frozenTokens = new ArrayList<>();
        for (FrozenProcessSeeker seeker : seekers) {
            if (searchData.containsKey(seeker.getNameId())) {
                log.info(String.format("%s started seeking", seeker.getClass().getName()));
                FrozenProcessSearchData seekerSearchData = searchData.get(seeker.getNameId());
                frozenTokens.addAll(seeker.seek(seekerSearchData.getTimeValue(), filters));
                log.info(String.format("%s ended seeking", seeker.getClass().getName()));
            }
        }
        return frozenTokens.stream().distinct().sorted(Comparator.comparing(new Function<WfFrozenToken, Long>() {
            @Override
            public Long apply(WfFrozenToken t) {
                return t.getProcessId();
            }
        }).reversed()).collect(Collectors.toList());
    }
}