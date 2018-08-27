package ru.runa.wfe.execution.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedToken;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.Token;

@Component
public class TokenDao extends GenericDao2<Token, CurrentToken, CurrentTokenDao, ArchivedToken, ArchivedTokenDao> {

    @Autowired
    TokenDao(CurrentTokenDao dao1, ArchivedTokenDao dao2) {
        super(dao1, dao2);
    }
}
