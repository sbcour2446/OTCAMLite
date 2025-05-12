package gov.mil.otc._3dvis.utility;

import javafx.scene.image.Image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageLoader {

    private static final ImageLoader SINGLETON = new ImageLoader();

    private ImageLoader() {
    }

    public static Image getLogo() {
        return getFxImage("/images/globe.png");
    }

    public static Image getFxImage(String resourcePathToFile) {
        return SINGLETON.doGetFxImage(resourcePathToFile);
    }

    public static BufferedImage getBufferedImage(String resourcePathToFile) {
        return SINGLETON.doGetImage(resourcePathToFile);
    }

    private Image doGetFxImage(String resourcePathToFile) {
        Image image = null;
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePathToFile)) {
            if (inputStream != null) {
                image = new Image(inputStream);
            } else {
                String message = String.format("Image not found: %s", resourcePathToFile);
                Logger.getGlobal().log(Level.WARNING, message);
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Could not load image: %s", resourcePathToFile), e);
        }
        return image;
    }

    private BufferedImage doGetImage(String resourcePathToFile) {
        BufferedImage bufferedImage = null;
        try (InputStream inputStream = getClass().getResourceAsStream(resourcePathToFile)) {
            if (inputStream != null) {
                bufferedImage = ImageIO.read(inputStream);
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, String.format("Could not load image: %s", resourcePathToFile), e);
        }
        return bufferedImage;
    }
}
