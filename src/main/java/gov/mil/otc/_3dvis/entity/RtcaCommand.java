package gov.mil.otc._3dvis.entity;

import gov.mil.otc._3dvis.entity.base.IEntity;

public class RtcaCommand {

    public enum Type {
        RESURRECT("Resurrect", 0),
        KILL_CATASTROPHIC("Kill Catastrophic", 100),
        KILL_MOBILITY("Kill Mobility", 50),
        KILL_FIREPOWER("Kill Firepower", 50),
        KILL_COMMUNICATION("Kill Communication", 25),
        HIT_NO_KILL("Hit", 0),
        MISS("Miss", 0),
        SUPPRESSION("Suppression",0);
        final String name;
        final int damagePercent;

        Type(String name, int damagePercent) {
            this.name = name;
            this.damagePercent = damagePercent;
        }

        public int getDamagePercent() {
            return damagePercent;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final IEntity entity;
    private final Type type;
    private final int suppressionTime;

    public RtcaCommand(IEntity entity, Type type) {
        this(entity, type, 0);
    }

    public RtcaCommand(IEntity entity, Type type, int suppressionTime) {
        this.entity = entity;
        this.type = type;
        this.suppressionTime = suppressionTime;
    }

    public IEntity getEntity() {
        return entity;
    }

    public Type getType() {
        return type;
    }

    public int getSuppressionTime() {
        return suppressionTime;
    }
}
