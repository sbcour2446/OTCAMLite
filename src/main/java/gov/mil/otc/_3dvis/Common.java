package gov.mil.otc._3dvis;

import java.awt.*;

public class Common {

    public static final Font FONT_XLARGE = Font.decode("consolas-plain-20");
    public static final Font FONT_XLARGE_BOLD = Font.decode("consolas-bold-20");
    public static final Font FONT_LARGE = Font.decode("consolas-plain-18");
    public static final Font FONT_LARGE_BOLD = Font.decode("consolas-bold-18");
    public static final Font FONT_MEDIUM = Font.decode("consolas-plain-14");
    public static final Font FONT_MEDIUM_BOLD = Font.decode("consolas-bold-14");
    public static final Font FONT_SMALL = Font.decode("consolas-plain-12");
    public static final Font FONT_SMALL_BOLD = Font.decode("consolas-bold-12");
    public static final Font FONT_XSMALL = Font.decode("consolas-plain-10");

    public static final String DATE_ONLY = "yyyy-MM-dd";
    public static final String TIME_ONLY = "HH:mm:ss";
    public static final String TIME_ONLY_WITH_MILLIS = "HH:mm:ss.SSS";
    public static final String TIME_WITH_ZONE = "HH:mm:ss zzz";
    public static final String DATE_TIME = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_TIME_SHORT = "MM-dd HH:mm:ss";
    public static final String DATE_TIME_HHmm = "yyyy-MM-dd HHmm";
    public static final String DATE_TIME_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_TIME_DECORATED = "EEE MMM dd yyyy   HH:mm:ss zzz";

    private Common() {
    }
}
