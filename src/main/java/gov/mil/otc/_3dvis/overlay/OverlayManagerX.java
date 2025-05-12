package gov.mil.otc._3dvis.overlay;

import java.util.HashMap;
import java.util.Map;

public class OverlayManagerX {
//
//    public static Color defaultColor = Color.BLACK;
//    public static final String OVERLAY_ID = "3DVis_Overlay_ID";
//
//    private static RenderableLayer renderableLayer = null;
    private static final Map<String, Overlay> overlayMap = new HashMap<>();
//    private static final Map<Integer, String> dimentionMap = new HashMap<>();
//    private static final Map<String, String> iconMap = new HashMap<>();
//
//    public static Overlay createOverlay(String overlayId, int urn) {
//        return createOverlay(overlayId, urn, true);
//    }
//
//    public static Overlay createOverlay(String overlayId, int urn, Boolean isActive) {
//        Overlay overlay = new Overlay(overlayId, urn);
//        if (isActive) {
//            add(overlay);
//        }
//        return overlay;
//    }
//
    public static Overlay getOverlay(String id) {
        return overlayMap.get(id);
    }
//
//    public static void removeAll() {
//        synchronized (overlayMap) {
//            for (Overlay o : overlayMap.values()) {
//                o.showRenderable(null, renderableLayer);
//            }
//            overlayMap.clear();
//        }
//    }
//
//    public static void remove(Overlay o) {
//        o.showRenderable(null, renderableLayer);
//        synchronized (overlayMap) {
//            overlayMap.remove(o.id);
//        }
//    }
//
//    // The Overlay class was designed to be time sensitive.  However MERT suppport
//    // requires simply the static display of the newest version of each overlay
//    // If a time sensitive display is needed in the future, use the update function.
//    public static void doUpdate(long time) {
//        synchronized (overlayMap) {
//            for (Overlay o : overlayMap.values()) {
////                o.update(time, renderableLayer);
//                o.show(renderableLayer);
//            }
//        }
//    }
//
    public static void endGroup(String groupId, long timeInMillis) {
//        synchronized (overlayMap) {
//            for (Map.Entry<String, Overlay> e : overlayMap.entrySet()) {
//                if (e.getKey().startsWith(groupId)) {
//                    e.getValue().setEndTime(timeInMillis);
////                e.getValue().displayTimes();
//                }
//            }
//        }
    }
//
//    public static ArrayList<Overlay> getAllVisible() {
//        ArrayList<Overlay> visibleList = new ArrayList<>();
//        synchronized (overlayMap) {
//            for (Overlay o : overlayMap.values()) {
//                if (o.isVisible()) {
//                    visibleList.add(o);
//                }
//            }
//        }
//        return visibleList;
//    }
//
//    static void add(Overlay overlay) {
//        synchronized (overlayMap) {
//            overlayMap.put(overlay.id, overlay);
//        }
//    }
//
//    public static ArrayList<Overlay> getAll() {
//        ArrayList<Overlay> list = new ArrayList<>();
//        synchronized (overlayMap) {
//            for (Overlay o : overlayMap.values()) {
//                list.add(o);
//            }
//        }
//        return list;
//    }
//
//    public String id;
//    protected ArrayList<OverlayData> history = new ArrayList<>();
//    public boolean isGraphic = false;
//    long currentTime;
//    int urn;
//    private TacticalGraphic shownRenderable = null;
//
//    protected Overlay() {
//        if (renderableLayer == null) {
//            renderableLayer = ((OperationalGraphicsLayer) Main.getController().getRegisteredObject(Constants.FEATURE_OPERATIONAL_GRAPHICS_LAYER)).getRenderableLayer();
//        }
//    }
//
//    private Overlay(String overlayId, int urn) {
//        if (renderableLayer == null) {
//            renderableLayer = ((OperationalGraphicsLayer) Main.getController().getRegisteredObject(Constants.FEATURE_OPERATIONAL_GRAPHICS_LAYER)).getRenderableLayer();
//        }
//        id = overlayId;
//        this.urn = urn;
//    }
//
//    public void addNewData(OverlayData data) {
//        synchronized (this) {
//            if (history.isEmpty()) {
//                history.add(data);
//            } else if (data.startTime < history.get(0).startTime) {
//                data.endTime = min(history.get(0).startTime - 1, data.endTime);// set end 1 milli less than the next start time
//                history.add(0, data);
//            } else {
//                for (int i = history.size() - 1; i >= 0; i--) {
//                    if (data.startTime > history.get(i).startTime) {
//                        history.get(i).endTime = min(history.get(i).startTime - 1, data.endTime);
//                        history.add(i + 1, data);
//                    }
//                }
//            }
//        }
//    }
//
//    long getEndTime() {
//        return history.isEmpty() ? 0 : history.get(history.size() - 1).endTime;
//    }
//
//    public void setEndTime(long timeInMillis) {
//        if (!history.isEmpty()) {
//            history.get(history.size() - 1).endTime = timeInMillis;
//        }
//    }
//
//    private void displayTimes() {
//        for (int i = 0; i < history.size(); i++) {
//            Calendar dtgStart = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//            Calendar dtgEnd = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
//            dtgStart.setTimeInMillis(history.get(i).startTime);
//            dtgEnd.setTimeInMillis(history.get(i).endTime);
//            System.out.printf("%s (%d)  %s - %s%s", id, i, Main.SDF_LONG.format(dtgStart.getTime()), Main.SDF_LONG.format(dtgEnd.getTime()), System.lineSeparator());
//        }
//    }
//
//    void showRenderable(TacticalGraphic renderable, RenderableLayer layer) {
//        if (renderable != shownRenderable) {
//            if (shownRenderable != null) {
//                layer.removeRenderable(shownRenderable);
//                shownRenderable = null;
//            }
//            if (renderable != null) {
//                layer.addRenderable(renderable);
//            }
//            shownRenderable = renderable;
//        }
//    }
//
//    public void update(long time, RenderableLayer layer) {
//        if (time != currentTime) {
//            TacticalGraphic r = shownRenderable;
//            if (!history.isEmpty() && (time >= history.get(0).startTime) && (time <= history.get(history.size() - 1).endTime)) {
//                for (int i = 0; i < history.size(); i++) {
//                    if (time >= history.get(i).startTime && time <= history.get(i).endTime) {
//                        r = history.get(i).getRenderable();
//                        break;
//                    }
//                }
//            } else {
//                displayTimes();
//            }
//            showRenderable(r, layer);
//            currentTime = time;
//        }
//    }
//
//    public void show(RenderableLayer layer) {
//        TacticalGraphic renderable = history.get(history.size() - 1).getRenderable();
//        showRenderable(renderable, layer);
//    }
//
//    // future enhancements needed
//    private boolean isVisible() {
//        return shownRenderable != null && shownRenderable.isVisible();
//    }
//
//    public void setVisible(boolean b) {
//        if (shownRenderable != null) {
//            shownRenderable.setVisible(b);
//        }
//    }
//
//    public void setLineSize(int newSize) {
//        synchronized (this) {
//            history.forEach(od -> {
//                od.lineSize = newSize;
//                TacticalGraphic tg = od.getRenderable();
//                if (tg != null) {
//                    tg.getAttributes().setOutlineWidth((double) newSize);
//                }
//            });
//        }
//    }
//
//    public int getLineSize() {
//        return history.isEmpty() ? 1 : (int) history.get(history.size() - 1).lineSize;
//    }
//
//    public void setScale(double scale) {
//        synchronized (this) {
//            history.forEach(od -> {
//                od.scale = scale;
//                TacticalGraphic tg = od.getRenderable();
//                if (tg != null) {
//                    tg.getAttributes().setScale(scale);
//                    tg.getHighlightAttributes().setScale(scale);
//                }
//            });
//        }
//    }
//
//    public double getScale() {
//        return history.isEmpty() ? .3 : history.get(history.size() - 1).scale;
//    }
//
//    public boolean hasLines() {
//        return history.isEmpty() ? false : history.get(history.size() - 1).positions.size() > 1;
//    }
//
//    public void setColor(Color newColor) {
//        synchronized (this) {
//            history.forEach(od -> {
//                od.color = newColor;
//                TacticalGraphic tg = od.getRenderable();
//                if (tg != null) {
//                    tg.getAttributes().setOutlineMaterial(new Material(newColor));
//                }
//            });
//        }
//    }
//
//    public Color getColor() {
//        return history.isEmpty() ? defaultColor : history.get(history.size() - 1).color;
//    }
//
//    public class OverlayData {
//
//        public String symbolCode = "";
//        public long startTime = 0, endTime = 0;
//        public ArrayList<Position> positions = new ArrayList();
//        public ArrayList<String> textFields = new ArrayList();
//        public ArrayList<String> additionalInfo = new ArrayList();
//        public String staffComments;
//        TacticalGraphic renderable = null;
//        boolean isSupported = false;
//        Color color = defaultColor;
//        double scale = .4;
//        double lineSize = 1;
//        int dimension = 0, type = 0;
//        Overlay parent;
//
//        public TacticalGraphic getRenderable() {
//            if (renderable == null && isSupported) {
//                renderable = createTacticalGraphic(symbolCode, positions, textFields, color);
//            }
//            return renderable;
//        }
//
//        // use this for the graphic overlays
//        public void setSymbolCode(int dim, int type, int subtype, int identity, int status) {
//            this.dimension = dim;
//            this.type = type;
//            isGraphic = (dim > 10 && dim < 17);
//            String code = dimentionMap.get(dim);
//            if (code != null) {
//                char identities[] = {'P', 'U', 'A', 'F', 'N', 'S', 'H', 'G', 'W', 'M', 'D', 'L', 'J', 'K', 'F', 'F'};
//                StringBuilder sb = new StringBuilder();
//                sb.append(code.charAt(0));
//                sb.append((code.charAt(1) == '-' ? identities[identity] : code.charAt(1)));
//                sb.append(code.charAt(2));
//                sb.append(((status == 0) ? 'P' : 'A'));
//                sb.append(iconMap.get(String.format("%02d-%02d-%02d", dim, type, subtype)));
//                sb.append("----X");
//                symbolCode = sb.length() == 15 ? sb.toString() : "";
//                if (dim == 12 && type == 7) {
//                    lineSize = 3; // increase the action point line size
//                }
//                setIsSupported();
//            }
//        }
//
//        public void setIsSupported() {
//            isSupported = new MilStd2525GraphicFactory().isSupported(symbolCode);
//        }
//
//        public Color getColor() {
//            return color;
//        }
//
//        public void setColor(Color color) {
//            this.color = color;
//        }
//
//        public TacticalGraphic createTacticalGraphic(String symbolCode, ArrayList<Position> positions, List<String> name, Color color) {
//            TacticalGraphic graphic;
//            try {
//                TacticalGraphicFactory factory = new MilStd2525GraphicFactory();
//                graphic = factory.createGraphic(symbolCode, positions, null);
//                if (graphic != null) {
//                    graphic.setValue(OVERLAY_ID, id);
//                    graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name);
//                    if (!name.isEmpty()) {
//                        graphic.setModifier(SymbologyConstants.UNIQUE_DESIGNATION, name.get(0));
//                        if (name.size() == 1) {
//                            graphic.setValue(AVKey.ROLLOVER_TEXT, graphic.getText());
//                        } else {
////                            graphic.setValue(AVKey.ROLLOVER_TEXT, symbolCode);
//                        }
//                    } else {
////                        graphic.setValue(AVKey.ROLLOVER_TEXT, symbolCode);
//                    }
//                    addJbcpModifiers(graphic);
//                    if (graphic.getValue(AVKey.ROLLOVER_TEXT) == null) {
//                        graphic.setValue(AVKey.ROLLOVER_TEXT, symbolCode);
//                    }
////            // Create a custom attributes bundle. Any fields set in this bundle will override the default attributes.
//                    TacticalGraphicAttributes attrs = new BasicTacticalGraphicAttributes();
//                    if (color != null) {
//                        attrs.setOutlineMaterial(new Material(color));
//                    }
//                    attrs.setScale(scale);
//                    attrs.setOutlineWidth(lineSize);
//                    if(positions.size() == 1){
//                        attrs.setScale(scale);
//                    }
//                    graphic.setAttributes(attrs);
//                    TacticalGraphicAttributes highlights = attrs.copy();
//                    highlights.setOutlineMaterial(Material.WHITE);
//                    highlights.setInteriorMaterial(Material.WHITE);
//                    graphic.setHighlightAttributes(highlights);
//                } else {
//                    System.out.println(symbolCode + " **** Unsupported");
//                }
//            } catch (Exception ex) {
//                graphic = null;
//                Logger.getGlobal().log(Level.WARNING, null, ex);
//            }
//            return graphic;
//        }
//
//        private void addJbcpModifiers(TacticalGraphic graphic) {
//            String rolloverText = (String) graphic.getValue(AVKey.ROLLOVER_TEXT);
//            if (graphic instanceof CircularRangeFan) {
//                // assume a center and a point on each ring
//                if (positions.size() > 1) {
//                    double earthRadius = Main.getController().getWWd().getView().getGlobe().getRadius();
//                    Position center = positions.get(0);
//                    ((CircularRangeFan) graphic).setPositions(positions);
//                    ArrayList<Double> radii = new ArrayList<>();
//                    for (int i = 1; i < positions.size(); i++) {
//                        radii.add((double) ((int) (Position.greatCircleDistance(center, positions.get(i)).radians * earthRadius)));
//                    }
//                    ((CircularRangeFan) graphic).setRadii(radii);
//                    if (textFields.size() > 0) {
//                        ((CircularRangeFan) graphic).setModifier(SymbologyConstants.UNIQUE_DESIGNATION, textFields.get(0));
//                        graphic.setValue(AVKey.ROLLOVER_TEXT, textFields.get(0));
//                    }
//                }
//
//            } else if (graphic instanceof CircularFireSupportArea) {
//                // apparently the radius is sent in the staff comments field
//                double radius = Double.parseDouble(staffComments);
//                ((CircularFireSupportArea) graphic).setPositions(positions);
//                ((CircularFireSupportArea) graphic).setRadius(radius);
//
//            } else if (graphic instanceof MilStd2525PointGraphic && rolloverText == null) {
//                // if no name, use the symbol code name
//                if (this.symbolCode.startsWith("PTS", 4)) {
//                    graphic.setValue(AVKey.ROLLOVER_TEXT, "Point/Single Target");
//                } else if (this.symbolCode.startsWith("GPPK", 4)) {
//                    graphic.setValue(AVKey.ROLLOVER_TEXT, "CHECK POINT");
//                } else if (this.symbolCode.startsWith("GPPR", 4)) {
//                    graphic.setValue(AVKey.ROLLOVER_TEXT, "RALLY POINT");
//                } else if (this.symbolCode.startsWith("GPPS", 4)) {
//                    graphic.setValue(AVKey.ROLLOVER_TEXT, "START POINT");
//                } else if (this.symbolCode.startsWith("GPPE", 4)) {
//                    graphic.setValue(AVKey.ROLLOVER_TEXT, "RELEASE POINT");
//                } else if (this.symbolCode.startsWith("GPO-", 4)) {
//                    graphic.setValue(AVKey.ROLLOVER_TEXT, "ROUTE");
//                }
//            } else if (graphic instanceof RectangularFireSupportArea) {
//                // width set in the staff comments field?
//                double width = Double.parseDouble(staffComments);
//                ((RectangularFireSupportArea) graphic).setWidth(width);
//            } else if (graphic instanceof Boundary && rolloverText == null) {
//                graphic.setValue(AVKey.ROLLOVER_TEXT, "Boundary");
//            } else if (graphic instanceof SupportingAttack && rolloverText == null) {
//                graphic.setValue(AVKey.ROLLOVER_TEXT, "Supporting Attack");
//            } else if (graphic instanceof MainAttack && rolloverText == null) {
//                graphic.setValue(AVKey.ROLLOVER_TEXT, "Main Attack");
//            }
//        }
//    }
}
