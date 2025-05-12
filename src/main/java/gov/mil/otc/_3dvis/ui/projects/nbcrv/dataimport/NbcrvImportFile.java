package gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.data.otcam.OtcamUtility;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.utility.Utility;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NbcrvImportFile {

    public enum FileType {
        OTHER,
        ATC_GPS,
        ATC_3DM,
        ATC_VIDEO,
        BFT_LDIF,
        BFT_PCAP,
        BFT_UTO,
        FLIR_DETECTIONS,
        FLIR_FAULTS,
        FLIR_NBCRV,
        FLIR_RADIO_SUMMARY,
        FLIR_SCREENSHOT,
        FLIR_SNAPSHOT,
        FLIR_UGV,
        MANUAL_DATA,
        OADMS_ROCKETSLED,
        OADMS_SIDECAR_ENCLOSURE,
        OADMS_WDL,
        OADMS_VIDEO,
        OTCAM,
        UAS_GPS,
        URN_MAP,
    }

    private final File file;
    private final FileType fileType;
    private final boolean isNew;

    public NbcrvImportFile(File file, String system, boolean newMission) {
        this.file = file;
        fileType = determineFileType(system);
        isNew = newMission || determineIsNew();
    }

    public File getFile() {
        return file;
    }

    public String getFileName() {
        return file.getName();
    }

    public FileType getFileType() {
        return fileType;
    }

    public boolean getIsNew() {
        return isNew;
    }

    public long getStartTime() {
        if (fileType == FileType.FLIR_SCREENSHOT) {
            return getFlirScreenshotTime(file.getName());
        } else if (fileType == FileType.FLIR_SNAPSHOT) {
            return getFlirSnapshotTime(file.getName());
        } else if (fileType == FileType.OADMS_VIDEO) {
            return getOadmsVideoTime(file.getName());
        } else if (fileType == FileType.ATC_VIDEO) {
            return getAtcVideoTime(file.getName());
        }
        return 0;
    }

    private FileType determineFileType(String system) {
        FileType type = FileType.OTHER;
        if (system.equalsIgnoreCase("atc")) {
            type = determineAtcFileType();
        } else if (system.equalsIgnoreCase("bft")) {
            type = determineBftFileType();
        } else if (system.equalsIgnoreCase("flir")) {
            type = determineFlirFileType();
        } else if (system.equalsIgnoreCase("manual_data")) {
            type = FileType.MANUAL_DATA;
        } else if (system.equalsIgnoreCase("oadms")) {
            type = determineOadmsFileType();
        } else if (system.equalsIgnoreCase("otcam")) {
            type = determineOtcamFileType();
        } else if (system.equalsIgnoreCase("uas")) {
            type = FileType.UAS_GPS;
        }
        return type;
    }

    private FileType determineAtcFileType() {
        String filename = file.getName().toLowerCase();
        if (filename.endsWith(".mp4") || filename.endsWith(".ts") || filename.endsWith(".mkv")) {
            return FileType.ATC_VIDEO;
        } else if (filename.contains("gps") && filename.contains(".csv")) {
            return FileType.ATC_GPS;
        } else if (filename.contains("3dm") && filename.contains(".csv")) {
            return FileType.ATC_3DM;
        }
        return FileType.OTHER;
    }

    private FileType determineBftFileType() {
        String filename = file.getName().toLowerCase();
        if (filename.endsWith(".pcap") || filename.endsWith(".pcapng") || filename.endsWith(".dvt")) {
            return FileType.BFT_PCAP;
        } else if (filename.startsWith("uto")) {
            return FileType.BFT_UTO;
        } else if (filename.endsWith("ldif")) {
            return FileType.BFT_LDIF;
        } else if (filename.equals("urn_map.csv")) {
            return FileType.URN_MAP;
        }
        return FileType.OTHER;
    }

    private FileType determineFlirFileType() {
        FileType type = FileType.OTHER;
        String filenameLowerCase = file.getName().toLowerCase();
        if (filenameLowerCase.endsWith(".csv")) {
            if (filenameLowerCase.startsWith("detections")) {
                type = FileType.FLIR_DETECTIONS;
            } else if (filenameLowerCase.startsWith("faults")) {
                type = FileType.FLIR_FAULTS;
            } else if (filenameLowerCase.startsWith("nbcrv")) {
                type = FileType.FLIR_NBCRV;
            } else if (filenameLowerCase.startsWith("radio_summary")) {
                type = FileType.FLIR_RADIO_SUMMARY;
            } else if (filenameLowerCase.startsWith("ugv")) {
                type = FileType.FLIR_UGV;
            }
        } else if (filenameLowerCase.endsWith(".png")
                && filenameLowerCase.contains("screenshot")) {
            type = FileType.FLIR_SCREENSHOT;
        } else if (filenameLowerCase.endsWith(".png") || filenameLowerCase.endsWith(".jpg")
                && filenameLowerCase.contains("snapshot")) {
            type = FileType.FLIR_SNAPSHOT;
        }
        return type;
    }

    private FileType determineOadmsFileType() {
        String filenameLower = file.getName().toLowerCase();
        if (filenameLower.endsWith(".xml")){
            if (filenameLower.contains("-rocket sled-")) {
                return FileType.OADMS_ROCKETSLED;
            } else if (filenameLower.contains("-sidecar enclosure-")) {
                return FileType.OADMS_SIDECAR_ENCLOSURE;
            } else if (filenameLower.contains("wdl-wdl") || filenameLower.contains("-lidar-")) {
                return FileType.OADMS_WDL;
            }
        } else if (filenameLower.endsWith(".mp4")) {
            return FileType.OADMS_VIDEO;
        }
        return FileType.OTHER;
    }

    private FileType determineOtcamFileType() {
        if (new OtcamUtility(file.getAbsolutePath()).open()) {
            return FileType.OTCAM;
        }
        return FileType.OTHER;
    }

    private boolean determineIsNew() {
        for (DataSource dataSource : DataManager.getDataSources()) {
            if (dataSource.getName().equalsIgnoreCase(getFileName())) {
                return false;
            }
        }
        return true;
    }

    private long getFlirScreenshotTime(String filename) {
        try {
            String[] parts = filename.split("_");
            if (parts.length > 0) {
                return Long.parseLong(parts[0]);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "NbcrvFile:getFlirScreenshotTime", e);
        }
        return 0;
    }

    private long getFlirSnapshotTime(String filename) {
        String[] parts = filename.split(",");
        if (parts.length > 1) {
            DateTimeFormatter dateTimeFormatter = new DateTimeFormatterBuilder()
                    .appendPattern("yyyy-MM-dd")
                    .appendLiteral("T")
                    .appendPattern("HHmmss.SSS")
                    .toFormatter();
            return Utility.parseTime(parts[1].substring(0, parts[1].indexOf("Z")), dateTimeFormatter);
        }
        return 0;
    }

    private long getOadmsVideoTime(String filename) {
        //2022-10-04_00-06-39
        int extIndex = filename.lastIndexOf(".");
        String ext = filename.substring(extIndex);
        if (filename.length() < 19 + ext.length()) {
            return 0;
        }

        String timeString = filename.substring(extIndex - 19, extIndex);
        long startTime = Utility.parseTime(timeString, DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"),
                SettingsManager.getSettings().getNbcrvSettings().getOadmsTimeZoneOffset() );
        return startTime + SettingsManager.getSettings().getNbcrvSettings().getOadmsVideoOffset();
    }

    private long getAtcVideoTime(String filename) {
        //20230514_220753_45FF.mkv
        //H1429_00154330025520230514220406660GAD_16_20230514_220406_NBCRV-02.ts
        String timestampFormat = "yyyyMMdd_HHmmss";
        int extIndex = filename.lastIndexOf(".");
        String ext = filename.substring(extIndex).toLowerCase();
        if (ext.equals(".mkv")) {
            if (filename.length() < timestampFormat.length() + ext.length()) {
                return 0;
            }
            String timeString = filename.substring(0, timestampFormat.length());
            return Utility.parseTime(timeString, DateTimeFormatter.ofPattern(timestampFormat));
        } else if (ext.equals(".ts")) {
            String[] values = filename.split("_");
            if (values.length < 6) {
                return 0;
            }
            String timeString = values[3] + "_" + values[4];
//            if (filename.length() < "xxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx_xx_yyyyMMdd_HHmmss".length() + ext.length()) {
//                return 0;
//            }
//            String timeString = filename.substring("xxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx_xx_".length(),
//                    "xxxxx_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx_xx_".length() + timestampFormat.length());
            return Utility.parseTime(timeString, DateTimeFormatter.ofPattern(timestampFormat));
        }
        return 0;
    }
}
