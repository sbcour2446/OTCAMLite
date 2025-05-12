package gov.mil.otc._3dvis.worldwindex.layers.earth;

import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.BufferedImage;

public class BMNGOneImage extends RenderableLayer {

    protected static final String IMAGE = "/images/BMNG_world.topo.bathy.200405.3.2048x1024.jpg";

    public BMNGOneImage() {
        BufferedImage bufferedImage = ImageLoader.getBufferedImage(IMAGE);
        setName(Logging.getMessage("layers.Earth.BlueMarbleOneImageLayer.Name"));
        addRenderable(new SurfaceImage(bufferedImage, Sector.FULL_SPHERE));

        // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
        this.setPickEnabled(false);
    }

    @Override
    public String toString() {
        return Logging.getMessage("layers.Earth.BlueMarbleOneImageLayer.Name");
    }
}
