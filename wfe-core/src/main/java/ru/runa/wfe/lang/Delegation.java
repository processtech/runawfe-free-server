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

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.extension.Configurable;

@XmlAccessorType(XmlAccessType.FIELD)
public class Delegation implements Serializable {
    private static final long serialVersionUID = 1L;

    private String className;
    private String configuration;

    public Delegation() {
    }

    public Delegation(String className, String configuration) {
        this.className = null == className ? null : className.intern();
        this.configuration = null == configuration ? null : configuration.intern();
    }

    /**
     * Checks all prerequisites needed for execution.
     */
    public void validate() {
        Preconditions.checkNotNull(className, "className in " + this);
    }

    public <T extends Configurable> T getInstance() throws Exception {
        Configurable configurable = ApplicationContextFactory.createAutowiredBean(className);
        configurable.setConfiguration(configuration);
        return (T) configurable;
    }

    public String getConfiguration() {
        return configuration;
    }

    public String getClassName() {
        return className;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("class", className).add("configuration", configuration).toString();
    }

}
