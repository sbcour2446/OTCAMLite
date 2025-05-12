package gov.mil.otc._3dvis.playback.dataset;

import gov.mil.otc._3dvis.playback.dataset.avcad.AvcadImportFolder;
import gov.mil.otc._3dvis.playback.dataset.bft.BftImportFolder;
import gov.mil.otc._3dvis.playback.dataset.manual.TirImportFolder;
import gov.mil.otc._3dvis.playback.dataset.mrwr.MrwrImportFolder;
import gov.mil.otc._3dvis.playback.dataset.otcam.OtcamImportFolder;
import gov.mil.otc._3dvis.playback.dataset.rpuas.RpuasImportFolder;
import gov.mil.otc._3dvis.playback.dataset.shadow.ShadowImportFolder;
import gov.mil.otc._3dvis.playback.dataset.stryker.StrykerImportFolder;
import gov.mil.otc._3dvis.playback.dataset.tapets.TapetsImportFolder;

import java.io.File;

public class ImportFolderFactory {

    public static ImportFolder create(File file) {
        if (file.getName().equalsIgnoreCase(OtcamImportFolder.FOLDER_NAME)) {
            return new OtcamImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(TirImportFolder.FOLDER_NAME)) {
            return new TirImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(TapetsImportFolder.FOLDER_NAME)) {
            return new TapetsImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(RpuasImportFolder.FOLDER_NAME)) {
            return new RpuasImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(StrykerImportFolder.FOLDER_NAME)) {
            return new StrykerImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(AvcadImportFolder.FOLDER_NAME)) {
            return new AvcadImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(BftImportFolder.FOLDER_NAME)) {
            return new BftImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(MrwrImportFolder.FOLDER_NAME)) {
            return new MrwrImportFolder(file);
        } else if (file.getName().equalsIgnoreCase(ShadowImportFolder.FOLDER_NAME)) {
            return new ShadowImportFolder(file);
        }
        return null;
    }
}
