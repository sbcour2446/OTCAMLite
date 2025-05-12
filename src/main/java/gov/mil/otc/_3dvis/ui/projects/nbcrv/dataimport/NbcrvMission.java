package gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport;

import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.entity.base.IEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NbcrvMission {

    private final String name;
    private final Mission mission;
    private final List<NbcrvImportFile> nbcrvImportFileList = new ArrayList<>();
    private final Map<String, NbcrvImportEntity> nbcrvImportEntityMap = new HashMap<>();
    private final Map<String, IEntity> otherEntityMap = new HashMap<>();
    private boolean hasRocketSledFile = true;
    private boolean includeUgv = false;
    private boolean flirOnly = false;
    private long startTime = 0;
    private long stopTime = 0;
    private int totalFilesToProcess = 0;

    public NbcrvMission(Mission mission) {
        this.name = mission.getName();
        this.mission = mission;
        this.startTime = mission.getTimestamp();
        this.stopTime = mission.getStopTime();
    }

    public NbcrvMission(String name) {
        this.name = name;
        this.mission = null;
        this.startTime = System.currentTimeMillis();
        this.stopTime = System.currentTimeMillis();
    }

    /**
     * Load mission folder using the following folder structure:
     * ---> {mission name}
     * ---> bft
     * {pcap files, or dvt files containing pcap files}
     * ---> nbcrv
     * ---> {vehicle name (i.e. "NBCRV 1")}
     * ---> atc
     * ---> vehicle_data
     * {gps csv files}
     * ---> video
     * ---> {video set name (i.e. "Assistant Surveyor")}
     * {video files}
     * ---> {video set name (i.e. "DVI Screen Capture")}
     * {video files}
     * ---> flir
     * {detection csv files}
     * {nbcrv csv files}
     * {screenshot image files}
     * ---> manual_data
     * {tir pdf files}
     * {pmcs csv files}
     * {observations csv files}
     * {surveyResponses csv files}
     * ---> oadms
     * {rocket sled xml file}
     * {sidecar enclosure xml file}
     * {wdl or lidar xml file}
     * {video file}
     * ---> {vehicle name (i.e. "Unit 2")}
     * ...
     * ---> otcam
     * {otcam database file}
     * ---> uas
     * {gps csv files}
     *
     * @param missionFolder The mission folder.
     */
    public boolean load(File missionFolder) {
        File[] sourceFolders = missionFolder.listFiles();
        if (sourceFolders == null || sourceFolders.length == 0) {
            return false;
        }
        for (File sourceFolder : sourceFolders) {
            if (!sourceFolder.isDirectory()) {
                continue;
            }
            if (sourceFolder.getName().equalsIgnoreCase("nbcrv")) {
                loadNbcrvFolders(sourceFolder);
            } else {
                loadOtherFiles(sourceFolder);
            }
        }
        return true;
    }

    private void loadNbcrvFolders(File nbcrvMainFolder) {
        File[] nbcrvFolders = nbcrvMainFolder.listFiles();
        if (nbcrvFolders == null) {
            return;
        }
        for (File nbcrvFolder : nbcrvFolders) {
            if (!nbcrvFolder.isDirectory()) {
                continue;
            }
            loadNbcrvFolder(nbcrvFolder);
        }
    }

    private void loadNbcrvFolder(File nbcrvFolder) {
        NbcrvImportEntity nbcrvImportEntity = nbcrvImportEntityMap.get(nbcrvFolder.getName());
        if (nbcrvImportEntity == null) {
            nbcrvImportEntity = new NbcrvImportEntity(nbcrvFolder.getName());
            nbcrvImportEntityMap.put(nbcrvFolder.getName(), nbcrvImportEntity);
        }
        File[] nbcrvSourceFolders = nbcrvFolder.listFiles();
        if (nbcrvSourceFolders == null) {
            return;
        }
        for (File nbcrvSourceFolder : nbcrvSourceFolders) {
            List<File> files = getFiles(nbcrvSourceFolder);
            for (File file : files) {
                nbcrvImportEntity.addFile(new NbcrvImportFile(file, nbcrvSourceFolder.getName(), isNew()));
            }
        }
    }

    private void loadOtherFiles(File directory) {
        List<File> files = getFiles(directory);
        for (File file : files) {
            addFile(new NbcrvImportFile(file, directory.getName(), isNew()));
        }
    }

    public void addFile(NbcrvImportFile nbcrvImportFile) {
        nbcrvImportFileList.add(nbcrvImportFile);
        if (nbcrvImportFile.getFileType() != NbcrvImportFile.FileType.OTHER) {
            totalFilesToProcess++;
        }
    }

    public String getName() {
        return name;
    }

    public Mission getMission() {
        return mission;
    }

    public boolean isNew() {
        return mission == null;
    }

    public List<NbcrvImportFile> getFileList() {
        return nbcrvImportFileList;
    }

    public boolean hasRocketSledFile() {
        return hasRocketSledFile;
    }

    public boolean isIncludeUgv() {
        return includeUgv;
    }

    public void setIncludeUgv(boolean includeUgv) {
        this.includeUgv = includeUgv;
    }

    public boolean isFlirOnly() {
        return flirOnly;
    }

    public void setFlirOnly(boolean flirOnly) {
        this.flirOnly = flirOnly;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public int getTotalFilesToProcess() {
        return totalFilesToProcess;
    }

    public List<NbcrvImportEntity> getNbcrvImportEntityList() {
        return new ArrayList<>(nbcrvImportEntityMap.values());
    }

    public IEntity addEntity(String name, IEntity entity) {
        return otherEntityMap.put(name, entity);
    }

    public IEntity getEntity(String name) {
        return otherEntityMap.get(name);
    }

    private List<File> getFiles(File directory) {
        List<File> files = new ArrayList<>();
        File[] directoryFiles = directory.listFiles();
        if (directoryFiles == null) {
            return files;
        }

        for (File file : directoryFiles) {
            if (file.isDirectory()) {
                files.addAll(getFiles(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }
}
