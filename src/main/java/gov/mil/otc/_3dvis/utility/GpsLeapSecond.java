package gov.mil.otc._3dvis.utility;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class GpsLeapSecond {

    public static int getLeapSecond(long millisecondsFromEpoch) {
        LocalDateTime localDateTime = LocalDateTime.of(2016, 12, 31, 0, 0, 0, 0);
        Instant reference = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
        if (millisecondsFromEpoch > reference.toEpochMilli()) {
            return -18000;
        }

        localDateTime = LocalDateTime.of(2015, 7, 1, 0, 0, 0, 0);
        reference = localDateTime.atZone(ZoneId.of("UTC")).toInstant();
        if (millisecondsFromEpoch > reference.toEpochMilli()) {
            return -17000;
        }

        return -16000;
    }

    private GpsLeapSecond() {
    }
}
