package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525;

import gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.graphics.lines.*;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;

public class MilStd2525GraphicFactoryEx extends MilStd2525GraphicFactory {

    @Override
    protected void populateClassMap() {
        super.populateClassMap();

        mapClass(TaskBreach.class, TaskBreach.getSupportedGraphics());
        mapClass(TaskBypass.class, TaskBypass.getSupportedGraphics());
        mapClass(TaskCanalize.class, TaskCanalize.getSupportedGraphics());
        mapClass(TaskClear.class, TaskClear.getSupportedGraphics());
        mapClass(TaskCounterAttack.class, TaskCounterAttack.getSupportedGraphics());
    }
}
