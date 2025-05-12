package gov.mil.otc._3dvis.data.iteration;

import java.util.Objects;

public class Iteration {

    private final String name;
    private final long startTime;
    private long stopTime = Long.MAX_VALUE;

    public Iteration(String name, long startTime) {
        this.name = name;
        this.startTime = startTime;
    }

    public Iteration(String name, long startTime, long stopTime) {
        this.name = name;
        this.startTime = startTime;
        this.stopTime = stopTime;
    }

    public String getName() {
        return name;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }

    public boolean inIteration(long timestamp) {
        return timestamp >= startTime && timestamp < stopTime;
    }

    public boolean overlap(Iteration iteration) {
        return inIteration(iteration.getStartTime()) ||
                (iteration.getStopTime() > startTime && iteration.getStopTime() < stopTime);
    }

    public boolean isValid() {
        return startTime > 0 && startTime < stopTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Iteration iteration = (Iteration) o;
        return getName().equals(iteration.getName()) &&
                getStartTime() == iteration.getStartTime() &&
                getStopTime() == iteration.getStopTime();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getStartTime(), getStopTime());
    }
}
