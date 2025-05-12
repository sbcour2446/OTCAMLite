package gov.mil.otc._3dvis.entity.base;

import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.media.MediaFile;
import gov.mil.otc._3dvis.media.MediaSet;

import java.util.ArrayList;
import java.util.List;

/**
 * Entity class to hold temporary details of an entity.  Entity must be converted to an IEntity implementation
 * before use.
 */
public class AdHocEntity {

    private final List<TspiData> tspiDataList = new ArrayList<>();
    private final List<Event> eventList = new ArrayList<>();
    private final List<MediaSet> mediaSetList = new ArrayList<>();
    private EntityType entityType = EntityType.createUnknown();
    private Affiliation affiliation = Affiliation.UNKNOWN;
    private String name = "";
    private String militarySymbol = "";
    private int urn = 0;
    private RtcaState rtcaState = RtcaState.createAlive();
    private String source = "";
    private int milesPid = -1;
    private boolean outOfComms = false;

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    public List<Event> getEventList() {
        return eventList;
    }

    public void addMediaFile(MediaFile mediaFile) {
        for (MediaSet mediaSet : mediaSetList) {
            if (mediaSet.getName().equals(mediaFile.getMediaSet())) {
                mediaSet.addMediaFile(mediaFile);
                return;
            }
        }
        MediaSet mediaSet = new MediaSet(mediaFile.getMediaSet(), mediaFile.getMediaGroup());
        mediaSet.addMediaFile(mediaFile);
        mediaSetList.add(mediaSet);
    }

    public List<MediaSet> getMediaSetList() {
        return mediaSetList;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    public Affiliation getAffiliation() {
        return affiliation;
    }

    public void setAffiliation(Affiliation affiliation) {
        this.affiliation = affiliation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMilitarySymbol() {
        return militarySymbol;
    }

    public void setMilitarySymbol(String militarySymbol) {
        this.militarySymbol = militarySymbol;
    }

    public int getUrn() {
        return urn;
    }

    public void setUrn(int urn) {
        this.urn = urn;
    }

    public RtcaState getRtcaState() {
        return rtcaState;
    }

    public void setRtcaState(RtcaState rtcaState) {
        this.rtcaState = rtcaState;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public int getMilesPid() {
        return milesPid;
    }

    public void setMilesPid(int milesPid) {
        this.milesPid = milesPid;
    }

    public boolean isOutOfComms() {
        return outOfComms;
    }

    public void setOutOfComms(boolean outOfComms) {
        this.outOfComms = outOfComms;
    }
}
