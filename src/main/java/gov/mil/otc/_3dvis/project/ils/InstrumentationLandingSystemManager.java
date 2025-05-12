package gov.mil.otc._3dvis.project.ils;

import gov.nasa.worldwind.geom.Position;

import java.util.ArrayList;
import java.util.List;

public class InstrumentationLandingSystemManager {

    private static final InstrumentationLandingSystemManager SINGLETON = new InstrumentationLandingSystemManager();
    private final List<InstrumentLandingSystem> instrumentLandingSystemList = new ArrayList<>();

    public static List<InstrumentLandingSystem> getInstrumentationLandingSystems() {
        return new ArrayList<>(SINGLETON.instrumentLandingSystemList);
    }

    public static InstrumentLandingSystem create(Position localizerPosition, Position glideSlopePosition) {
        InstrumentLandingSystem instrumentLandingSystem = new InstrumentLandingSystem(localizerPosition, glideSlopePosition);
        SINGLETON.instrumentLandingSystemList.add(instrumentLandingSystem);
        return instrumentLandingSystem;
    }

    public static void remove(InstrumentLandingSystem instrumentLandingSystem) {
        SINGLETON.instrumentLandingSystemList.remove(instrumentLandingSystem);
    }
}
