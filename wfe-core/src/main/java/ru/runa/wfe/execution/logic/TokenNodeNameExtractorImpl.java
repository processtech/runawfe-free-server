package ru.runa.wfe.execution.logic;

import ru.runa.wfe.lang.Node;

public class TokenNodeNameExtractorImpl implements TokenNodeNameExtractor {

    @Override
    public String extract(Node node) {
        return node.getName();
    }

}
