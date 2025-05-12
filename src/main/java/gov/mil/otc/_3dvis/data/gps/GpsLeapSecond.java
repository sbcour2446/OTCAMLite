package gov.mil.otc._3dvis.data.gps;

import java.util.Calendar;
import java.util.TimeZone;

public class GpsLeapSecond {

    private GpsLeapSecond() {
    }

    public static int getLeapSecond(long millisecondsFromEpoch) {
        Calendar reference = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        reference.set(2015, Calendar.JUNE, 30);
        if (millisecondsFromEpoch > reference.getTimeInMillis()) {
            reference.set(2016, Calendar.DECEMBER, 31);
            if (millisecondsFromEpoch > reference.getTimeInMillis()) {
                return -18000;
            }
            return -17000;
        }
        return -16000;
    }
}
