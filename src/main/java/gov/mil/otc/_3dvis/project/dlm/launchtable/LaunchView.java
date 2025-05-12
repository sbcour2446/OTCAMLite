package gov.mil.otc._3dvis.project.dlm.launchtable;

import gov.mil.otc._3dvis.project.dlm.Launch;
import gov.mil.otc._3dvis.utility.Utility;

public class LaunchView {

    private final Launch launch;

    public LaunchView(Launch launch) {
        this.launch = launch;
    }

    public Launch getLaunch() {
        return launch;
    }

    public String getTime() {
        return Utility.formatTime(launch.getStartTime());
    }

    public String getLaunchNumber() {
        return String.valueOf(launch.getLaunchNumber());
    }
}
