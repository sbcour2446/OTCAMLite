package gov.mil.otc._3dvis.data.ulog;

import gov.mil.otc._3dvis.data.tpsi.TspiDataFile;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.datamodel.timed.ValuePairTimedData;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ULogFile implements TspiDataFile {

    private final File file;
    private ULogReader uLogReader = null;
    private final List<TspiData> tspiData = new ArrayList<>();
    private final List<ValuePairTimedData> otherGpsData = new ArrayList<>();
//    private final List<ValuePairTimedData> batteryData = new ArrayList<>();

    public ULogFile(File file) {
        this.file = file;
    }

    public boolean process() {
        try {
            uLogReader = new ULogReader(file);
            tspiData.clear();
            otherGpsData.clear();
//            batteryData.clear();
            uLogReader.getData(tspiData, otherGpsData);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "ULogFile::process", e);
        }

        return true;
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public List<TspiData> getTspiDataList() {
        return tspiData;
    }

    public List<ValuePairTimedData> getOtherGpsData() {
        return otherGpsData;
    }
//
//    public List<ValuePairTimedData> getBatteryData() {
//        return batteryData;
//    }
}
