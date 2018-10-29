package ru.runa.common.web.html;

import java.util.List;

import org.apache.ecs.html.TR;

/**
 * Created on 10.11.2004
 * 
 */
public interface RowBuilder {

    public boolean hasNext();

    public TR buildNext();

    public List<TR> buildNextArray();
}
