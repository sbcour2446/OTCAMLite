package gov.mil.otc._3dvis.ui.tools.missiontable;

import gov.mil.otc._3dvis.data.mission.Mission;
import gov.mil.otc._3dvis.utility.Utility;

public class MissionView {

    private final Mission mission;

    public MissionView(Mission mission) {
        this.mission = mission;
    }

    public Mission getMission() {
        return mission;
    }

    public String getName() {
        return mission.getName();
    }

    public String getStart() {
        return Utility.formatTime(mission.getTimestamp());
    }

    public String getEnd() {
        return Utility.formatTime(mission.getStopTime());
    }
}
