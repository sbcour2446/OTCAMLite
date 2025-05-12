package gov.mil.otc._3dvis.overlay;

import gov.mil.otc._3dvis.worldwindex.symbology.milstd2525.MilStd2525GraphicFactoryEx;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.symbology.*;
import gov.nasa.worldwind.symbology.milstd2525.MilStd2525TacticalSymbol;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class OvlLoader {

    protected static RenderableLayer processOvl(File file) {
        Document document = getDocument(file);
        if (document != null) {
            return processDocument(document);
        }
        return null;
    }

    private static Document getDocument(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(file);
        } catch (ParserConfigurationException | SAXException | IOException | DOMException ex) {
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
        return null;
    }

    private static RenderableLayer processDocument(Document document) {
        List<Renderable> renderables = new ArrayList<>();
        NodeList nodes = document.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (!(node instanceof Element)) {
                continue;
            }
            if (node.getNodeName().equalsIgnoreCase("MODEL")) {
                renderables.addAll(processModelNode(node));
            } else if (node.getNodeName().equalsIgnoreCase("tacticalGraphics")) {
                renderables.addAll(processTacticalGraphicsNode(node));
            } else {
                Logger.getGlobal().log(Level.WARNING, "OvlLoader::processDocument::not processed node:" + node.getNodeName());
            }
        }

        if (renderables.isEmpty()) {
            return null;
        }

        RenderableLayer renderableLayer = new RenderableLayer();
        renderableLayer.addRenderables(renderables);
        return renderableLayer;
    }

    private static List<Renderable> processModelNode(Node node) {
        List<Renderable> renderables = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (!(childNode instanceof Element)) {
                continue;
            }
            NodeList milbobjectNodes = childNode.getChildNodes();
            String milId = "";
            String name = "";
            String lineColor = "";
            String lineWidth = "";
            String size = "";
            List<String> title = new ArrayList<>();
            String positionString = "";
            String usasGraphicId = "";
            List<Position> positions = new ArrayList<>();
            Position center = Position.ZERO;
            double radius = 0;
            double semiMajor = 0;
            double semiMinor = 0;
            double bearing = 0;
            for (int k = 0; k < milbobjectNodes.getLength(); k++) {
                Node milbobjectNode = milbobjectNodes.item(k);
                if (milbobjectNode instanceof Element) {
                    switch (milbobjectNode.getNodeName()) {
                        case "MIL_ID":
                            milId = milbobjectNode.getTextContent();
                            break;
                        case "NAME":
                            name = milbobjectNode.getTextContent();
                            break;
                        case "LINE_COLOR":
                            lineColor = milbobjectNode.getTextContent();
                            break;
                        case "LINE_WIDTH":
                            lineWidth = milbobjectNode.getTextContent();
                            break;
                        case "SIZE":
                            size = milbobjectNode.getTextContent();
                            break;
                        case "RADIUS": // TODO what is the unit of measure?
                            radius = Double.parseDouble(milbobjectNode.getTextContent());
                            break;
                        case "BEARING": // TODO what is the unit of measure?
                            bearing = Double.parseDouble(milbobjectNode.getTextContent());
                            break;
                        case "SEMI_MAJOR": // TODO what is the unit of measure?
                            semiMajor = Double.parseDouble(milbobjectNode.getTextContent());
                            break;
                        case "SEMI_MINOR": // TODO what is the unit of measure?
                            semiMinor = Double.parseDouble(milbobjectNode.getTextContent());
                            break;
                        case "T", "T1":
                            title.add(milbobjectNode.getTextContent());
                            break;
                        case "CENTER": {
                            positionString = milbobjectNode.getTextContent();
                            String[] position = positionString.split(" ");
                            double lat = Double.parseDouble(position[0]);
                            double lon = Double.parseDouble(position[1]);
                            center = Position.fromDegrees(lat, lon);
                        }
                        break;
                        case "POSITION":
                            positionString = milbobjectNode.getTextContent();
                            String[] position = positionString.split(" ");
                            double lat = Double.parseDouble(position[0]);
                            double lon = Double.parseDouble(position[1]);
                            positions.add(Position.fromDegrees(lat, lon));
                            break;
                        case "USAS_GRAPHIC_ID":
                            usasGraphicId = milbobjectNode.getTextContent();
                            break;
                        default:
                            break;
                    }
                }
            }

            if (childNode.getNodeName().equalsIgnoreCase("milbobject")) {
                TacticalGraphic g = null;
                if (milId.equalsIgnoreCase("GFGPSAN-------X")) {
                    if (title.size() > 0) {
                        title.set(0, "sai");
                    } else {
                        title.add("sai");
                    }
                }
                g = createTacticalGraphic(milId, positions, title);

                if (g != null) {
                    TacticalGraphicAttributes attrs = new BasicTacticalGraphicAttributes();
                    TacticalGraphicAttributes attrsHighlight = new BasicTacticalGraphicAttributes();

                    Color color = new Color(0, 0, 0);

                    if (!lineColor.isEmpty()) {
                        String[] colorString = lineColor.split("[, ]");
                        int red = Integer.parseInt(colorString[0]);
                        int green = Integer.parseInt(colorString[1]);
                        int blue = Integer.parseInt(colorString[2]);
                        color = new Color(red, green, blue);
                    }

                    if (!lineWidth.isEmpty()) {
                        double width = Double.parseDouble(lineWidth);
                        attrs.setOutlineWidth(width);
                        attrsHighlight.setOutlineWidth(width);
                    }

                    attrs.setScale(.5);
                    attrs.setInteriorOpacity(.0);
                    attrs.setOutlineOpacity(.75);
//                                    attrs.setInteriorMaterial(new Material(Color.));
                    attrs.setOutlineMaterial(new Material(color));
                    attrs.setTextModifierMaterial(Material.WHITE);

                    attrsHighlight.setScale(.5);
                    attrsHighlight.setInteriorOpacity(1.0);
                    attrsHighlight.setOutlineOpacity(1.0);
                    attrsHighlight.setInteriorMaterial(Material.WHITE);
                    attrsHighlight.setOutlineMaterial(Material.WHITE);
                    attrsHighlight.setTextModifierMaterial(Material.WHITE);

                    g.setAttributes(attrs);
                    g.setHighlightAttributes(attrsHighlight);
                    g.setText(title.size() > 0 ? title.get(0) : "");
                    g.setShowTextModifiers(true);
                    g.setValue(AVKey.DISPLAY_NAME, title.size() > 0 ? title.get(0) : ""); // Tool tip text.
                    renderables.add(g);
                }
            } else if (childNode.getNodeName().equalsIgnoreCase("circle")) {
                SurfaceCircle circle = new SurfaceCircle(center, radius * 1000);
                BasicShapeAttributes attrs = new BasicShapeAttributes();

                Color color = new Color(0, 0, 0);

                if (!lineColor.isEmpty()) {
                    String[] colorString = lineColor.split("[, ]");
                    int red = Integer.parseInt(colorString[0]);
                    int green = Integer.parseInt(colorString[1]);
                    int blue = Integer.parseInt(colorString[2]);
                    color = new Color(red, green, blue);
                }
                if (!lineWidth.isEmpty()) {
                    double width = Double.parseDouble(lineWidth);
                    attrs.setOutlineWidth(width);
                }

                attrs.setInteriorOpacity(.15);
                attrs.setOutlineOpacity(.75);
                attrs.setInteriorMaterial(new Material(color));
                attrs.setOutlineMaterial(new Material(color));

                circle.setAttributes(attrs);
                circle.setHighlightAttributes(attrs);

                circle.setValue(AVKey.DISPLAY_NAME, name); // Tool tip text.
                renderables.add(circle);
            } else if (childNode.getNodeName().equalsIgnoreCase("ellipse")) {
                SurfaceEllipse ellipse = new SurfaceEllipse(center, semiMajor * 2000, semiMinor * 2000, Angle.fromDegrees(bearing + 90));
                BasicShapeAttributes attrs = new BasicShapeAttributes();

                Color color = new Color(0, 0, 0);

                if (!lineColor.isEmpty()) {
                    String[] colorString = lineColor.split("[, ]");
                    int red = Integer.parseInt(colorString[0]);
                    int green = Integer.parseInt(colorString[1]);
                    int blue = Integer.parseInt(colorString[2]);
                    color = new Color(red, green, blue);
                }

                if (!lineWidth.isEmpty()) {
                    double width = Double.parseDouble(lineWidth);
                    attrs.setOutlineWidth(width);
                }

                attrs.setInteriorOpacity(.15);
                attrs.setOutlineOpacity(.75);
                attrs.setInteriorMaterial(new Material(color));
                attrs.setOutlineMaterial(new Material(color));

                ellipse.setAttributes(attrs);
                ellipse.setHighlightAttributes(attrs);

                ellipse.setValue(AVKey.DISPLAY_NAME, name); // Tool tip text.
                renderables.add(ellipse);
            } else if (childNode.getNodeName().equalsIgnoreCase("polygon")) {

                SurfacePolyline polygon = new SurfacePolyline(positions);
                BasicShapeAttributes attrs = new BasicShapeAttributes();

                Color color = new Color(0, 0, 0);

                if (!lineColor.isEmpty()) {
                    String[] colorString = lineColor.split("[, ]");
                    int red = Integer.parseInt(colorString[0]);
                    int green = Integer.parseInt(colorString[1]);
                    int blue = Integer.parseInt(colorString[2]);
                    color = new Color(red, green, blue);
                }

                if (!lineWidth.isEmpty()) {
                    double width = Double.parseDouble(lineWidth);
                    attrs.setOutlineWidth(width);
                }

                attrs.setInteriorOpacity(.75);
                attrs.setOutlineOpacity(.75);
                attrs.setInteriorMaterial(new Material(color));
                attrs.setOutlineMaterial(new Material(color));

                polygon.setAttributes(attrs);
                polygon.setHighlightAttributes(attrs);

                polygon.setValue(AVKey.DISPLAY_NAME, name); // Tool tip text.
                renderables.add(polygon);
            } else if (childNode.getNodeName().equalsIgnoreCase("tacsymbol")) {
                TacticalSymbol s = createTacticalSymbol(milId, positions.get(0), name);
                if (s != null) {
                    renderables.add(s);
                }
            } else {
                Logger.getGlobal().log(Level.WARNING, "OvlLoader::processModelNode::not processed node:" + childNode.getNodeName());
            }
        }
        return renderables;
    }

    private static List<Renderable> processTacticalGraphicsNode(Node node) {
        List<Renderable> renderables = new ArrayList<>();
        try {
            NodeList childNodes = node.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                if (childNode != null && childNode.getNodeName().equalsIgnoreCase("graphic")) {
                    String symbolId = String.format("Symbol %02d", i);
                    Renderable renderable = processGraphicNode(childNode, symbolId);
                    if (renderable != null) {
                        renderables.add(renderable);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "OvlLoader::processTacticalGraphicsNode", e);
        }
        return renderables;
    }

    private static Renderable processGraphicNode(Node node, String name) {
        try {
            NodeList childNodes = node.getChildNodes();
            String milStd2525Id = "";
            Position position = Position.ZERO;
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node childNode = childNodes.item(i);
                // just use the first point
                if (childNode.getNodeName().equals("milStd2525Id")) {
                    milStd2525Id = childNode.getTextContent();
                } else if (childNode.getNodeName().equals("points")) {
                    Node lat = childNode.getFirstChild().getFirstChild();
                    Node lon = childNode.getFirstChild().getLastChild();
                    position = Position.fromDegrees(
                            Double.parseDouble(lat.getTextContent()),
                            Double.parseDouble(lon.getTextContent()));
                }
            }
            if (milStd2525Id.length() > 1 && position != Position.ZERO) {
                TacticalSymbol tacticalSymbol = createTacticalSymbol(milStd2525Id, position, name);
                if (tacticalSymbol != null) {
                    tacticalSymbol.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                    tacticalSymbol.getAttributes().setOpacity(.8);
                    ((MilStd2525TacticalSymbol) tacticalSymbol).setShowFrame(true);
                    ((MilStd2525TacticalSymbol) tacticalSymbol).setShowFill(true);
                    return tacticalSymbol;
                }
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "OvlLoader::processGraphicNode", e);
        }
        return null;
    }

    private static TacticalGraphic createTacticalGraphic(String symbolCode, List<Position> positions, List<String> name) {
        return createTacticalGraphic(symbolCode, positions, name, null);
    }

    private static TacticalGraphic createTacticalGraphic(String symbolCode, List<Position> positions, List<String> name, Color color) {
        TacticalGraphic graphic;
        try {
            TacticalGraphicFactory factory = new MilStd2525GraphicFactoryEx();
            graphic = factory.createGraphic(symbolCode, positions, null);
            if (graphic != null) {
                graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name);
//            graphic.setValue(AVKey.DISPLAY_NAME, name); // Tool tip text.
//            // Create a custom attributes bundle. Any fields set in this bundle will override the default attributes.
//            TacticalGraphicAttributes attrs = new BasicTacticalGraphicAttributes();
//            if (color != null) {
//                attrs.setOutlineMaterial(new Material(color));
//            }
//            graphic.setAttributes(attrs);
            }
        } catch (Exception e) {
            graphic = null;
            Logger.getGlobal().log(Level.WARNING, "OvlLoader::createTacticalGraphic", e);
        }
        return graphic;
    }

    private static TacticalSymbol createTacticalSymbol(String symbolCode, Position position, String name) {
        TacticalSymbol symbol;
        try {
            symbol = new MilStd2525TacticalSymbol(symbolCode, position);
            symbol.setValue(AVKey.DISPLAY_NAME, name); // Tool tip text.
            symbol.setModifier(name, name);
            symbol.setShowLocation(false);
            TacticalSymbolAttributes attrs = new BasicTacticalSymbolAttributes();
            TacticalSymbolAttributes attrsHighlight = new BasicTacticalSymbolAttributes();
            attrs.setScale(.25);
            attrsHighlight.setScale(.25);
            attrs.setOpacity(.5);
            attrsHighlight.setOpacity(1.0);
            symbol.setAttributes(attrs);
            symbol.setHighlightAttributes(attrsHighlight);
        } catch (Exception ex) {
            symbol = null;
            Logger.getGlobal().log(Level.WARNING, null, ex);
        }
        return symbol;
    }

    private OvlLoader() {
    }
}
