package gov.mil.otc._3dvis.ui.tools.iterationtable;

import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.utility.Utility;

public class IterationView {

    private final Iteration iteration;

    public IterationView(Iteration iteration) {
        this.iteration = iteration;
    }

    public Iteration getMission() {
        return iteration;
    }

    public String getName() {
        return iteration.getName();
    }

    public String getStart() {
        return Utility.formatTime(iteration.getStartTime());
    }

    public String getEnd() {
        return Utility.formatTime(iteration.getStopTime());
    }
}
