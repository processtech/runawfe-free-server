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
package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import ru.runa.wfe.lang.Node;

/**
 * Logging message nodes execution.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "8")
public class CurrentReceiveMessageLog extends CurrentNodeEnterLog implements ReceiveMessageLog {
    private static final long serialVersionUID = 1L;

    public CurrentReceiveMessageLog() {
    }

    public CurrentReceiveMessageLog(Node node, String message) {
        super(node);
        if (message.length() < 1000) {
            addAttribute(ATTR_MESSAGE, message);
        } else {
            setBytes(message.getBytes(Charsets.UTF_8));
        }
    }
}
