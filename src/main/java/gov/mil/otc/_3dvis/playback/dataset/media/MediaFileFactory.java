package gov.mil.otc._3dvis.playback.dataset.media;

import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.ui.data.dataimport.media.MediaTimestampFormat;
import gov.mil.otc._3dvis.utility.Utility;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaFileFactory {

    public static MediaFile create(File file, MediaTimestampFormat mediaTimestampFormat) {
        if (file != null && file.exists() && !file.isDirectory()) {
            switch (mediaTimestampFormat) {
                case NONE -> {
                }
                case METADATA -> {
                    return createMediaFileMetadata(file);
                }
                case STANDARD -> {
                    return createMediaFileStandard(file);
                }
                case STANDARD_MILLI -> {
                }
                case STANDARD_MEDIA_SET -> {
                }
                case DAY_OF_YEAR -> {
                    return createMediaFileDayOfYear(file);
                }
                case DAY_OF_YEAR_MILLI -> {
                    return createMediaFileDayOfYearMilli(file);
                }
                case DAY_OF_YEAR_MEDIA_SET -> {
                }
                case SHADOW -> {
                }
                case APACHE -> {
                }
                case BLACK_HAWK -> {
                }
            }
        }
        return null;
    }

    // get timestamp from metadata
    protected static MediaFile createMediaFileMetadata(File file) {
        String mediaSet = file.getParent();
        long startTime = getStartTimeFromMeta(file);
        long stopTime = 0;
        if (startTime > 0) {
            stopTime = startTime + (long) getDuration(file);
        }
        return new MediaFile(file.getAbsolutePath(), startTime, stopTime, "", mediaSet);
    }

    // yyyyMMdd_HHmmss
    protected static MediaFile createMediaFileStandard(File file) {
        String[] filename = file.getName().split("[-_.]");
        if (filename.length < 3) {
            return null;
        }

        String mediaSet = file.getParent();
        String timestampString = filename[0] + filename[1];
        long startTime = Utility.parseTime(timestampString, "yyyyMMddHHmmss");
        long stopTime = 0;
        if (startTime > 0) {
            stopTime = startTime + (long) getDuration(file);
        }
        return new MediaFile(file.getAbsolutePath(), startTime, stopTime, "", mediaSet);
    }

    //DAY_OF_YEAR("yyyy_DDD_HHmmss.[ext]"),
    protected static MediaFile createMediaFileDayOfYear(File file) {
        String[] filename = file.getName().split("[-_.]");
        if (filename.length < 4) {
            return null;
        }

        String mediaSet = file.getParent();
        String timestampString = filename[0] + filename[1] + filename[2];
        long startTime = Utility.parseTime(timestampString, "yyyyDDDHHmmss");
        long stopTime = 0;
        if (startTime > 0) {
            stopTime = startTime + (long) getDuration(file);
        }
        return new MediaFile(file.getAbsolutePath(), startTime, stopTime, "", mediaSet);
    }

    //DAY_OF_YEAR_MILLI("yyyy_DDD_HHmmssSSS.[ext]"),
    protected static MediaFile createMediaFileDayOfYearMilli(File file) {
        String[] filename = file.getName().split("[-_.]");
        if (filename.length < 4) {
            return null;
        }

        String mediaSet = file.getParent();
        String timestampString = filename[0] + filename[1] + filename[2];
        long startTime = Utility.parseTime(timestampString, "yyyyDDDHHmmssSSS");
        long stopTime = 0;
        if (startTime > 0) {
            stopTime = startTime + (long) getDuration(file);
        }
        return new MediaFile(file.getAbsolutePath(), startTime, stopTime, "", mediaSet);
    }

    private static double getDuration(File videoFile) {
        try {
            FFprobe fFprobe = new FFprobe("external\\ffmpeg\\bin\\ffprobe");
            FFmpegProbeResult probeResult = fFprobe.probe(videoFile.getAbsolutePath());
            return probeResult.format.duration * 1000;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return 0;
    }

    private static long getStartTimeFromMeta(File videoFile) {
        String format = "yyyy-MM-dd HH:mm:ss";
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(videoFile))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("CreateDate")) {
                    int beginIndex = line.indexOf("<xmp:CreateDate>") + "<xmp:CreateDate>".length();
                    int endIndex = line.indexOf("</xmp:CreateDate>");
                    String createDateString = line.substring(beginIndex, endIndex - 1);
                    createDateString = createDateString.replace('T', ' ');
                    if (createDateString.length() != format.length()) {
                        createDateString += ":00";
                    }
                    return Utility.parseTime(createDateString, format);
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return 0;
    }
}
