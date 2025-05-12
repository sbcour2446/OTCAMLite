package gov.mil.otc._3dvis.ui.tools.eventtable;

import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.utility.Utility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class C2MessageEventView extends EventView {

    private final C2MessageEvent c2MessageEvent;

    public C2MessageEventView(C2MessageEvent c2MessageEvent) {
        super(c2MessageEvent);
        this.c2MessageEvent = c2MessageEvent;
    }

    public String getOriginatorTime() {
        return Utility.formatTime(c2MessageEvent.getTimestamp());
    }

    public String getSenderUrn() {
        return String.valueOf(c2MessageEvent.getSenderUrn());
    }

    public String getSenderName() {
        IEntity entity = c2MessageEvent.getEntity();
        if (entity != null) {
            if (entity.getEntityDetail() != null) {
                return entity.getEntityDetail().getName();
            } else {
                return entity.getEntityId().toString();
            }
        }
        return "";
    }

    public String getDestinationUrnList() {
        return c2MessageEvent.getDestinationUrns();
    }

    public String getDestinationNames() {
        StringBuilder stringBuilder = new StringBuilder();
        String[] urns = c2MessageEvent.getDestinationUrns().split(",");
        String prefix = "";
        for (String urnString : urns) {
            stringBuilder.append(prefix);
            prefix = ",";

            int urn = 0;
            try {
                urn = Integer.parseInt(urnString);
            } catch (Exception e) {
                Logger.getGlobal().log(Level.INFO, null, e);
            }
            IEntity entity = EntityManager.getEntityFromUrn(urn);
            if (entity != null) {
                if (entity.getEntityDetail() != null) {
                    stringBuilder.append(entity.getEntityDetail().getName());
                } else {
                    stringBuilder.append(entity.getEntityId().toString());
                }
            }
        }
        return stringBuilder.toString();
    }

    public String getMessageType() {
        return c2MessageEvent.getMessageType();
    }

    public String getSummary() {
        return c2MessageEvent.getSummary();
    }

    public String getMessage() {
        return c2MessageEvent.getMessage();
    }
}
