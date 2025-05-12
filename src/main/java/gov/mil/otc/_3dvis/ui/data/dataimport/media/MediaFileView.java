package gov.mil.otc._3dvis.ui.data.dataimport.media;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.media.MediaFile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MediaFileView {

    private final MediaFile mediaFile;
    private final IEntity entity;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
    private final StringProperty entityName = new SimpleStringProperty();
    private final StringProperty filename = new SimpleStringProperty();
    private final StringProperty mediaSet = new SimpleStringProperty();
    private final StringProperty startTime = new SimpleStringProperty();
    private final StringProperty stopTime = new SimpleStringProperty();

    public MediaFileView(MediaFile mediaFile, IEntity entity) {
        this.mediaFile = mediaFile;
        this.entity = entity;
        entityName.set(entity.getName());
        filename.set(mediaFile.getAbsolutePath());
        mediaSet.set(mediaFile.getMediaSet());

        LocalDateTime localDateTime = Instant.ofEpochMilli(mediaFile.getStartTime()).atZone(ZoneId.of("UTC"))
                .toLocalDateTime();
        startTime.set(localDateTime.format(dateTimeFormatter));
        stopTime.set("loading");
        String threadName = "MediaFileView load:" + mediaFile.getAbsolutePath();
        new Thread(this::load, threadName).start();
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public IEntity getEntity() {
        return entity;
    }

    public String getEntityName() {
        return entityName.get();
    }

    public StringProperty entityNameProperty() {
        return entityName;
    }

    public String getFilename() {
        return filename.get();
    }

    public StringProperty filenameProperty() {
        return filename;
    }

    public String getMediaSet() {
        return mediaSet.get();
    }

    public StringProperty mediaSetProperty() {
        return mediaSet;
    }

    public String getStartTime() {
        return startTime.get();
    }

    public StringProperty startTimeProperty() {
        return startTime;
    }

    public String getStopTime() {
        return stopTime.get();
    }

    public StringProperty stopTimeProperty() {
        return stopTime;
    }

    public boolean equals(Object obj) {
        if ((obj instanceof MediaFileView)) {
            return ((MediaFileView) obj).mediaFile.equals(mediaFile);
        }
        return false;
    }

    public int hashCode() {
        return mediaFile.hashCode();
    }

    private void load() {
        try {
            FFprobe fFprobe = new FFprobe("external\\ffmpeg\\bin\\ffprobe");
            FFmpegProbeResult probeResult = fFprobe.probe(mediaFile.getAbsolutePath());
//            if (probeResult.getStreams().size() > 1) {
////                double duration = 30 * 60 * 1000;
////                mediaFile.setStopTime(mediaFile.getStartTime() + (long) duration);
////                LocalDateTime localDateTime = Instant.ofEpochMilli(mediaFile.getStopTime())
////                        .atZone(ZoneId.of("UTC")).toLocalDateTime();
////                stopTime.set(localDateTime.format(dateTimeFormatter));
//                for (FFmpegStream fFmpegStream : probeResult.getStreams()) {
//                    if (fFmpegStream.codec_type.equals(FFmpegStream.CodecType.VIDEO)) {
//                        double duration = fFmpegStream.duration * 1000;
//                        mediaFile.setStopTime(mediaFile.getStartTime() + (long) duration);
//                        LocalDateTime localDateTime = Instant.ofEpochMilli(mediaFile.getStopTime())
//                                .atZone(ZoneId.of("UTC")).toLocalDateTime();
//                        stopTime.set(localDateTime.format(dateTimeFormatter));
//                        break;
//                    }
//                }
//            } else {
            double duration = probeResult.format.duration * 1000;
            mediaFile.setStopTime(mediaFile.getStartTime() + (long) duration);
            LocalDateTime localDateTime = Instant.ofEpochMilli(mediaFile.getStopTime())
                    .atZone(ZoneId.of("UTC")).toLocalDateTime();
            stopTime.set(localDateTime.format(dateTimeFormatter));
//            }
            return;
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        stopTime.set("failed");
    }
}
