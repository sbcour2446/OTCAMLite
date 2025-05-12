package gov.mil.otc._3dvis.utility;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Utility {

    public static String formatTime(long timeInMillis) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
        return localDateTime.format(dateTimeFormatter);
    }

    public static String formatTime(long timeInMillis, String format) {
        LocalDateTime localDateTime = Instant.ofEpochMilli(timeInMillis).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(dateTimeFormatter);
    }

    public static long parseTime(String value, String format) {
        return parseTime(value, DateTimeFormatter.ofPattern(format), 0);
    }

    public static long parseTime(String value, DateTimeFormatter dateTimeFormatter) {
        return parseTime(value, dateTimeFormatter, 0);
    }

    public static long parseTime(String value, DateTimeFormatter dateTimeFormatter, int timeZoneOffset) {
        try {
            LocalDateTime localDateTime = LocalDateTime.parse(value, dateTimeFormatter).plusHours(timeZoneOffset);
            Instant instant = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
            return instant.toEpochMilli();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "Utility:parseTime", e);
            return 0;
        }
    }

    public static long tryParseTime(String value) {
        String[] formats = {Common.DATE_TIME, "M/dd/yyyy HH:mm"};

        for (String format : formats) {
            long time = parseTime(value, format);
            if (time > 0) {
                return time;
            }
        }

        return 0;
    }

    public static long dayOfYearToEpoch(int year, int dayOfYear) {
        return Year.of(year).atDay(dayOfYear).atStartOfDay().atZone(ZoneId.of("UTC")).toInstant().toEpochMilli();
    }

    public static double calculateDistance1(Position p1, Position p2) {
        Vec4 v1 = WWController.getWorldWindowPanel().getModel().getGlobe().computePointFromPosition(p1);
        Vec4 v2 = WWController.getWorldWindowPanel().getModel().getGlobe().computePointFromPosition(p2);
        return v1.distanceTo3(v2);
    }

    public static double calculateDistance2(Position p1, Position p2) {
        double[] xyz1 = getXYZfromLatLonRadians(p1.getLatitude().getRadians(), p1.getLongitude().getRadians(), p1.getElevation());
        double[] xyz2 = getXYZfromLatLonRadians(p2.getLatitude().getRadians(), p2.getLongitude().getRadians(), p2.getElevation());
        return Math.sqrt((xyz1[0] - xyz2[0]) * (xyz1[0] - xyz2[0])
                + (xyz1[1] - xyz2[1]) * (xyz1[1] - xyz2[1])
                + (xyz1[2] - xyz2[2]) * (xyz1[2] - xyz2[2]));
    }

    public static double[] getXYZfromLatLonRadians(double latitude, double longitude, double height) {
        double a = 6378137.0; //semi major axis
        double b = 6356752.3142; //semi minor axis
        double cosLat = Math.cos(latitude);
        double sinLat = Math.sin(latitude);

        double rSubN = (a * a) / Math.sqrt(((a * a) * (cosLat * cosLat) + ((b * b) * (sinLat * sinLat))));

        double x = (rSubN + height) * cosLat * Math.cos(longitude);
        double y = (rSubN + height) * cosLat * Math.sin(longitude);
        double z = ((((b * b) / (a * a)) * rSubN) + height) * sinLat;

        return new double[]{x, y, z};
    }

    private Utility() {
    }
}
