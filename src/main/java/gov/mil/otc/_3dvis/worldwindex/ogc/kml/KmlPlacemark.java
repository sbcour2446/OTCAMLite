package gov.mil.otc._3dvis.worldwindex.ogc.kml;

import gov.nasa.worldwind.ogc.kml.KMLAbstractGeometry;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.KMLPolygon;
import gov.nasa.worldwind.ogc.kml.impl.KMLExtrudedPolygonImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLPolygonImpl;
import gov.nasa.worldwind.ogc.kml.impl.KMLRenderable;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;

public class KmlPlacemark extends KMLPlacemark {

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate
     *                     no namespace qualification.
     */
    public KmlPlacemark(String namespaceURI) {
        super(namespaceURI);
    }

    @Override
    protected KMLRenderable selectPolygonRenderable(KMLTraversalContext tc, KMLAbstractGeometry geom) {
        KMLPolygon shape = (KMLPolygon) geom;

        if (shape.getOuterBoundary().getCoordinates() == null) {
            return null;
        }

        if (shape.isExtrude()) {
            return new KMLExtrudedPolygonImpl(tc, this, geom);
        } else {
            return new KMLPolygonImpl(tc, this, geom);
        }
    }
}
