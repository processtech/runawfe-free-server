package ru.runa.wfe.rest.converter;

import java.util.List;
import org.mapstruct.Mapper;
import ru.runa.wfe.job.dto.WfJob;
import ru.runa.wfe.rest.dto.WfeJob;

@Mapper
public interface WfeJobMapper {
    WfeJob map(WfJob job);

    List<WfeJob> map(List<WfJob> jobs);
}
