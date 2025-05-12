package gov.mil.otc._3dvis.worldwindex.ogc.kml;

import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.ogc.kml.KMLParserContext;
import gov.nasa.worldwind.ogc.kml.KMLRoot;
import gov.nasa.worldwind.ogc.kml.io.KMLDoc;
import gov.nasa.worldwind.util.Logging;
import gov.nasa.worldwind.util.WWIO;
import gov.nasa.worldwind.util.xml.XMLEventParserContextFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class KmlRoot extends gov.nasa.worldwind.ogc.kml.KMLRoot {

    public static KmlRoot createAndParse(Object docSource) throws IOException, XMLStreamException {
        KmlRoot kmlRoot = KmlRoot.create(docSource);

        if (kmlRoot == null) {
            String message = Logging.getMessage("generic.UnrecognizedSourceTypeOrUnavailableSource",
                    docSource.toString());
            throw new IllegalArgumentException(message);
        }

        try {
            // Try with a namespace aware parser.
            kmlRoot.parse();
        } catch (XMLStreamException e) {
            // Try without namespace awareness.
            kmlRoot = KmlRoot.create(docSource, false);
            kmlRoot.parse();
        }

        return kmlRoot;
    }

    public static KmlRoot create(Object docSource) throws IOException {
        return create(docSource, true);
    }

    public static KmlRoot create(Object docSource, boolean namespaceAware) throws IOException {
        if (docSource == null) {
            String message = Logging.getMessage("nullValue.DocumentSourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (docSource instanceof File) {
            return new KmlRoot((File) docSource, namespaceAware);
        } else if (docSource instanceof URL) {
            return new KmlRoot((URL) docSource, null, namespaceAware);
        } else if (docSource instanceof InputStream) {
            return new KmlRoot((InputStream) docSource, null, namespaceAware);
        } else if (docSource instanceof String) {
            File file = new File((String) docSource);
            if (file.exists()) {
                return new KmlRoot(file, namespaceAware);
            }

            URL url = WWIO.makeURL(docSource);
            if (url != null) {
                return new KmlRoot(url, null, namespaceAware);
            }
        }

        return null;
    }

    public KmlRoot(KMLDoc docSource) throws IOException {
        super(docSource);
    }

    public KmlRoot(KMLDoc docSource, boolean namespaceAware) throws IOException {
        super(docSource, namespaceAware);
    }

    public KmlRoot(File docSource) throws IOException {
        super(docSource);
    }

    public KmlRoot(File docSource, boolean namespaceAware) throws IOException {
        super(docSource, namespaceAware);
    }

    public KmlRoot(InputStream docSource, String contentType) throws IOException {
        super(docSource, contentType);
    }

    public KmlRoot(InputStream docSource, String contentType, boolean namespaceAware) throws IOException {
        super(docSource, contentType, namespaceAware);
    }

    public KmlRoot(URL docSource, String contentType) throws IOException {
        super(docSource, contentType);
    }

    public KmlRoot(URL docSource, String contentType, boolean namespaceAware) throws IOException {
        super(docSource, contentType, namespaceAware);
    }

    public KmlRoot(String namespaceURI, KMLDoc docSource) throws IOException {
        super(namespaceURI, docSource);
    }

    public KmlRoot(String namespaceURI, KMLDoc docSource, boolean namespaceAware) throws IOException {
        super(namespaceURI, docSource, namespaceAware);
    }

    @Override
    protected KMLParserContext createParserContext(XMLEventReader reader) {
        KMLParserContext ctx = (KMLParserContext) XMLEventParserContextFactory.createParserContext(KMLConstants.KML_MIME_TYPE, this.getNamespaceURI());

        if (!(ctx instanceof KmlParserContext)) {
            // Register a parser context for this root's default namespace
            String[] mimeTypes = new String[]{KMLConstants.KML_MIME_TYPE, KMLConstants.KMZ_MIME_TYPE};
            XMLEventParserContextFactory.prependParserContext(mimeTypes, new KmlParserContext(this.getNamespaceURI()));
            ctx = (KMLParserContext) XMLEventParserContextFactory.createParserContext(KMLConstants.KML_MIME_TYPE, this.getNamespaceURI());
        }

        ctx.setEventReader(reader);

        return ctx;
    }
}
