package gov.mil.otc._3dvis.entity.base;

import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityFilter;
import gov.mil.otc._3dvis.entity.RtcaCommand;
import gov.mil.otc._3dvis.event.Event;
import gov.mil.otc._3dvis.media.MediaCollection;
import gov.mil.otc._3dvis.settings.IconType;
import gov.nasa.worldwind.geom.Position;
import javafx.scene.control.ContextMenu;

import java.awt.image.BufferedImage;
import java.util.List;

public interface IEntity {

    EntityId getEntityId();

    String getName();

    void addContextMenuItems(ContextMenu contextMenu);

    void setRealtimeStart(long timestamp);

    void setRealtimeStop(long timestamp);

    void addEntityScope(EntityScope entityScope);

    void addEntityDetail(EntityDetail entityDetail);

    void addTspi(TspiData tspiData);

    void addTspiList(List<TspiData> tspiDataList);

    void addEvent(Event event);

    void addTir(TimedFile tir);

//    void addMedia(MediaFile mediaFile);

//    void addMediaSet(MediaSet mediaSet);

    void addPositionChangeListener(IPositionChangeListener positionChangeListener);

    void removePositionChangeListener(IPositionChangeListener positionChangeListener);

    EntityDetail getEntityDetail();

    EntityDetail getLastEntityDetail();

    EntityDetail getEntityDetailBefore(long timestamp);

    EntityDetail getEntityDetailAfter(long timestamp);

    TspiData getFirstTspi();

    TspiData getCurrentTspi();

    Position getPosition();

    Position getPositionBefore(long timestamp);

    long getLastUpdateTime();

    Event getLastEvent();

    TimedFile getLastTir();

    List<TimedFile> getAllTirs();

    MediaCollection getMediaCollection();
//    boolean hasMedia();

//    List<MediaSet> getMedia();

    boolean isInScope();

    boolean isTimedOut();

    /**
     * Gets the filtered value.  A value of true is to include or display, false is to exclude or hide.
     *
     * @return True if filtered in, included, or displayed, false if filtered out, excluded, or hidden.
     */
    boolean isFiltered();

    boolean isAircraft();

    boolean isLifeForm();

    boolean isMunition();

    boolean isPlatform();

    TrackingAttribute getTrackingAttribute();

    List<Position> getTracks();

    void toggleStatusAnnotation();

    BufferedImage createIcon();

    BufferedImage getIcon();

    IconType getIconType();

    void resetIcon();

    void refreshStatus();

    String getEntityGroupName();

    EntityDisplay getEntityDisplay();

    void setEntityGroup(String entityGroupName, EntityDisplay entityDisplay);

    void removeEntityGroup();

    void setSelected(boolean selected);

    boolean update(long time, EntityFilter entityFilter);

    void dispose();

    boolean supportsRtcaCommands();

    boolean supportsTracking();

    void sendRtcaCommand(RtcaCommand rtcaCommand);

    List<EntityScope> getAllEntityScopes();

    List<EntityDetail> getAllEntityDetails();

    List<TspiData> getAllTspiData();

    List<Event> getAllEvents();
}
