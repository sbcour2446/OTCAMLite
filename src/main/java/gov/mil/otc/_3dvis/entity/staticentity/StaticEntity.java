package gov.mil.otc._3dvis.entity.staticentity;

import gov.mil.otc._3dvis.entity.RtcaCommand;
import gov.mil.otc._3dvis.entity.base.AbstractEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StaticEntity extends AbstractEntity {

    public StaticEntity(EntityId entityId) {
        this(entityId, "/images/static/static_0.png");
    }

    public StaticEntity(EntityId entityId, String imageName) {
        super(entityId);
        entityIcon = initializeImage(imageName);
    }

    public StaticEntity(EntityId entityId, BufferedImage image) {
        super(entityId);
        this.entityIcon = image;
    }

    public void setImage(BufferedImage image) {
        this.entityIcon = image;
    }

    private BufferedImage initializeImage(String imageName) {
        BufferedImage bufferedImage = null;
        try (InputStream inputStream = getClass().getResourceAsStream(imageName)) {
            if (inputStream != null) {
                bufferedImage = ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Could not load image: %s", imageName), e);
        }
        return bufferedImage;
    }

    @Override
    public BufferedImage createIcon() {
        return getIcon();
    }

    @Override
    public boolean supportsRtcaCommands() {
        return false;
    }

    @Override
    public void sendRtcaCommand(RtcaCommand rtcaCommand) {
        //not supported
    }
}
