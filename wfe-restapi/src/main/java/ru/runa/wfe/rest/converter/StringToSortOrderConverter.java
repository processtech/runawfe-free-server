package ru.runa.wfe.rest.converter;

import org.springframework.core.convert.converter.Converter;
import ru.runa.wfe.rest.dto.BatchPresentationRequest.Sorting.Order;;

public class StringToSortOrderConverter implements Converter<String, Order> {

    @Override
    public Order convert(String source) {
        return Order.valueOf(source);
    }

}
