package ru.runa.wfe.redmine.storage;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.taskadapter.redmineapi.bean.CustomField;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.TrackerFactory;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import org.json.simple.JSONValue;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.AbstractDateFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;

@CommonsLog
public class RedmineFieldMapper {

    static final String FIELD_ID = "id";
    static final String FIELD_SUBJECT = "subject";
    static final String FIELD_DESCRIPTION = "description";
    static final String FIELD_STATUS_ID = "status_id";
    static final String FIELD_ASSIGNED_TO_ID = "assigned_to_id";
    static final String FIELD_TRACKER_ID = "tracker_id";
    static final String FIELD_PRIORITY_ID = "priority_id";
    static final String FIELD_DUE_DATE = "due_date";
    static final String FIELD_START_DATE = "start_date";
    static final String FIELD_DONE_RATIO = "done_ratio";
    static final String FIELD_ESTIMATED_HOURS = "estimated_hours";
    static final String FIELD_SPENT_HOURS = "spent_hours";

    private static final Set<String> BUILTIN_FIELDS = ImmutableSet.of(
            FIELD_ID, FIELD_SUBJECT, FIELD_DESCRIPTION, FIELD_STATUS_ID, FIELD_ASSIGNED_TO_ID,
            FIELD_TRACKER_ID, FIELD_PRIORITY_ID, FIELD_DUE_DATE, FIELD_START_DATE,
            FIELD_DONE_RATIO, FIELD_ESTIMATED_HOURS, FIELD_SPENT_HOURS);

    public static boolean isBuiltinField(String fieldName) {
        return fieldName != null && BUILTIN_FIELDS.contains(fieldName);
    }

    public static String resolveFieldName(VariableDefinition attr) {
        String fieldName = attr.getRedmineFieldName();
        return Strings.isNullOrEmpty(fieldName) ? attr.getName() : fieldName;
    }

    public UserTypeMap toUserTypeMap(UserType userType, Issue issue) {
        UserTypeMap map = new UserTypeMap(userType);
        for (VariableDefinition attr : userType.getAttributes()) {
            String fieldName = requireFieldName(userType, attr);
            map.put(attr.getName(), coerceToAttrType(attr, readField(issue, fieldName)));
        }
        return map;
    }

