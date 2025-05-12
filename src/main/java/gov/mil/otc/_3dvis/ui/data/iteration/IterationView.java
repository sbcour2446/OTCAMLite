package gov.mil.otc._3dvis.ui.data.iteration;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.data.iteration.Iteration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class IterationView {

    private final Iteration originalIteration;
    private final boolean added;
    private Iteration iteration;
    private boolean invalid = false;
    private boolean updated = false;
    private boolean removed = false;

    public IterationView(Iteration iteration) {
        this(iteration, false);
    }

    public IterationView(Iteration iteration, boolean added) {
        this.originalIteration = iteration;
        this.iteration = iteration;
        this.added = added;
    }

    public void update(Iteration iteration) {
        if (!originalIteration.equals(iteration)) {
            this.iteration = iteration;
            updated = true;
        }
    }

    public void remove() {
        removed = true;
    }

    public void reset() {
        iteration = originalIteration;
        updated = false;
        removed = false;
    }

    public Iteration getOriginalIteration() {
        return originalIteration;
    }

    public Iteration getIteration() {
        return iteration;
    }

    public String getName() {
        return iteration.getName();
    }

    public String getStartTime() {
        LocalDateTime localDateTime = Instant.ofEpochMilli(iteration.getStartTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
        return localDateTime.format(dateTimeFormatter);
    }

    public String getStopTime() {
        if (iteration.getStopTime() == Long.MAX_VALUE) {
            return "live iteration";
        }
        LocalDateTime localDateTime = Instant.ofEpochMilli(iteration.getStopTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
        return localDateTime.format(dateTimeFormatter);
    }

    public String getStatus() {
        String status = "";
        String prefix = "";
        if (isAdded()) {
            status += "new";
            prefix = " - ";
        } else if (isRemoved()) {
            status += "removed";
            prefix = " - ";
        } else if (isUpdated()) {
            status += "updated";
            prefix = " - ";
        }
        if (isInvalid()) {
            status += prefix + "invalid";
        }
        return status;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public void setInvalid(boolean invalid) {
        this.invalid = invalid;
    }

    public boolean isAdded() {
        return added;
    }

    public boolean isUpdated() {
        return updated;
    }

    public boolean isRemoved() {
        return removed;
    }
}
