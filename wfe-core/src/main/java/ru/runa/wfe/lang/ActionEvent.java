/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.lang;

import java.io.Serializable;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class ActionEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String TRANSITION = "transition";
        
    public static final String NODE_ENTER = "node-enter";
    public static final String NODE_LEAVE = "node-leave";
            
    public static final String TASK_CREATE = "task-create";
    public static final String TASK_ASSIGN = "task-assign";
    public static final String TASK_END = "task-end";
    
    public static final String TIMER = "timer";

    private final String eventType;
    private final List<Action> actions = Lists.newArrayList();

    public ActionEvent(String eventType) {
        this.eventType = eventType;
    }

    public List<Action> getActions() {
        return actions;
    }

    public Action addAction(Action action) {
        Preconditions.checkNotNull(action, "can't add a null action to an event");
        actions.add(action);
        action.setEvent(this);
        return action;
    }

    public String getEventType() {
        return eventType;
    }

    @Override
    public String toString() {
        return eventType;
    }
}
