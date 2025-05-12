package gov.mil.otc._3dvis.event;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.C2MessageAnnotation;
import gov.mil.otc._3dvis.vmf.VmfMessage;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;

import javax.swing.*;

public class C2MessageEvent extends Event {

    private static final long DISPLAY_TIME = 15000; //15 seconds
    private IEntity entity;
    private final int senderUrn;
    private final String destinationUrns;
    private final String messageType;
    private final String summary;
    private final String message;
    private C2MessageAnnotation c2MessageAnnotation = null;

    public C2MessageEvent(IEntity entity, VmfMessage vmfMessage) {
        super(vmfMessage.getHeader().getOriginatorTime(),
                vmfMessage.getHeader().getOriginatorTime() + DISPLAY_TIME);
        this.entity = entity;
        this.senderUrn = vmfMessage.getHeader().getSenderUrn();

        StringBuilder stringBuilder = new StringBuilder();
        String prefix = "";
        for (int urn : vmfMessage.getHeader().getDestUrns()) {
            stringBuilder.append(prefix);
            stringBuilder.append(urn);
            prefix = ",";
        }

        this.destinationUrns = stringBuilder.toString();
        this.messageType = vmfMessage.getHeader().getMessageType().getName();
        this.summary = vmfMessage.getSummary();
        this.message = vmfMessage.getText();
    }

    public C2MessageEvent(long timestamp, IEntity entity, int senderUrn, String destinationUrns,
                          String messageType, String summary, String message) {
        super(timestamp, timestamp + DISPLAY_TIME);
        this.entity = entity;
        this.senderUrn = senderUrn;
        this.destinationUrns = destinationUrns;
        this.messageType = messageType;
        this.summary = summary;
        this.message = message;
    }

    public C2MessageEvent(long timestamp, int senderUrn, String destinationUrns,
                          String messageType, String summary, String message) {
        super(timestamp, timestamp + DISPLAY_TIME);
        this.senderUrn = senderUrn;
        this.destinationUrns = destinationUrns;
        this.messageType = messageType;
        this.summary = summary;
        this.message = message;
    }

    public IEntity getEntity() {
        return entity;
    }

    public int getSenderUrn() {
        return senderUrn;
    }

    public String getDestinationUrns() {
        return destinationUrns;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getSummary() {
        return summary;
    }

    public String getMessage() {
        return message;
    }

    @Override
    protected void setType() {
        type = "C2 Message";
    }

    @Override
    public String getDescription() {
        return getMessage();
    }

    @Override
    public void update(long time, RenderableLayer layer) {
        boolean isActive = isActive(time);
        if (isActive) {
            Position position = entity.getPosition();
            if (position != null) {
                if (!isVisible) {
                    c2MessageAnnotation = new C2MessageAnnotation(entity, this);
                    layer.addRenderable(c2MessageAnnotation);
                    isVisible = true;
                }
                SwingUtilities.invokeLater(() -> c2MessageAnnotation.setPosition(position));
            }
        } else if (isVisible) {
            if (c2MessageAnnotation != null) {
                layer.removeRenderable(c2MessageAnnotation);
            }
            isVisible = false;
        }
    }

    @Override
    public void dispose(RenderableLayer layer) {
        if (c2MessageAnnotation != null) {
            layer.removeRenderable(c2MessageAnnotation);
        }
    }
}
