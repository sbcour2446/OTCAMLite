package gov.mil.otc._3dvis.event.otcam;

import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.datamodel.RtcaState;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.RtcaEvent;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.utility.Utility;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;

import java.awt.*;
import java.util.Calendar;

public class OtcamEvent extends RtcaEvent {

    private final EventType eventType;
    private final EventOrigin eventOrigin;
    private final EventFrom eventFrom;
    private final AdminAction adminAction;
    private final EntityId targetId;
    private final EntityId shooterId;
    private final EntityType munition;
    private final String munitionName;
    private final RtcaState rtcaState;
    private EventIndicator eventIndicator;
    private PairingLine pairingLine;

    public OtcamEvent(long eventTime, EntityId eventId, EventType eventType, EventOrigin eventOrigin, EventFrom eventFrom,
                      AdminAction adminAction, EntityId targetId, EntityId shooterId, EntityType munition,
                      String munitionName, RtcaState rtcaState) {
        super(eventTime, eventId);
        this.eventType = eventType;
        this.eventOrigin = eventOrigin;
        this.eventFrom = eventFrom;
        this.adminAction = adminAction;
        this.targetId = targetId;
        this.shooterId = shooterId;
        this.munition = munition;
        this.munitionName = munitionName;
        this.rtcaState = rtcaState;
    }

    public EventType getEventType() {
        return eventType;
    }

    public EventOrigin getEventOrigin() {
        return eventOrigin;
    }

    public EventFrom getEventFrom() {
        return eventFrom;
    }

    public AdminAction getAdminAction() {
        return adminAction;
    }

    public EntityId getTargetId() {
        return targetId;
    }

    public EntityId getShooterId() {
        return shooterId;
    }

    public EntityType getMunition() {
        return munition;
    }

    public String getMunitionDescription() {
        if (munition != null) {
            return munition.getDescription();
        } else {
            return munitionName;
        }
    }

    public String getMunitionName() {
        return munitionName;
    }

    public RtcaState getRtcaState() {
        return rtcaState;
    }

    public String getResults() {
        if (adminAction != null) {
            return adminAction.name();
        }

        return rtcaState.toString();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(switch (eventType) {
            case OTHER -> "Other";
            case ADMIN -> "Admin";
            case EW -> "EW Effect";
            case ENGAGE -> "Engagement";
        });
        stringBuilder.append(DELIMITER);
        stringBuilder.append(switch (eventOrigin) {
            case OTHER -> "Other";
            case ADMIN -> "Admin";
            case RTCA -> "RTCA";
            case USER -> "Platform";
            case CONTROLLER -> "Controller";
        });
        stringBuilder.append(DELIMITER);
        stringBuilder.append(switch (eventFrom) {
            case OTHER -> "Other";
            case DIRECT -> "Direct";
            case INDIRECT -> "Indirect";
            case MINE -> "MINE";
            case NBC -> "NBC";
            case CHEAT -> "Cheat";
            case ADMIN -> "Admin";
        });

        stringBuilder.append(System.lineSeparator());

        if (adminAction != null) {
            stringBuilder.append(String.format("Admin Action: %s", adminAction));
            stringBuilder.append(System.lineSeparator());
        }

        if (targetId != null) {
            stringBuilder.append(String.format("Target: %s", targetId));
            stringBuilder.append(System.lineSeparator());
        }

        if (shooterId != null) {
            stringBuilder.append(String.format("Shooter: %s", shooterId));
            stringBuilder.append(System.lineSeparator());
        }

        if (targetId != null && shooterId != null) {
            IEntity target = EntityManager.getEntity(targetId);
            IEntity shooter = EntityManager.getEntity(shooterId);
            if (target != null && target.getPosition() != null &&
                    shooter != null && shooter.getPosition() != null) {
                double range = Utility.calculateDistance1(shooter.getPosition(), target.getPosition());
                stringBuilder.append(String.format("Range: %.1f", range));
                stringBuilder.append(System.lineSeparator());
            }
        }

        if (munition != null) {
            stringBuilder.append(String.format("Munition: %s", munition.getDescription()));
            stringBuilder.append(System.lineSeparator());
        } else if (!munitionName.isEmpty()) {
            stringBuilder.append(String.format("Munition: %s", munitionName));
            stringBuilder.append(System.lineSeparator());
        }

        if (rtcaState != null) {
            stringBuilder.append(String.format("Results: %s", rtcaState));
            stringBuilder.append(System.lineSeparator());
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getTimestamp());
        stringBuilder.append(String.format("Time: %s", simpleDateFormat.format(calendar.getTime())));

        return stringBuilder.toString();
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    protected void setType() {
        type = "OTCAM Event";
    }

    @Override
    public String getDescription() {
        return toString();
    }

    @Override
    public void update(long time, RenderableLayer layer) {
        boolean isActive = isActive(time);
        if (isActive) {
            if (!isVisible) {
                show(layer);
            } else {
                update();
            }
        } else if (isVisible) {
            hide(layer);
        }
    }

    @Override
    public void dispose(RenderableLayer layer) {
        hide(layer);
    }

    @Override
    public Position getEventLocation() {
        IEntity target = EntityManager.getEntity(targetId);
        return target != null ? target.getPositionBefore(getTimestamp()) : null;
    }

    private void show(RenderableLayer layer) {
        if (eventIndicator == null) {
            if (targetId != null) {
                IEntity target = EntityManager.getEntity(targetId);
                if (target != null && target.getPosition() != null) {
                    eventIndicator = new EventIndicator(target);
                    eventIndicator.setValue(AVKey.ROLLOVER_TEXT, getDescription());

                    if (shooterId != null) {
                        IEntity shooter = EntityManager.getEntity(shooterId);
                        if (shooter != null && shooter.getPosition() != null) {
                            Color color = Color.YELLOW;
                            EntityDetail entityDetail = shooter.getEntityDetail();
                            if (entityDetail != null) {
                                color = SettingsManager.getSettings().getAffiliationColor(entityDetail.getAffiliation());
                            }
                            pairingLine = new PairingLine(target.getPosition(), shooter.getPosition(),
                                    true, color);
                            pairingLine.setValue(AVKey.ROLLOVER_TEXT, getDescription());
                        }
                    }
                }
            }
        }
        if (eventIndicator != null) {
            layer.addRenderable(eventIndicator);
        }
        if (pairingLine != null) {
            layer.addRenderable(pairingLine);
        }
        isVisible = true;
    }

    private void hide(RenderableLayer layer) {
        if (eventIndicator != null) {
            layer.removeRenderable(eventIndicator);
        }
        if (pairingLine != null) {
            layer.removeRenderable(pairingLine);
        }
        isVisible = false;
    }

    private void update() {
        if (eventIndicator != null) {
            IEntity target = EntityManager.getEntity(targetId);
            if (target != null && target.getPosition() != null) {
                eventIndicator.setPosition(target.getPosition());
            }
        }
    }
}
