package gov.mil.otc._3dvis.worldwindex.ogc.kml;

import gov.nasa.worldwind.ogc.kml.KMLParserContext;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;

public class KmlParserContext extends KMLParserContext {

    public KmlParserContext(String defaultNamespace) {
        super(defaultNamespace);
    }

    public KmlParserContext(XMLEventReader eventReader, String defaultNamespace) {
        super(eventReader, defaultNamespace);
    }

    public KmlParserContext(KmlParserContext ctx) {
        super(ctx);
    }

    @Override
    protected void initializeParsers(String ns) {
        super.initializeParsers(ns);
        this.parsers.put(new QName(ns, "Placemark"), new KmlPlacemark(ns));
    }
}
