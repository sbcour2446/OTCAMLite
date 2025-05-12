package gov.mil.otc._3dvis.data;

import gov.mil.otc._3dvis.data.database.Database;
import gov.mil.otc._3dvis.data.iteration.IIterationListener;
import gov.mil.otc._3dvis.data.iteration.Iteration;
import gov.mil.otc._3dvis.time.TimeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IterationManager {

    private final List<IIterationListener> iterationListeners = Collections.synchronizedList(new ArrayList<>());
    private final List<Iteration> iterationList = Collections.synchronizedList(new ArrayList<>());
    private final Database database;
    private Iteration liveIteration = null;

    protected IterationManager(Database database) {
        this.database = database;
        iterationList.addAll(database.getIterations());
    }

    public Iteration getCurrentIteration() {
        for (Iteration iteration : getIterations()) {
            if (iteration.inIteration(TimeManager.getTime())) {
                return iteration;
            }
        }
        return null;
    }

    public void addListener(IIterationListener iterationListener) {
        iterationListeners.add(iterationListener);
    }

    public List<Iteration> getIterations() {
        return database.getIterations();
    }

    public Iteration startLiveIteration(Iteration iteration) {
        if (liveIteration != null) {
            liveIteration.setStopTime(iteration.getStartTime());
            database.updateIteration(liveIteration, liveIteration);
        }
        liveIteration = iteration;
        database.addIteration(iteration);
        notifyIterationUpdate();
        return iteration;
    }

    public Iteration getLiveIteration() {
        return liveIteration;
    }

    public void stopLiveIteration(long timestamp) {
        if (liveIteration != null) {
            liveIteration.setStopTime(timestamp);
            database.updateIteration(liveIteration, liveIteration);
            liveIteration = null;
        }
        notifyIterationUpdate();
    }

    public void addIteration(Iteration iteration) {
        iterationList.add(iteration);
        database.addIteration(iteration);
        notifyIterationUpdate();
    }

    public void updateIteration(Iteration oldIteration, Iteration newIteration) {
        database.updateIteration(oldIteration, newIteration);
        notifyIterationUpdate();
    }

    public void removeIteration(Iteration iteration) {
        iterationList.remove(iteration);
        database.removeIteration(iteration.getName());
        notifyIterationUpdate();
    }

    private void notifyIterationUpdate() {
        synchronized (iterationListeners) {
            for (IIterationListener iterationListener : iterationListeners) {
                iterationListener.onIterationUpdate(new ArrayList<>(iterationList));
            }
        }
    }
}
