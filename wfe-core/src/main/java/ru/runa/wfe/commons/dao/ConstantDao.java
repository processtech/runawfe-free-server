package ru.runa.wfe.commons.dao;

import lombok.val;
import org.springframework.stereotype.Component;

/**
 * DAO for database initialization and variables managing. Creates appropriate
 * tables (drops tables if such tables already exists) and records.
 */
@Component
public class ConstantDao extends GenericDao<Constant> {

    public ConstantDao() {
        super(Constant.class);
    }

    public Constant get(String name) {
        val c = QConstant.constant;
        return queryFactory.selectFrom(c).where(c.name.eq(name)).fetchFirst();
    }

    /**
     * Load constant value. Returns null, if constant is not present.
     * 
     * @param name
     *            constant name.
     * @return constant value.
     */
    public String getValue(String name) {
        Constant constant = get(name);
        if (constant == null) {
            return null;
        }
        return constant.getValue();
    }

    /**
     * Save constant.
     * 
     * @param name
     *            constant name.
     * @param value
     *            constant value.
     */
    public void setValue(String name, String value) {
        Constant constant = get(name);
        if (constant == null) {
            create(new Constant(name, value));
        } else {
            constant.setValue(value);
        }
    }
}
