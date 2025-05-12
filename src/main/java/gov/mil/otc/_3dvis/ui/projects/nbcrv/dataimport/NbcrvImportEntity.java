package gov.mil.otc._3dvis.ui.projects.nbcrv.dataimport;

import java.util.ArrayList;
import java.util.List;

public class NbcrvImportEntity {

    private final String name;
    private final List<NbcrvImportFile> nbcrvImportFileList = new ArrayList<>();
    private int totalFilesToProcess = 0;

    public NbcrvImportEntity(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addFile(NbcrvImportFile nbcrvImportFile) {
        nbcrvImportFileList.add(nbcrvImportFile);
        if (nbcrvImportFile.getFileType() != NbcrvImportFile.FileType.OTHER) {
            totalFilesToProcess++;
        }
    }

    public List<NbcrvImportFile> getNbcrvImportFileList() {
        return nbcrvImportFileList;
    }

    public int getTotalFilesToProcess() {
        return totalFilesToProcess;
    }
}
