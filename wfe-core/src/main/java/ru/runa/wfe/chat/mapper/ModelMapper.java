package ru.runa.wfe.chat.mapper;

import java.util.Collection;

/**
 * @author Sergey Inyakin
 */
public interface ModelMapper<E, D> {
    E toEntity(D dto);
    D toDto(E entity);
    Collection<E> toEntities(Collection<D> dtos);
    Collection<D> toDtos(Collection<E> entities);
}
