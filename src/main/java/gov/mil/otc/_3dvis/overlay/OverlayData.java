package gov.mil.otc._3dvis.overlay;

import gov.mil.otc._3dvis.WWController;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525GraphicFactory;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525PointGraphic;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.CircularFireSupportArea;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.CircularRangeFan;
import gov.nasa.worldwind.symbology.milstd2525.graphics.areas.RectangularFireSupportArea;
import gov.nasa.worldwind.symbology.milstd2525.graphics.lines.Boundary;
import gov.nasa.worldwind.symbology.milstd2525.graphics.lines.MainAttack;
import gov.nasa.worldwind.symbology.milstd2525.graphics.lines.SupportingAttack;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OverlayData {

    private final Overlay overlay;
    public String symbolCode = "";
    public long startTime = 0, endTime = 0;
    public ArrayList<Position> positions = new ArrayList<>();
    public ArrayList<String> textFields = new ArrayList<>();
    public ArrayList<String> additionalInfo = new ArrayList<>();
    public String staffComments;
    TacticalGraphic renderable = null;
    boolean isSupported = false;
    Color color = Overlay.DEFAULT_COLOR;
    double scale = .4;
    double lineSize = 1;
    int dimension = 0, type = 0;
    Overlay parent;

    public OverlayData(Overlay overlay) {
        this.overlay = overlay;
    }

    public TacticalGraphic getRenderable() {
        if (renderable == null && isSupported) {
            renderable = createTacticalGraphic(symbolCode, positions, textFields, color);
        }
        return renderable;
    }

    // use this for the graphic overlays
    public void setSymbolCode(int dim, int type, int subtype, int identity, int status) {
        this.dimension = dim;
        this.type = type;
        overlay.setGraphic(dim > 10 && dim < 17);
        String code = Dimension.get(dim);
        if (code != null) {
            char identities[] = {'P', 'U', 'A', 'F', 'N', 'S', 'H', 'G', 'W', 'M', 'D', 'L', 'J', 'K', 'F', 'F'};
            StringBuilder sb = new StringBuilder();
            sb.append(code.charAt(0));
            sb.append((code.charAt(1) == '-' ? identities[identity] : code.charAt(1)));
            sb.append(code.charAt(2));
            sb.append(((status == 0) ? 'P' : 'A'));
            sb.append(IconMap.get(String.format("%02d-%02d-%02d", dim, type, subtype)));
            sb.append("----X");
            symbolCode = sb.length() == 15 ? sb.toString() : "";
            if (dim == 12 && type == 7) {
                lineSize = 3; // increase the action point line size
            }
            setIsSupported();
        }
    }

    public void setIsSupported() {
        isSupported = new MilStd2525GraphicFactory().isSupported(symbolCode);
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public TacticalGraphic createTacticalGraphic(String symbolCode, ArrayList<Position> positions, List<String> name, Color color) {
        TacticalGraphic graphic;
        try {
            TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
            graphic = factory.createGraphic(symbolCode, positions, null);
            if (graphic != null) {
                graphic.setValue(Overlay.OVERLAY_ID, overlay.getId());
                graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name);
                if (!name.isEmpty()) {
                    graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name.get(0));
                    if (name.size() == 1) {
                        graphic.setValue(AVKey.ROLLOVER_TEXT, graphic.getText());
                    } else {
//                            graphic.setValue(AVKey.ROLLOVER_TEXT, symbolCode);
                    }
                } else {
//                        graphic.setValue(AVKey.ROLLOVER_TEXT, symbolCode);
                }
                addJbcpModifiers(graphic);
                if (graphic.getValue(AVKey.ROLLOVER_TEXT) == null) {
                    graphic.setValue(AVKey.ROLLOVER_TEXT, symbolCode);
                }
//            // Create a custom attributes bundle. Any fields set in this bundle will override the default attributes.
                TacticalGraphicAttributes attrs = new BasicTacticalGraphicAttributes();
                if (color != null) {
                    attrs.setOutlineMaterial(new Material(color));
                }
                attrs.setScale(scale);
                attrs.setOutlineWidth(lineSize);
                if (positions.size() == 1) {
                    attrs.setScale(scale);
                }
                graphic.setAttributes(attrs);
                TacticalGraphicAttributes highlights = attrs.copy();
                highlights.setOutlineMaterial(Material.WHITE);
                highlights.setInteriorMaterial(Material.WHITE);
                graphic.setHighlightAttributes(highlights);
            } else {
                System.out.println(symbolCode + " **** Unsupported");
            }
        } catch (Exception e) {
            graphic = null;
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return graphic;
    }

    private void addJbcpModifiers(TacticalGraphic graphic) {
        String rolloverText = (String) graphic.getValue(AVKey.ROLLOVER_TEXT);
        if (graphic instanceof CircularRangeFan) {
            // assume a center and a point on each ring
            if (positions.size() > 1) {
                double earthRadius = WWController.getView().getGlobe().getRadius();
                Position center = positions.get(0);
                ((CircularRangeFan) graphic).setPositions(positions);
                ArrayList<Double> radii = new ArrayList<>();
                for (int i = 1; i < positions.size(); i++) {
                    radii.add((double) ((int) (Position.greatCircleDistance(center, positions.get(i)).radians * earthRadius)));
                }
                ((CircularRangeFan) graphic).setRadii(radii);
                if (!textFields.isEmpty()) {
                    ((CircularRangeFan) graphic).setModifier(SymbologyConstants.UNIQUE_DESIGNATION, textFields.get(0));
                    graphic.setValue(AVKey.ROLLOVER_TEXT, textFields.get(0));
                }
            }

        } else if (graphic instanceof CircularFireSupportArea) {
            // apparently the radius is sent in the staff comments field
            double radius = Double.parseDouble(staffComments);
            ((CircularFireSupportArea) graphic).setPositions(positions);
            ((CircularFireSupportArea) graphic).setRadius(radius);

        } else if (graphic instanceof MilStd2525PointGraphic && rolloverText == null) {
            // if no name, use the symbol code name
            if (this.symbolCode.startsWith("PTS", 4)) {
                graphic.setValue(AVKey.ROLLOVER_TEXT, "Point/Single Target");
            } else if (this.symbolCode.startsWith("GPPK", 4)) {
                graphic.setValue(AVKey.ROLLOVER_TEXT, "CHECK POINT");
            } else if (this.symbolCode.startsWith("GPPR", 4)) {
                graphic.setValue(AVKey.ROLLOVER_TEXT, "RALLY POINT");
            } else if (this.symbolCode.startsWith("GPPS", 4)) {
                graphic.setValue(AVKey.ROLLOVER_TEXT, "START POINT");
            } else if (this.symbolCode.startsWith("GPPE", 4)) {
                graphic.setValue(AVKey.ROLLOVER_TEXT, "RELEASE POINT");
            } else if (this.symbolCode.startsWith("GPO-", 4)) {
                graphic.setValue(AVKey.ROLLOVER_TEXT, "ROUTE");
            }
        } else if (graphic instanceof RectangularFireSupportArea) {
            // width set in the staff comments field?
            double width = Double.parseDouble(staffComments);
            ((RectangularFireSupportArea) graphic).setWidth(width);
        } else if (graphic instanceof Boundary && rolloverText == null) {
            graphic.setValue(AVKey.ROLLOVER_TEXT, "Boundary");
        } else if (graphic instanceof SupportingAttack && rolloverText == null) {
            graphic.setValue(AVKey.ROLLOVER_TEXT, "Supporting Attack");
        } else if (graphic instanceof MainAttack && rolloverText == null) {
            graphic.setValue(AVKey.ROLLOVER_TEXT, "Main Attack");
        }
    }
}
