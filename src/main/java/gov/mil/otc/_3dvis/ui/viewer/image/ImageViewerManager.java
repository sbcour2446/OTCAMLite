package gov.mil.otc._3dvis.ui.viewer.image;

import gov.mil.otc._3dvis.entity.base.IEntity;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageViewerManager {

    public static void show(IEntity entity, Map<Long, File> files) {
        ImageViewer imageViewer = SINGLETON.imageViewerMap.get(entity);
        if (imageViewer == null) {
            imageViewer = new ImageViewer(entity, files);
            SINGLETON.imageViewerMap.put(entity, imageViewer);
        }
    }

    public static File getCurrent(IEntity entity) {
        ImageViewer imageViewer = SINGLETON.imageViewerMap.get(entity);
        if (imageViewer != null) {
            return imageViewer.getCurrentFile();
        } else {
            return null;
        }
    }

    protected static void close(IEntity entity) {
        SINGLETON.imageViewerMap.remove(entity);
    }

    private static final ImageViewerManager SINGLETON = new ImageViewerManager();
    private final Map<IEntity, ImageViewer> imageViewerMap = new HashMap<>();

    private ImageViewerManager() {
    }
}
