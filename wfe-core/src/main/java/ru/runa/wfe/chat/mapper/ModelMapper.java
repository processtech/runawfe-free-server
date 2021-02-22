package ru.runa.wfe.chat.mapper;

import java.util.Collection;
import java.util.List;

/**
 * @author Sergey Inyakin
 */
public interface ModelMapper<E, D> {
    E toEntity(D dto);
    D toDto(E entity);
    List<E> toEntities(Collection<D> dtos);
    List<D> toDtos(Collection<E> entities);
}