    private Object coerceToAttrType(VariableDefinition attr, Object value) {
        if (!(value instanceof String) || ((String) value).isEmpty()) {
            return value;
        }
        String raw = (String) value;
        VariableFormat format;
        try {
            format = attr.getFormatNotNull();
        } catch (Exception e) {
            return raw;
        }
        if (String.class.equals(format.getJavaClass())) {
            return raw;
        }
        try {
            if (format instanceof DateTimeFormat) {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ROOT).parse(raw);
            }
            if (format instanceof DateFormat) {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(raw);
            }
            if (format instanceof TimeFormat) {
                return new SimpleDateFormat("HH:mm", Locale.ROOT).parse(raw);
            }
            if (format instanceof AbstractDateFormat) {
                return new SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(raw);
            }
            if (format instanceof BooleanFormat) {
                return "1".equals(raw) || "true".equalsIgnoreCase(raw) || "on".equalsIgnoreCase(raw);
            }
            return format.parse(raw);
        } catch (ParseException | RuntimeException e) {
            log.warn("Redmine: cannot coerce '" + raw + "' for attribute '" + attr.getName()
                    + "' to " + format.getClass().getSimpleName() + ", returning raw string", e);
            return raw;
        }
    }

    public void applyToIssue(UserType userType, UserTypeMap source, Issue issue) {
        for (VariableDefinition attr : userType.getAttributes()) {
            String fieldName = requireFieldName(userType, attr);
            if (FIELD_ID.equals(fieldName) || FIELD_SPENT_HOURS.equals(fieldName)) {
                continue;
            }
            Object value = source.get(attr.getName());
            writeField(issue, fieldName, value);
        }
    }

    private String requireFieldName(UserType userType, VariableDefinition attr) {
        String redmineFieldName = attr.getRedmineFieldName();
        if (!Strings.isNullOrEmpty(redmineFieldName)) {
            return redmineFieldName;
        }
        String attrName = attr.getName();
        if (!isBuiltinField(attrName)) {
            log.debug("Attribute '" + attrName + "' of user type '" + userType.getName()
                    + "' has no redmineFieldName set and its name does not match any built-in Redmine field;"
                    + " falling back to attribute name — Redmine custom field '" + attrName + "' will be expected");
        }
        return attrName;
    }

    private Object readField(Issue issue, String fieldName) {
        switch (fieldName) {
            case FIELD_ID:
                return toLong(issue.getId());
            case FIELD_SUBJECT:
                return issue.getSubject();
            case FIELD_DESCRIPTION:
                return issue.getDescription();
            case FIELD_STATUS_ID:
                return toLong(issue.getStatusId());
            case FIELD_ASSIGNED_TO_ID:
                return toLong(issue.getAssigneeId());
            case FIELD_TRACKER_ID:
                return issue.getTracker() != null ? toLong(issue.getTracker().getId()) : null;
            case FIELD_PRIORITY_ID:
                return toLong(issue.getPriorityId());
            case FIELD_DUE_DATE:
                return issue.getDueDate();
            case FIELD_START_DATE:
                return issue.getStartDate();
            case FIELD_DONE_RATIO:
                return toLong(issue.getDoneRatio());
            case FIELD_ESTIMATED_HOURS:
                return issue.getEstimatedHours();
            case FIELD_SPENT_HOURS:
                return issue.getSpentHours();
            default:
                CustomField cf = issue.getCustomFieldByName(fieldName);
                return cf != null ? cf.getValue() : null;
        }
    }

    private void writeField(Issue issue, String fieldName, Object value) {
        switch (fieldName) {
            case FIELD_SUBJECT:
                issue.setSubject(stringValue(value));
                return;
            case FIELD_DESCRIPTION:
                issue.setDescription(stringValue(value));
                return;
            case FIELD_STATUS_ID:
                issue.setStatusId(toInteger(value));
                return;
            case FIELD_ASSIGNED_TO_ID:
                issue.setAssigneeId(toInteger(value));
                return;
            case FIELD_TRACKER_ID:
                Integer trackerId = toInteger(value);
                issue.setTracker(trackerId != null ? TrackerFactory.create(trackerId) : null);
                return;
            case FIELD_PRIORITY_ID:
                issue.setPriorityId(toInteger(value));
                return;
            case FIELD_DUE_DATE:
                issue.setDueDate(TypeConversionUtil.convertTo(Date.class, value));
                return;
            case FIELD_START_DATE:
                issue.setStartDate(TypeConversionUtil.convertTo(Date.class, value));
                return;
            case FIELD_DONE_RATIO:
                issue.setDoneRatio(toInteger(value));
                return;
            case FIELD_ESTIMATED_HOURS:
                issue.setEstimatedHours(TypeConversionUtil.convertTo(Float.class, value));
                return;
            default:
                CustomField cf = issue.getCustomFieldByName(fieldName);
                if (cf == null) {
                    throw new InternalApplicationException("Redmine: unknown field or unattached custom field '" + fieldName
                            + "' (ensure the custom field exists in Redmine and is attached to the issue)");
                }
                cf.setValue(customFieldValue(value));
        }
    }

    private static String customFieldValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Date) {
            Date date = (Date) value;
            String pattern = hasTimePortion(date) ? "yyyy-MM-dd'T'HH:mm:ssXXX" : "yyyy-MM-dd";
            return new SimpleDateFormat(pattern, Locale.ROOT).format(date);
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "1" : "0";
        }
        if (value instanceof Collection || value instanceof Map) {
            return JSONValue.toJSONString(value);
        }
        return value.toString();
    }

    private static boolean hasTimePortion(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c.get(Calendar.HOUR_OF_DAY) != 0
                || c.get(Calendar.MINUTE) != 0
                || c.get(Calendar.SECOND) != 0
                || c.get(Calendar.MILLISECOND) != 0;
    }

    private static Long toLong(Integer value) {
        return value == null ? null : value.longValue();
    }

    private static Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        Long longValue = TypeConversionUtil.convertTo(Long.class, value);
        return longValue == null ? null : longValue.intValue();
    }

    private static String stringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
