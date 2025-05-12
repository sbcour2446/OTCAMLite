/*
 *   Task CANALYZE from MIL-STD 2525B
 *   2.X.1.4  (G*TPC-----****X)
 *
 *  GaN Corp. for Army OTC  (3DVis)
 *
 */
package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.List;

/**
 * @author Chris
 */
public class TaskCanalize extends TaskBreach {

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this
     * class supports.
     */
    public static List<String> getSupportedGraphics() {
        return List.of(TacGrpSidc.TSK_CNZ);
    }

    public TaskCanalize(String symbolCode) {
        super(symbolCode);
        this.setValue(AVKey.ROLLOVER_TEXT, "Task CANALIZE");
        isBreach = false;
    }


}
