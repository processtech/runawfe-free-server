package ru.runa.wfe.chat.mapper;

/**
 * @author Sergey Inyakin
 */
public interface ModelMapper<E, D> {
    E toEntity(D dto);
    D toDto(E entity);
}
