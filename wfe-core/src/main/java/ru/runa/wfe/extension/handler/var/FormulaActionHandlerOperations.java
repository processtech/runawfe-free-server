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
package ru.runa.wfe.extension.handler.var;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.TypeConversionUtil;

public class FormulaActionHandlerOperations {
    private static final Log log = LogFactory.getLog(FormulaActionHandlerOperations.class);

    public Object sum(Object o1, Object o2) {
        if (BigDecimal.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return ((BigDecimal) o1).add(asBigDecimal((Number) o2));
        }
        if (Number.class.isInstance(o1) && BigDecimal.class.isInstance(o2)) {
            return asBigDecimal((Number) o1).add((BigDecimal) o2);
        }
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() + ((Number) o2).doubleValue());
        }
        if (Number.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() + ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Long((long) (((Long) o1).doubleValue() + ((Long) o2).doubleValue()));
        }
        if (Date.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Date(((Date) o1).getTime() + (long) (((Number) o2).doubleValue() * 60 * 1000));
        }
        if (Date.class.isInstance(o1) && Date.class.isInstance(o2)) {
            Date date2 = (Date) o2;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date2);
            return new Date(((Date) o1).getTime() + calendar.get(Calendar.HOUR_OF_DAY) * 3600000 + calendar.get(Calendar.MINUTE) * 60000);
        }
        if (String.class.isInstance(o1)) {
            return (String) o1 + translate(o2, String.class);
        }
        if (String.class.isInstance(o2)) {
            return translate(o1, String.class).toString() + (String) o2;
        }
        log.error("Cannot make summation for " + (o1 != null ? o1.getClass() : "null") + " with " + (o2 != null ? o2.getClass() : "null"));
        return null;
    }

    public Object translate(Object o, Class<?> c) {
        if (c == String.class && Date.class.isInstance(o)) {
            Date date = (Date) o;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) == 1970 && calendar.get(Calendar.MONTH) == Calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                return CalendarUtil.format(date, CalendarUtil.HOURS_MINUTES_FORMAT);
            }
            if (calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
                return CalendarUtil.format(date, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
            }
            return CalendarUtil.format(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        }
        if (Date.class.isAssignableFrom(c) && Date.class.isInstance(o)) {
            return o;
        }
        return TypeConversionUtil.convertTo(c, o);
    }

    public Object sub(Object o1, Object o2) {
        if (BigDecimal.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return ((BigDecimal) o1).subtract(asBigDecimal((Number) o2));
        }
        if (Number.class.isInstance(o1) && BigDecimal.class.isInstance(o2)) {
            return asBigDecimal((Number) o1).subtract((BigDecimal) o2);
        }
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() - ((Number) o2).doubleValue());
        }
        if (Number.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() - ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Long((long) (((Number) o1).doubleValue() - ((Number) o2).doubleValue()));
        }
        if (Date.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Date(((Date) o1).getTime() - (long) (((Number) o2).doubleValue() * 60 * 1000));
        }
        if (Date.class.isInstance(o1) && Date.class.isInstance(o2)) {
            return new Long((((Date) o1).getTime() - ((Date) o2).getTime()) / 60000);
        }
        log.error("Cannot make substraction for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object mul(Object o1, Object o2) {
        if (BigDecimal.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return ((BigDecimal) o1).multiply(asBigDecimal((Number) o2));
        }
        if (Number.class.isInstance(o1) && BigDecimal.class.isInstance(o2)) {
            return asBigDecimal((Number) o1).multiply((BigDecimal) o2);
        }
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() * ((Number) o2).doubleValue());
        }
        if (Number.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() * ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Long((long) (((Number) o1).doubleValue() * ((Number) o2).doubleValue()));
        }
        log.error("Cannot make multiplication for " + (o1 != null ? o1.getClass() : "null") + " with " + (o2 != null ? o2.getClass() : "null"));
        return null;
    }

    public Object div(Object o1, Object o2) {
        if (BigDecimal.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return ((BigDecimal) o1).divide(asBigDecimal((Number) o2), MathContext.DECIMAL128);
        }
        if (Number.class.isInstance(o1) && BigDecimal.class.isInstance(o2)) {
            return asBigDecimal((Number) o1).divide((BigDecimal) o2, MathContext.DECIMAL128);
        }
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Double) o1).doubleValue() / ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Long((long) (((Long) o1).doubleValue() / ((Number) o2).doubleValue()));
        }
        log.error("Cannot make division for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object changeSign(Object o) {
        if (BigDecimal.class.isInstance(o)) {
            return ((BigDecimal) o).negate();
        }
        if (Double.class.isInstance(o)) {
            return new Double(-((Double) o).doubleValue());
        }
        if (Long.class.isInstance(o)) {
            return new Long(-((Long) o).longValue());
        }
        log.error("Cannot make changeSign for " + o.getClass());
        return null;
    }

    public Object not(Object o) {
        if (Boolean.class.isInstance(o)) {
            return new Boolean(!((Boolean) o).booleanValue());
        }
        log.error("Cannot make not for " + o.getClass());
        return null;
    }

    public Object less(Object o1, Object o2) {
        if (BigDecimal.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return ((BigDecimal) o1).compareTo(asBigDecimal((Number) o2)) < 0;
        }
        if (Number.class.isInstance(o1) && BigDecimal.class.isInstance(o2)) {
            return asBigDecimal((Number) o1).compareTo((BigDecimal) o2) < 0;
        }
        if (Double.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Boolean(((Double) o1).doubleValue() < ((Double) o2).doubleValue());
        }
        if (Double.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Boolean(((Double) o1).doubleValue() < ((Long) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Boolean(((Long) o1).doubleValue() < ((Double) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Boolean(((Long) o1).longValue() < ((Long) o2).longValue());
        }
        if (String.class.isInstance(o1) && String.class.isInstance(o2)) {
            return new Boolean(((String) o1).compareTo((String) o2) < 0);
        }
        if (Date.class.isInstance(o1) && Date.class.isInstance(o2)) {
            return new Boolean(((Date) o1).compareTo((Date) o2) < 0);
        }
        log.error("Cannot make less for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object bigger(Object o1, Object o2) {
        return less(o2, o1);
    }

    public Object equal(Object o1, Object o2) {
        if (BigDecimal.class.isInstance(o1) && BigDecimal.class.isInstance(o2)) {
            return new Boolean(((BigDecimal) o1).compareTo((BigDecimal) o2) == 0);
        }
        return new Boolean(o1.equals(o2));
    }

    public Object lessOrEqual(Object o1, Object o2) {
        Object less = less(o1, o2);
        Object equal = equal(o1, o2);
        return or(less, equal);
    }

    public Object biggerOrEqual(Object o1, Object o2) {
        Object bigger = bigger(o1, o2);
        Object equal = equal(o1, o2);
        return or(bigger, equal);
    }

    public Object or(Object o1, Object o2) {
        if (Boolean.class.isInstance(o1) && Boolean.class.isInstance(o2)) {
            return new Boolean(((Boolean) o1).booleanValue() || ((Boolean) o2).booleanValue());
        }
        log.error("Cannot make or for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object and(Object o1, Object o2) {
        if (Boolean.class.isInstance(o1) && Boolean.class.isInstance(o2)) {
            return new Boolean(((Boolean) o1).booleanValue() && ((Boolean) o2).booleanValue());
        }
        log.error("Cannot make and for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object xor(Object o1, Object o2) {
        if (Boolean.class.isInstance(o1) && Boolean.class.isInstance(o2)) {
            return new Boolean(((Boolean) o1).booleanValue() ^ ((Boolean) o2).booleanValue());
        }
        log.error("Cannot make xor for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    private BigDecimal asBigDecimal(Number n) {
        if (BigDecimal.class.isInstance(n)) {
            return (BigDecimal) n;
        } else if (Double.class.isInstance(n)) {
            return BigDecimal.valueOf((Double) n);
        } else {
            return BigDecimal.valueOf((Long) n);
        }
    }

}