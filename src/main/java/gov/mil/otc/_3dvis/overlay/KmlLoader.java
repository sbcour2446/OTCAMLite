package gov.mil.otc._3dvis.overlay;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.impl.KMLController;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KmlLoader {

    protected static RenderableLayer getRenderable(File file) {
        try {
//            KmlRoot kmlRoot = KmlRoot.createAndParse(file);
            KMLRoot kmlRoot = KMLRoot.createAndParse(file);

            // Set the document's display name
            kmlRoot.setField(AVKey.DISPLAY_NAME, getName(file, kmlRoot));

            KMLAbstractFeature feature = kmlRoot.getFeature();
            if (feature != null) {
                feature.setField("tessellate", true);
            }

            // Create a KMLController to adapt the KMLRoot to the WorldWind renderable interface.
            KMLController kmlController = new KMLController(kmlRoot);

            // Adds a new layer containing the KMLRoot to the end of the WorldWindow's layer list. This
            // retrieves the layer name from the KMLRoot's DISPLAY_NAME field.
            RenderableLayer layer = new RenderableLayer();
            layer.setName((String) kmlRoot.getField(AVKey.DISPLAY_NAME));
            layer.addRenderable(kmlController);
            return layer;
        } catch (IOException | XMLStreamException e) {
            if (Logger.getGlobal().isLoggable(Level.WARNING)) {
                String message = "Error adding KML file " + file.getAbsolutePath();
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        return null;
    }

    private static String getName(Object kmlSource, KMLRoot kmlRoot) {
        KMLAbstractFeature rootFeature = kmlRoot.getFeature();

        if (rootFeature != null && !WWUtil.isEmpty(rootFeature.getName())) {
            return rootFeature.getName();
        }

        if (kmlSource instanceof File) {
            return ((File) kmlSource).getName();
        }

        if (kmlSource instanceof URL) {
            return ((URL) kmlSource).getPath();
        }

        if (kmlSource instanceof String) {
            URL url = WWIO.makeURL((String) kmlSource);
            if (url != null) {
                return url.getPath();
            }
        }

        return "KML Layer";
    }
}
