package gov.mil.otc._3dvis.datamodel;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;

public class EntityDetail extends TimedData {

    private final EntityType entityType;
    private final Affiliation affiliation;
    private final String name;
    private final String militarySymbol;
    private final int urn;
    private final RtcaState rtcaState;
    private final String source;
    private final int milesPid;
    private final boolean outOfComms;

    private EntityDetail(Builder builder) {
        super(builder.timestamp);
        entityType = builder.entityType;
        affiliation = builder.affiliation;
        name = builder.name;
        militarySymbol = builder.militarySymbol;
        urn = builder.urn;
        rtcaState = builder.rtcaState;
        source = builder.source;
        milesPid = builder.milesPid;
        outOfComms = builder.outOfComms;
    }

    public EntityDetail(EntityDetail entityDetail) {
        super(entityDetail.getTimestamp());
        this.entityType = new EntityType(entityDetail.entityType);
        this.affiliation = entityDetail.affiliation;
        this.name = entityDetail.name;
        this.militarySymbol = entityDetail.militarySymbol;
        this.urn = entityDetail.urn;
        this.rtcaState = new RtcaState(entityDetail.rtcaState);
        this.source = entityDetail.source;
        this.milesPid = entityDetail.milesPid;
        this.outOfComms = entityDetail.outOfComms;
    }

    public EntityDetail(EntityDetail entityDetail, long newTime) {
        super(newTime);
        this.entityType = new EntityType(entityDetail.entityType);
        this.affiliation = entityDetail.affiliation;
        this.name = entityDetail.name;
        this.militarySymbol = entityDetail.militarySymbol;
        this.urn = entityDetail.urn;
        this.rtcaState = new RtcaState(entityDetail.rtcaState);
        this.source = entityDetail.source;
        this.milesPid = entityDetail.milesPid;
        this.outOfComms = entityDetail.outOfComms;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public String getName() {
        return name;
    }

    public String getMilitarySymbol() {
        return militarySymbol;
    }

    public int getUrn() {
        return urn;
    }

    public RtcaState getRtcaState() {
        return rtcaState;
    }

    public String getSource() {
        return source;
    }

    public int getMilesPid() {
        return milesPid;
    }

    public boolean isOutOfComms() {
        return outOfComms;
    }

    public static class Builder {
        private long timestamp = 0;
        private EntityType entityType = EntityType.createUnknown();
        private Affiliation affiliation = Affiliation.UNKNOWN;
        private String name = "";
        private String militarySymbol = "";
        private int urn = 0;
        private RtcaState rtcaState = RtcaState.createAlive();
        private String source = "";
        private int milesPid = 0;
        private boolean outOfComms = false;

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setEntityType(EntityType entityType) {
            this.entityType = entityType;
            return this;
        }

        public Builder setAffiliation(Affiliation affiliation) {
            this.affiliation = affiliation;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMilitarySymbol(String militarySymbol) {
            this.militarySymbol = militarySymbol;
            return this;
        }

        public Builder setUrn(int urn) {
            this.urn = urn;
            return this;
        }

        public Builder setRtcaState(RtcaState rtcaState) {
            this.rtcaState = rtcaState;
            return this;
        }

        public Builder setSource(String source) {
            this.source = source;
            return this;
        }

        public Builder setMilesPid(int milesPid) {
            this.milesPid = milesPid;
            return this;
        }

        public Builder setOutOfComms(boolean outOfComms) {
            this.outOfComms = outOfComms;
            return this;
        }

        public EntityDetail build() {
            return new EntityDetail(this);
        }
    }
}
