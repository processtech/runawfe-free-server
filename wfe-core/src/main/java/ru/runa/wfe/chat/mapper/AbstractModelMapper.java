package ru.runa.wfe.chat.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AbstractModelMapper<E, D> implements ModelMapper<E, D>{
    @Override
    public E toEntity(D dto) {
        throw new UnsupportedOperationException("Mapping is not supported yet");
    }

    @Override
    public D toDto(E entity) {
        throw new UnsupportedOperationException("Mapping is not supported yet");
    }

    @Override
    public List<E> toEntities(Collection<D> dtos) {
        List<E> result = new ArrayList<>(dtos.size());
        for (D dto : dtos) {
            result.add(toEntity(dto));
        }
        return result;
    }

    @Override
    public List<D> toDtos(Collection<E> entities) {
        List<D> result = new ArrayList<>(entities.size());
        for (E entity : entities) {
            result.add(toDto(entity));
        }
        return result;
    }
}
