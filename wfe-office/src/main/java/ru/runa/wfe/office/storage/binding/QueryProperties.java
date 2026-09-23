package ru.runa.wfe.office.storage.binding;

import java.util.Properties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.runa.wfe.var.UserType;

@Getter
@RequiredArgsConstructor
public class QueryProperties {
    private final Properties properties;
    private final UserType userType;
    private final String condition;
    private final QueryRole queryRole;
}
