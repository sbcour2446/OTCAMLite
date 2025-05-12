package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.symbology.milstd2525.graphics.TacGrpSidc;

import java.util.List;

public class TaskCounterAttack extends AbstractAxisArrowEx {

    /**
     * Indicates the graphics supported by this class.
     *
     * @return List of masked SIDC strings that identify graphics that this
     * class supports.
     */
    public static List<String> getSupportedGraphics() {
        return List.of(TacGrpSidc.TSK_CATK);
    }

    public TaskCounterAttack(String sidc) {
        super(sidc);
        this.setValue(AVKey.ROLLOVER_TEXT, "Task COUNTERATTACK");
        arrowSize = .3;
    }

    @Override
    protected void doRenderGraphic(DrawContext dc) {
        for (Path path : this.paths) {
            path.getAttributes().setOutlineStipplePattern((short) 0xAAAA);
            path.getAttributes().setOutlineStippleFactor(5);
            path.getAttributes().setOutlineWidth(2);
        }
        super.doRenderGraphic(dc);
    }

}
