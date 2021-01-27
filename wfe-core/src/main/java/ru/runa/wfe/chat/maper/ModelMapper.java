package ru.runa.wfe.chat.maper;

/**
 * @author Sergey Inyakin
 */
public interface ModelMapper<E, D> {
    E toEntity(D dto);
    D toDto(E entity);
}
