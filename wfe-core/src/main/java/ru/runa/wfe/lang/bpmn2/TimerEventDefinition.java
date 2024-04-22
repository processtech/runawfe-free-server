package ru.runa.wfe.lang.bpmn2;

import com.google.common.base.Preconditions;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.dom4j.Element;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;
import ru.runa.wfe.commons.bc.BusinessDuration;
import ru.runa.wfe.job.StartProcessTimerJob;
import ru.runa.wfe.util.JodaTimeUtils;
import ru.runa.wfe.util.Pair;

/**
 * See BPMN2, &lt;timerEventDefinition&gt;.
 * <ul>
 * <li>For startEvent, timer event(s) is(are) scheduled when process definition is depolyed.
 * <li>For boundaryEvent, timer event(s) is(are) scheduled when process execution enters node to which boundaryEvent is attached.
 * </ul>
 */
public abstract class TimerEventDefinition {

    @RequiredArgsConstructor
    public enum Type {
        DATE,
        DURATION,
        CYCLE
    }

    /**
     * Used when parsing BPMN XML.
     *
     * @param element Must be &lt;timerEventDefinition&gt; element, or null.
     * @return Null if argument is null.
     */
    public static TimerEventDefinition createFromBpmnElement(Element element) {
        if (element == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<Element> children = element.elements();
        Preconditions.checkArgument(children.size() == 1);
        val child = children.get(0);

        String expression = child.getText();
        switch (child.getName()) {
            case "timeDate":
                return new TimeDate(expression);
            case "timeDuration":
                return new TimeDuration(expression);
            case "timeCycle":
                return new TimeCycle(expression);
            default:
                throw new IllegalArgumentException();
        }
    }

    /**
     * Used to construct from TimerJob subclass instance fields.
     */
    public static TimerEventDefinition createFromTypeAndExpression(@NonNull Type type, @NonNull String expression) {
        switch (type) {
            case DATE:
                return new TimeDate(expression);
            case DURATION:
                return new TimeDuration(expression);
            case CYCLE:
                return new TimeCycle(expression);
            default:
                throw new IllegalArgumentException();
        }
    }


    private static final String dateFormat = "yyyy-MM-dd'T'HH:mm";

    private static Date parseDate(String s) {
        try {
            final List<String> split = new ArrayList<>();
            for (String string : s.split("([T ])")) {
                if (!string.trim().isEmpty()) {
                    split.add(string);
                }
            }
            if (split.size() == 2) {
                // TODO If you want universal parser, use Joda-Time's ISODateTimeFormat: https://stackoverflow.com/questions/2201925
                return new SimpleDateFormat(dateFormat).parse(s);
            }

            if (split.size() != 1) {
                throw new ParseException("Unparseable date: " + s, 0);
            }

            final String dateOrTime = split.get(0);
            if (dateOrTime.contains("-")) {
                final LocalDate date = LocalDate.parse(dateOrTime);
                return Date.from(LocalDateTime.of(date, LocalTime.now().plusMinutes(1L)).atZone(ZoneId.systemDefault()).toInstant());
            } else {
                final LocalDateTime now = LocalDateTime.now();
                final LocalTime time = LocalTime.parse(dateOrTime);
                final LocalDateTime dateTime = LocalDateTime.of(LocalDate.now(), time);

                if (dateTime.isBefore(now)) {
                    return Date.from(dateTime.plusDays(1L).atZone(ZoneId.systemDefault()).toInstant());
                }
                return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String formatDate(Date d) {
        return new SimpleDateFormat(dateFormat).format(d);
    }


    /**
     * Milliseconds. How long after {@link StartProcessTimerJob#timerEventNextDate} process is allowed to start.
     * After that (e.g. if TimerJobExecutor took too long to process other tasks before current StartProcessTimerJob) process start is skipped,
     * and only {@link StartProcessTimerJob#timerEventNextDate} and {@link StartProcessTimerJob#timerEventRemainingCount} are adjusted.
     * <p>
     * See: #1243-55, {@link #parseDuration(String)}.
     */
    public static final int ALLOW_START_PROCESS_DELAY = 30 * 1000;

    /**
     * Accepts intervals in form PnW or PnYnMnDTnHnM, but seconds are forbidden.
     * <p>
     * This is because TimerJobExecutor checks condition "next event time was hit just now" with 30 seconds precision
     * ({@link #ALLOW_START_PROCESS_DELAY}); which in turn is because TimerJobExecutor runs every 5 seconds
     * but may ocassionally take more time to run.
     * Since UI (both DevStudio and web site) never allows users to input seconds anyway, half-minute precision is ok.
     * See: #1243-55, {@link #computeNextEvent(TimeCycle, Date, Date, Long, int)}.
     */
    private static Period parseDuration(String s) {
        val p = Period.parse(s);
        if (p.getSeconds() != 0) {
            throw new IllegalArgumentException("Seconds are disallowed in durations, in '" + s + "'");
        }
        return p;
    }

    static String formatDuration(Period p) {
        return ISOPeriodFormat.standard().print(p);
    }


    /**
     * @param baseDate             Either startDate from which periods are counted, or previous (just triggered) event planned timestamp.
     * @param remainingCount       Infinite if null.
     * @param alreadyPassedPeriods 0 if called for startDate, 1 if called for previous date (which counts period before previous date).
     * @return Values for {@link StartProcessTimerJob#timerEventNextDate} and for {@link StartProcessTimerJob#timerEventRemainingCount}.
     * If returns null, job must be deleted.
     */
    public static Pair<Date, Long> computeNextEvent(
            @NonNull TimeCycle def,
            @NonNull Date now,
            @NonNull Date baseDate,
            Long remainingCount,
            int alreadyPassedPeriods
    ) {
        Preconditions.checkArgument(alreadyPassedPeriods >= 0);
        if (remainingCount != null && remainingCount <= 0) {
            return null;
        }

        // Process must be started if (now <= baseDate + duration + ALLOW_START_PROCESS_DELAY).
        // So, we skip period only if (now > baseDate + duration + DELAY).
        val start = baseDate.getTime() + ALLOW_START_PROCESS_DELAY;
        val end = now.getTime();
        if (start < end) {
            // Add full periods.
            alreadyPassedPeriods += JodaTimeUtils.countFullPeriodsInInterval(new Interval(start, end), def.duration);
        }
        Date nextDate = new DateTime(baseDate).plus(def.duration.multipliedBy(alreadyPassedPeriods)).toDate();
        if (now.getTime() > nextDate.getTime() + ALLOW_START_PROCESS_DELAY) {
            // Add incomplete period.
            alreadyPassedPeriods++;
            nextDate = new DateTime(baseDate).plus(def.duration.multipliedBy(alreadyPassedPeriods)).toDate();
            // One iteration must be enough; otherwise it's a bug.
            Preconditions.checkState(now.getTime() <= nextDate.getTime() + ALLOW_START_PROCESS_DELAY);
        }

        if (remainingCount != null) {
            remainingCount -= alreadyPassedPeriods;
            if (remainingCount <= 0) {
                return null;
            }
        }

        return new Pair<>(nextDate, remainingCount);
    }


    private TimerEventDefinition() {
    }

    /**
     * Serialized to database; see also {@link #createFromTypeAndExpression(Type, String)}.
     */
    public abstract Type getType();

    /**
     * Serialized to database; see also {@link #createFromTypeAndExpression(Type, String)}.
     */
    public abstract String getExpression();

    /**
     * @see ru.runa.wfe.commons.bc.AbstractBusinessCalendar#apply(Date, BusinessDuration)
     */
    public abstract Date addDurationToDate(Date date);


    /**
     * See BPMN2, &lt;timerEventDefinition&gt; / &lt;timeDate&gt;.
     */
    public static final class TimeDate extends TimerEventDefinition {

        /**
         * When timer event will be triggered.
         * <p>
         * If {@code timestamp} is already in the past when timer event is scheduled (see {@link TimerEventDefinition}'s javadoc),
         * then scheduling is cancelled.
         */
        public final Date timestamp;

        public TimeDate(String expression) {
            timestamp = TimerEventDefinition.parseDate(expression);
        }

        @Override
        public Type getType() {
            return Type.DATE;
        }

        @Override
        public String getExpression() {
            return TimerEventDefinition.formatDate(timestamp);
        }

        @Override
        public Date addDurationToDate(Date date) {
            throw new IllegalAccessError();
        }
    }

    /**
     * See BPMN2, &lt;timerEventDefinition&gt; / &lt;timeDuration&gt;.
     */
    public static final class TimeDuration extends TimerEventDefinition {

        /**
         * Duration between scheduling and triggering timer event.
         * <p>
         * This is ISO 8601 "duration + context" syntax, where "context" is event scheduling time.
         * Other syntaxes ("start + end", "start + duration", "duration + end") are not supported.
         * <p>
         * TODO Merge with BusinessDuration: see #1243-46, #1243-56.
         */
        public final Period duration;

        public TimeDuration(String expression) {
            duration = TimerEventDefinition.parseDuration(expression);
        }

        @Override
        public Type getType() {
            return Type.DURATION;
        }

        @Override
        public String getExpression() {
            return TimerEventDefinition.formatDuration(duration);
        }

        @Override
        public Date addDurationToDate(Date date) {
            return new DateTime(date).plus(duration).toDate();
        }
    }

    /**
     * See BPMN2, &lt;timerEventDefinition&gt; / &lt;timeCycle&gt;.
     * <p>
     * Does NOT extend TimeDuration, to avoid confusion with "instanceof" checks.
     */
    public static final class TimeCycle extends TimerEventDefinition {
        private static Pattern expressionRegex = Pattern.compile("R(\\d{0,19})(/[^/]+)?/([^/]+)");

        /**
         * How many timer events to trigger. Positive. Null means "unbounded".
         * <p>
         * If {@link #start} is already in the past when timer event is scheduled (see {@link TimerEventDefinition}'s javadoc),
         * and {@code count} is non-null, {@code count} is reduced to skip already expired periods.
         * If resulting {@code count} is 0 or less, scheduling is cancelled.
         */
        public final Long count;

        /**
         * When first timer event will be triggered. Null means "{@code duration} after scheduling".
         * <p>
         * If {@code start} is already in the past when timer event is scheduled (see {@link TimerEventDefinition}'s javadoc),
         * and {@link #count} is non-null, {@code count} is decreased to skip already expired periods.
         * If resulting {@code count} is 0 or less, scheduling is cancelled.
         */
        public final Date start;

        /**
         * Duration between timer events, and if {@link #start} is null, then also between scheduling and first timer events.
         */
        public final Period duration;

        /**
         * Format: "R/duration", or "Rcount/duration", or "R/start/duration", or "Rcount/start/duration".
         * <p>
         * See also {@link #start}, {@link #duration}, {@link #count}.
         * <p>
         * Examples:
         * <ul>
         * <li>"R/PT5M" -- trigger timer event indefinitely, every 5 minutes, first event after 5 minutes after scheduling.
         * <li>"R20/P1D" -- trigger timer event 20 times, every 1 day, first event after 1 day after scheduling.
         * <li>"R366/2020-01-01T00:00/P1D" -- trigger timer event every midinght in year 2020 (it's a leap year, so I specified 366 repeats).
         *     If you schedule this event in the middle of the year 2020, it will be triggered only for remaining midnights;
         *     If you schedule this event in year 2021 or later, no events will be triggered.
         * </ul>
         */
        public TimeCycle(String expression) {
            val m = expressionRegex.matcher(expression);
            Preconditions.checkArgument(m.matches());
            if (m.group(1).isEmpty()) {
                count = null;
            } else {
                count = Long.parseLong(m.group(1));
                Preconditions.checkArgument(count > 0);
            }
            if (m.group(2) == null) {
                start = null;
            } else {
                start = TimerEventDefinition.parseDate(m.group(2).substring(1));
            }
            duration = TimerEventDefinition.parseDuration(m.group(3));
        }

        @Override
        public Type getType() {
            return Type.CYCLE;
        }

        @Override
        public String getExpression() {
            val sb = new StringBuilder(60);
            sb.append('R');
            if (count != null) {
                sb.append(count);
            }
            if (start != null) {
                sb.append('/').append(TimerEventDefinition.formatDate(start));
            }
            sb.append('/').append(formatDuration(duration));
            return sb.toString();
        }

        @Override
        public Date addDurationToDate(Date date) {
            return new DateTime(date).plus(duration).toDate();
        }
    }
}
