package ru.runa.wfe.graph;

import com.google.common.collect.Lists;
import java.awt.Color;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.PropertyResources;

@CommonsLog
public final class DrawProperties {
    private static PropertyResources resources = new PropertyResources("graph.properties");

    public static final int TRANSITION_DRAW_WIDTH = 1;
    public static final double TRANSITION_SM_ANGLE = Math.PI / 12;
    public static final double TRANSITION_SM_L = 2.5 / Math.tan(TRANSITION_SM_ANGLE);
    public static final int GRID_SIZE = 12;

    public static final float TRANSITION_CLEAN_WIDTH = 10;
    public static final float FIGURE_CLEAN_WIDTH = 5;

    public static Color getBackgroundColor() {
        return getColorProperty("backgroundColor", Color.WHITE);
    }

    public static Color getFigureBackgroundColor() {
        return getColorProperty("figureBackgroundColor", new Color(0x98, 0xF3, 0xA5));
    }

    public static Color getActiveFigureBackgroundColor() {
        return getColorProperty("activeFigureBackgroundColor", new Color(0x78, 0xff, 0x78));
    }

    public static Color getBaseColor() {
        return getColorProperty("baseColor", Color.BLACK);
    }

    public static Color getTransitionColor() {
        return getColorProperty("transitionColor", Color.BLACK);
    }

    public static Color getHighlightColor() {
        return getColorProperty("highlightColor", new Color(0, 0x99, 0));
    }

    public static String getHighlightColorString() {
        String string = resources.getStringProperty("highlightColor", "009900");
        if (string.startsWith("0x")) {
            string = string.substring(2);
        }
        return "#" + string;
    }

    public static Color getAlarmColor() {
        return getColorProperty("alarmColor", new Color(0x99, 0, 0));
    }

    public static Color getLightAlarmColor() {
        return getColorProperty("lightAlarmColor", new Color(0x99, 0x66, 0x33));
    }

    public static int getFontSize() {
        return resources.getIntegerProperty("fontSize", 9);
    }

    public static int getSmoothDist() {
        return resources.getIntegerProperty("smoothDist", 10);
    }

    public static String getFontFamily() {
        return resources.getStringProperty("fontFamily", "Verdana");
    }

    public static Color getTextColor() {
        return getColorProperty("textColor", new Color(0, 0x99, 0x99));
    }

    public static boolean useEdgingOnly() {
        return resources.getBooleanProperty("edgingOnly", true);
    }

    public static boolean showSwimlaneInBPMN() {
        return resources.getBooleanProperty("bpmn.showSwimlane", true);
    }

    private static Color getColorProperty(String propertyName, Color defaultColor) {
        String colorValue = resources.getStringProperty(propertyName, null);
        if (colorValue != null) {
            try {
                return Color.decode(colorValue);
            } catch (NumberFormatException e) {
                log.error(e.getMessage(), e);
            }
        }
        return defaultColor;
    }

    public static boolean isLogsInGraphEnabled() {
        return resources.getBooleanProperty("logs.enabled", false);
    }

    public static List<Severity> getLogsInGraphSeverities() {
        String name = resources.getStringPropertyNotNull("logs.severity.threshold");
        Severity threshold = Severity.valueOf(name);
        List<Severity> severities = Lists.newArrayList(Severity.values());
        for (Severity severity : Severity.values()) {
            if (severity.ordinal() < threshold.ordinal()) {
                severities.remove(severity);
            }
        }
        return severities;
    }

    public static boolean isSmoothLinesEnabled() {
        return resources.getBooleanProperty("smoothLines", true);
    }
}
