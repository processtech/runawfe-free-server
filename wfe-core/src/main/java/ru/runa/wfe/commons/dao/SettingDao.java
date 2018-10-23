/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.commons.dao;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.val;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * DAO for database initialization and variables managing. Creates appropriate
 * tables (drops tables if such tables already exists) and records.
 */
@Component
// TODO rm700
@Transactional
public class SettingDao extends GenericDao<Setting> {

    public SettingDao() {
        super(Setting.class);
    }

    public String getValue(String fileName, String name) {
        val s = QSetting.setting;
        return queryFactory.select(s.value).from(s).where(s.fileName.eq(fileName).and(s.name.eq(name))).fetchFirst();
    }

    public void setValue(String fileName, String name, String value) {
        log.debug("setValue(" + fileName + ", " + name + ", " + value + ")");

        val s = QSetting.setting;
        BooleanExpression cond = s.fileName.eq(fileName).and(s.name.eq(name));

        if (value == null) {
            queryFactory.delete(s).where(cond).execute();
            return;
        }

        val id = queryFactory.select(s.id).from(s).where(cond).fetchFirst();
        if (id == null) {
            create(new Setting(fileName, name, value));
        } else {
            queryFactory.update(s).set(s.value, value).where(cond).execute();
        }
    }

    public void clear() {
        val s = QSetting.setting;
        queryFactory.delete(s).execute();
    }
}
