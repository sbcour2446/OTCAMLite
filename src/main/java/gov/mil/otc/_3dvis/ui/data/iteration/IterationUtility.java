package gov.mil.otc._3dvis.ui.data.iteration;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.ui.utility.DialogUtilities;

public class IterationUtility {

    public static void startNewIteration() {
        Iteration iteration = IterationFormController.showLiveIteration(MainApplication.getInstance().getStage());
        if (iteration == null) {
            return;
        }
        Iteration liveIteration = DataManager.getIterationManager().getLiveIteration();
        if (liveIteration != null && !DialogUtilities.showYesNoDialog("Start New Iteration",
                "Do you wish to stop current iteration and start a new iteration?", null)) {
            return;
        }
        DataManager.getIterationManager().startLiveIteration(iteration);
    }

    public static void stopCurrentIteration() {
        Iteration liveIteration = DataManager.getIterationManager().getLiveIteration();
        if (liveIteration == null) {
            DialogUtilities.showInformationDialog("Stop Iteration", "Stop Iteration",
                    "No iteration currently set.", MainApplication.getInstance().getStage());
            return;
        } else if (!DialogUtilities.showYesNoDialog("Stop Iteration",
                "Do you wish to stop current iteration?", null)) {
            return;
        }
        DataManager.getIterationManager().stopLiveIteration(System.currentTimeMillis());
    }

    private IterationUtility() {
    }
}
