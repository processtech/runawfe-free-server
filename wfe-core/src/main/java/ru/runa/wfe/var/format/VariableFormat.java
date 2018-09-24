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
package ru.runa.wfe.var.format;

import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * Variable format allows convertions between Strings and Objects. Each variable in process definition bound to specific format.
 *
 * @author dofs
 * @since 4.0
 */
public abstract class VariableFormat {

    public abstract Class<?> getJavaClass();

    public abstract String getName();

    /**
     * Parses variable object from strings. Array of strings here due to conversation from html form.
     *
     * @param source
     *            serialized string.
     * @return object, can be <code>null</code>
     */
    public final Object parse(String source) {
        if (source == null) {
            return null;
        }
        try {
            return convertFromStringValue(source);
        } catch (Exception e) {
            throw new InternalApplicationException("Unable to parse '" + source + "' in " + this, e);
        }
    }

    protected abstract Object convertFromStringValue(String source) throws Exception;

    /**
     * Formats given variable object.
     *
     * @param object
     *            object, can be <code>null</code>
     * @return formatted string or <code>null</code>
     */
    public final String format(Object object) {
        if (object == null) {
            return null;
        }
        return convertToStringValue(object);
    }

    protected abstract String convertToStringValue(Object object);

    /**
     * Parses variable object from JSON string.
     *
     * @return object, can be <code>null</code>
     */
    public Object parseJSON(String json) {
        try {
            if (json == null) {
                return null;
            }
            JSONParser parser = new JSONParser();
            Object jsonObject = parser.parse(json);
            if (jsonObject == null) {
                return null;
            }
            return convertFromJSONValue(jsonObject);
        } catch (ParseException e) {
            throw new InternalApplicationException("Unable to parse '" + json + "' in " + this, e);
        }
    }

    protected Object convertFromJSONValue(Object jsonValue) {
        if (jsonValue != null && getJavaClass().isAssignableFrom(jsonValue.getClass())) {
            return jsonValue;
        }
        return TypeConversionUtil.convertTo(getJavaClass(), jsonValue);
    }

    /**
     * Formats given variable object to JSON format.
     *
     * @param value Can be <code>null</code>.
     * @return JSON string or <code>null</code>.
     */
    public final String formatJSON(Object value) {
        if (value == null) {
            return null;
        }
        return JSONValue.toJSONString(convertToJSONValue(value));
    }

    protected Object convertToJSONValue(Object value) {
        return value;
    }

    /**
     * Applies operation depends on variable format type.
     *
     * @param operation
     *            Operation, applied to format.
     * @param context
     *            Operation call context. Contains additional data for operation.
     * @return Returns operation result.
     */
    public <TResult, TContext> TResult processBy(VariableFormatVisitor<TResult, TContext> operation, TContext context) {
        return operation.onOther(this, context);
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}
