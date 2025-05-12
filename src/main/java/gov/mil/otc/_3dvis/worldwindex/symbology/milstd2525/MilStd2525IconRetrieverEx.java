package gov.mil.otc._3dvis.worldwindex.symbology.milstd2525;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.symbology.SymbologyConstants;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525IconRetriever;
import gov.nasa.worldwind.symbology.milstd2525.SymbolCode;
import gov.nasa.worldwind.util.Logging;

import java.awt.image.BufferedImage;

public class MilStd2525IconRetrieverEx extends MilStd2525IconRetriever {

    static {
        schemePathMap.put("g", "tacgrp"); // Scheme Tactical Graphics
    }

    /**
     * Create a new retriever that will retrieve icons from the specified
     * location. The retrieval path may be a file URL to a directory on the
     * local file system (for example, file:///symbols/mil-std-2525). A URL to a
     * network resource (http://myserver.com/milstd2525/), or a URL to a JAR or
     * ZIP file (jar:file:milstd2525-symbols.zip!).
     *
     * @param retrieverPath File path or URL to the symbol directory, for
     *                      example "http://myserver.com/milstd2525/".
     */
    public MilStd2525IconRetrieverEx(String retrieverPath) {
        super(retrieverPath);
    }


    /**
     * Create an icon for a MIL-STD-2525C symbol. By default the symbol will
     * include a filled frame and an icon. The fill, frame, and icon can be
     * turned off by setting retrieval parameters. If both frame and icon are
     * turned off then this method will return an image containing a circle.
     *
     * @param sidc   SIDC identifier for the symbol.
     * @param params Parameters that affect icon retrieval. See
     *               <a href="#parameters">Parameters</a> in class documentation.
     * @return An BufferedImage containing the icon for the requested symbol, or
     * null if the icon cannot be retrieved.
     */
    @Override
    public BufferedImage createIcon(String sidc, AVList params) {
        if (sidc == null) {
            String msg = Logging.getMessage("nullValue.SymbolCodeIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        SymbolCode symbolCode = new SymbolCode(sidc);
        BufferedImage image = null;

        boolean mustDrawFill = this.mustDrawFill(symbolCode, params) && !symbolCode.getScheme().equals(SymbologyConstants.SCHEME_TACTICAL_GRAPHICS);  // by GaN for OTC (3DVis)
        boolean mustDrawIcon = this.mustDrawIcon(symbolCode, params);
        boolean mustDrawFrame = this.mustDrawFrame(symbolCode, params) && !symbolCode.getScheme().equals(SymbologyConstants.SCHEME_TACTICAL_GRAPHICS);  // by GaN for OTC (3DVis)

        if (mustDrawFrame || mustDrawIcon) {
            if (mustDrawFill && mustDrawFrame) {
                image = this.drawFill(symbolCode, params, null);
            }

            if (mustDrawFrame) {
                image = this.drawFrame(symbolCode, params, image);
            }

            if (mustDrawIcon) {
                image = this.drawIcon(symbolCode, params, image);
            }
        }

        // Draw a dot if both frame and icon are turned off
        if (image == null) {
            image = this.drawCircle(symbolCode, params, image);
        }

        return image;
    }

    @Override
    protected String getMaskedIconCode(SymbolCode symbolCode, AVList params) {
        String si = this.getSimpleStandardIdentity(symbolCode); // Either Unknown, Friend, Neutral, or Hostile.
        String status = this.getSimpleStatus(symbolCode); // Either Present or Anticipated.

        if (this.mustDrawFrame(symbolCode, params)) {
            status = SymbologyConstants.STATUS_PRESENT;
        }

        SymbolCode maskedCode = new SymbolCode(symbolCode.toString());
        if (symbolCode.getScheme().equals(SymbologyConstants.SCHEME_TACTICAL_GRAPHICS)) {  // added by GaN for OTC (3DVis)
            maskedCode.setStandardIdentity(null);
        } else {
            maskedCode.setStandardIdentity(si);
        }
        maskedCode.setStatus(status);
        maskedCode.setSymbolModifier(null); // Ignore the Symbol Modifier field.
        maskedCode.setCountryCode(null); // Ignore the Country Code field.
        maskedCode.setOrderOfBattle(null); // Ignore the Order of Battle field.

        return maskedCode.toString();
    }
}
