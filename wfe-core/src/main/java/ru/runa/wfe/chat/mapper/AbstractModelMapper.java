package ru.runa.wfe.chat.mapper;

import java.util.ArrayList;
import java.util.Collection;

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
    public Collection<E> toEntities(Collection<D> dtos) {
        Collection<E> result = new ArrayList<>(dtos.size());
        for (D dto : dtos) {
            result.add(toEntity(dto));
        }
        return result;
    }

    @Override
    public Collection<D> toDtos(Collection<E> entities) {
        Collection<D> result = new ArrayList<>(entities.size());
        for (E entity : entities) {
            result.add(toDto(entity));
        }
        return result;
    }
}
