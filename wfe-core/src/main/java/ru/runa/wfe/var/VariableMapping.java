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
package ru.runa.wfe.var;

import com.google.common.base.MoreObjects;
import java.io.Serializable;

public class VariableMapping implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final String USAGE_READ = "read";
    public static final String USAGE_WRITE = "write";
    public static final String USAGE_SYNC = "sync";
    public static final String USAGE_MULTIINSTANCE_LINK = "multiinstancelink";
    public static final String USAGE_SELECTOR = "selector";
    public static final String USAGE_TEXT = "text";
    public static final String USAGE_DISCRIMINATOR_VARIABLE = "variable";
    public static final String USAGE_DISCRIMINATOR_GROUP = "group";
    public static final String USAGE_DISCRIMINATOR_RELATION = "relation";

    private String name;
    private String mappedName;
    private String usage;

    public VariableMapping(String name, String mappedName, String usage) {
        this.name = name;
        this.mappedName = mappedName;
        this.usage = usage;
    }

    public boolean isReadable() {
        return hasUsage(USAGE_READ);
    }

    public boolean isWritable() {
        return hasUsage(USAGE_WRITE);
    }

    public boolean isSyncable() {
        return hasUsage(USAGE_SYNC);
    }

    public boolean isMultiinstanceLink() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK);
    }

    public boolean isPropertySelector() {
        return hasUsage(USAGE_SELECTOR);
    }

    public boolean isMultiinstanceLinkByVariable() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_VARIABLE);
    }

    public boolean isMultiinstanceLinkByGroup() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_GROUP);
    }

    public boolean isMultiinstanceLinkByRelation() {
        return hasUsage(USAGE_MULTIINSTANCE_LINK) && hasUsage(USAGE_DISCRIMINATOR_RELATION);
    }

    public boolean isText() {
        return hasUsage(USAGE_TEXT);
    }

    public boolean hasUsage(String accessLiteral) {
        if (usage == null) {
            return false;
        }
        return usage.indexOf(accessLiteral) != -1;
    }

    public String getUsage() {
        return usage;
    }

    public String getMappedName() {
        return mappedName;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("usage", usage).add("name", getName()).add("mappedName", mappedName).toString();
    }
}
