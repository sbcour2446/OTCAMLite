package gov.mil.otc._3dvis.entity.render;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.WWController;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.layer.Constant;
import gov.mil.otc._3dvis.layer.control.ButtonAnnotation;
import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.drag.DragContext;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.geom.coords.MGRSCoord;
import gov.nasa.worldwind.geom.coords.UTMCoord;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.util.Logging;

import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import static gov.mil.otc._3dvis.layer.Constant.OPACITY;

public class StatusAnnotation extends GlobeAnnotation implements SelectListener {

    private static final int WIDTH = 450;
    private static final int MAX_FIELD_LENGTH = 50;
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Common.DATE_TIME_WITH_MILLIS);
    private final BasicDragger basicDragger;
    private final ButtonAnnotation closeButton = new ButtonAnnotation(
            ImageLoader.getBufferedImage("/images/browser-close-16x16.gif"));
    private IEntity entity;
    private int height = 0;
    private boolean closeButtonInitialized = false;
    private Point lastScreenPoint = null;
    private int nameFieldWidth = 11;
    private int stabilizedWidth = 0;

    public StatusAnnotation(IEntity entity) {
        super(String.format(String.format("%%-%ds : ", 100), " "), entity.getPosition());
        this.entity = entity;
        basicDragger = new BasicDragger(WWController.getWorldWindowPanel());
        WWController.getWorldWindowPanel().addSelectListener(this);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        initialize();
    }

    private void initialize() {
        attributes.setFont(Common.FONT_MEDIUM_BOLD);
        attributes.setBorderColor(new Color(1f, 1f, 1f, .4f));
        attributes.setBackgroundColor(new Color(0, 0, 0, .4f));
        attributes.setTextColor(Color.WHITE);
        attributes.setCornerRadius(0);
        attributes.setEffect(AVKey.TEXT_EFFECT_OUTLINE);
        attributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        if (entity.isMunition() || entity.isAircraft()) {
            setAltitudeMode(WorldWind.ABSOLUTE);
        } else {
            setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
            setPosition(new Position(getPosition(), 0));
        }
        attributes.setDrawOffset(new Point(50, 100));
        attributes.setSize(new Dimension(WIDTH, 0));
        attributes.setInsets(new Insets(10, 10, 10, 25));
        setDragEnabled(true);
        setPickEnabled(true);

        closeButton.getAttributes().setOpacity(OPACITY);
        closeButton.setPickEnabled(true);
        addChild(closeButton);

        text = getStatusAnnotation();
    }

    public void updateText() {
        text = getStatusAnnotation();
        closeButtonInitialized = false;
    }

    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder();

        addRow(stringBuilder, "ID", entity.getEntityId().toString());

        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail == null) {
            return stringBuilder.toString();
        }

        addRow(stringBuilder, "Name", entityDetail.getName());
        addRow(stringBuilder, "Source", entityDetail.getSource());
        addRow(stringBuilder, "EntityType", entityDetail.getEntityType().toString());
        if (!entityDetail.getEntityType().isUnknown()) {
            addRow(stringBuilder, "Description", entityDetail.getEntityType().getDescription());
        }
        addRow(stringBuilder, "Affiliation", entityDetail.getAffiliation().toString());

        if (entityDetail.getUrn() > 0) {
            addRow(stringBuilder, "URN", String.valueOf(entityDetail.getUrn()));
        }

        addRow(stringBuilder, "Status", entityDetail.getRtcaState().toString());
        addRow(stringBuilder, "Location", getLocationString());

        TspiData tspiData = entity.getCurrentTspi();
        if (tspiData != null && tspiData.getHeading() != null && tspiData.getPitch() != null && tspiData.getRoll() != null) {
            String headingString = String.format("%3.2f", tspiData.getHeading());
            String pitchString = String.format("%3.2f", tspiData.getPitch());
            String rollString = String.format("%3.2f", tspiData.getRoll());

            addRow(stringBuilder, "Orientation", String.format("H:%6s°, P:%6s°, R:%6s°",
                    headingString, pitchString, rollString));
        }

        if (entityDetail.getMilesPid() > 0) {
            addRow(stringBuilder, "MILES", String.valueOf(entityDetail.getMilesPid()));
        }

        if (!entityDetail.getRtcaState().getRtcaOther().isEmpty()) {
            addRow(stringBuilder, "\u00A0Status(ex)", entityDetail.getRtcaState().getRtcaOther());
        }

        if (entityDetail.getRtcaState().getDamagePercent() > 0) {
            addRow(stringBuilder, "Damage", String.valueOf(entityDetail.getRtcaState().getDamagePercent()));
        }

        Calendar timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        timestamp.setTimeInMillis(entity.getLastUpdateTime());
        addRow(stringBuilder, "Last Update", simpleDateFormat.format(timestamp.getTime()));

        if (entity.getLastEvent() != null) {
            addRow(stringBuilder, "Last Event", entity.getLastEvent().getDescription());
        }

        return stringBuilder.toString();
    }

    protected void addRow(StringBuilder stringBuilder, String name, String value) {
        if (name.length() > nameFieldWidth) {
            nameFieldWidth = Math.min(name.length(), MAX_FIELD_LENGTH);
        }

        stringBuilder.append(String.format(String.format("%%-%ds : ", nameFieldWidth),
                name.substring(0, Math.min(name.length(), nameFieldWidth))));

        String[] lineValues = value.split(System.lineSeparator());
        for (int i = 0; i < lineValues.length; i++) {
            String line = lineValues[i];
            int startIndex = 0;
            int endIndex = line.length();
            if (endIndex > MAX_FIELD_LENGTH) {
                line = line + " ";
                endIndex = line.substring(0, MAX_FIELD_LENGTH).lastIndexOf(" ") + 1;
            }
            stringBuilder.append(line, startIndex, endIndex);
            stringBuilder.append(System.lineSeparator());
            while (endIndex < line.length()) {
                startIndex = endIndex;
                endIndex = line.length();
                if (endIndex > startIndex + MAX_FIELD_LENGTH) {
                    endIndex = startIndex + line.substring(startIndex, startIndex + MAX_FIELD_LENGTH).lastIndexOf(" ") + 1;
                }
                stringBuilder.append(String.format(String.format("\u00A0%%-%ds : ", nameFieldWidth - 1), " "));
                stringBuilder.append(line, startIndex, endIndex);
                stringBuilder.append(System.lineSeparator());
            }
            if (i < lineValues.length - 1) {
                stringBuilder.append(String.format(String.format("\u00A0%%-%ds : ", nameFieldWidth - 1), " "));
            }
        }
    }

    protected String getLocationString() {
        switch (SettingsManager.getPreferences().getUnitPreference().getPositionUnit()) {
            case LAT_LON_DD -> {
                return String.format("%3.6f°, %3.6f°, %,dm",
                        getPosition().getLatitude().degrees, getPosition().getLongitude().degrees,
                        (int) Math.round(getPosition().getElevation()));
            }
            case MGRS -> {
                MGRSCoord mgrsCoord = MGRSCoord.fromLatLon(getPosition().getLatitude(), getPosition().getLongitude());
                return String.format("%s, %,dm", mgrsCoord, (int) Math.round(getPosition().getElevation()));
            }
            case UTM -> {
                UTMCoord utmCoord = UTMCoord.fromLatLon(getPosition().getLatitude(), getPosition().getLongitude());
                return String.format("%s%s %dE %dN, %,dm", utmCoord.getZone(),
                        utmCoord.getHemisphere().equals(AVKey.NORTH) ? 'N' : 'S',
                        (int) utmCoord.getEasting(), (int) utmCoord.getNorthing(),
                        (int) Math.round(getPosition().getElevation()));
            }
            default -> {
                return "unknown format";
            }
        }
    }

    @Override
    public void selected(SelectEvent event) {
        if (event.getTopObject() == closeButton) {
            String eventAction = event.getEventAction();
            if (SelectEvent.ROLLOVER.equals(eventAction)) {
                if (!closeButton.getAttributes().isHighlighted()) {
                    closeButton.getAttributes().setHighlighted(true);
                }
            } else if (SelectEvent.LEFT_PRESS.equals(eventAction)) {
                entity.toggleStatusAnnotation();
            }
        } else {
            if (closeButton.getAttributes().isHighlighted()) {
                closeButton.getAttributes().setHighlighted(false);
            }

            if (event.getEventAction().equals(SelectEvent.DRAG) && event.getTopObject() == this) {
                basicDragger.selected(event);
            }
        }
    }

    @Override
    public void drag(DragContext dragContext) {
        int changeX = dragContext.getPoint().x - dragContext.getPreviousPoint().x;
        int changeY = dragContext.getPoint().y - dragContext.getPreviousPoint().y;

        AnnotationAttributes attrs = getAttributes();
        int newX = attrs.getDrawOffset().x + changeX;
        int newY = attrs.getDrawOffset().y - changeY;
        attrs.setDrawOffset(new Point(newX, newY));
    }

    @Override
    protected void doRenderNow(DrawContext dc) {
        if (dc.isPickingMode() && getPickSupport() == null) {
            return;
        }

        Vec4 point = getAnnotationDrawPoint(dc);
        if (point == null) {
            return;
        }

        if (dc.getView().getFrustumInModelCoordinates().getNear().distanceTo(point) < 0) {
            return;
        }

        Vec4 screenPoint = dc.getView().project(point);
        if (screenPoint == null) {
            return;
        }

        Dimension size = getPreferredSize(dc);

        if (lastScreenPoint == null) {
            lastScreenPoint = new Point((int) screenPoint.x, (int) screenPoint.y);
        } else if (dc.getView().equals(WWController.getWorldWindowPanel().getView())) {
            Point newScreenPoint = new Point((int) screenPoint.x, (int) screenPoint.y);
            int changeX = newScreenPoint.x - lastScreenPoint.x;
            int changeY = newScreenPoint.y - lastScreenPoint.y;

            if (height != size.height) {
                changeY += size.height - height;
            }

            lastScreenPoint = newScreenPoint;
            AnnotationAttributes attrs = getAttributes();
            int newX = attrs.getDrawOffset().x - changeX;
            int newY = attrs.getDrawOffset().y - changeY;

            if ((screenPoint.y + newY) < Constant.TIMELINE_TOP) {
                newY = (int) -screenPoint.y + Constant.TIMELINE_TOP;
            } else if ((screenPoint.y + newY + size.height) > dc.getView().getViewport().height) {
                newY = dc.getView().getViewport().height - (int) screenPoint.y - size.height;
            }

            int middle = size.width / 2;
            if ((screenPoint.x + newX - middle) < 0) {
                newX = (int) -screenPoint.x + size.width / 2;
            } else if ((screenPoint.x + newX + middle) > dc.getView().getViewport().width) {
                newX = dc.getView().getViewport().width - (int) screenPoint.x - middle;
            }

            attrs.setDrawOffset(new Point(newX, newY));
        }

        Position pos = dc.getGlobe().computePositionFromPoint(point);

        // Scale and opacity depending on distance from eye
        double[] scaleAndOpacity = computeDistanceScaleAndOpacity(dc, point, size);

        drawTopLevelAnnotation(dc, (int) screenPoint.x, (int) screenPoint.y, size.width, size.height,
                scaleAndOpacity[0], scaleAndOpacity[1], pos);

        if (!closeButtonInitialized || height != size.height) {
            height = size.height;
            Rectangle insetBounds = computeInsetBounds(size.width, size.height);
            closeButton.getAttributes().setDrawOffset(new Point(insetBounds.width + 8, insetBounds.height - 7));
            closeButtonInitialized = true;
        }
    }

    @Override
    public Dimension getPreferredSize(DrawContext dc) {
        if (dc == null) {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        // Clamp the caller specified size.
        Dimension size = new Dimension(getAttributes().getSize());
        if (size.width < 1) {
            size.width = 1;
        }
        if (size.height < 0) {
            size.height = 0;
        }

        // Compute the size of this annotation's inset region.
        java.awt.Rectangle insetBounds = computeInsetBounds(size.width, size.height);
        Dimension insetSize = new Dimension(insetBounds.width, insetBounds.height);

        // Wrap the text to fit inside the annotation's inset bounds. Then adjust the inset bounds to the wrapped
        // text, depending on the annotation's attributes.
        insetSize = adjustSizeToText(dc, insetSize.width, insetSize.height);

        // Adjust the inset bounds to the child annotations.
        java.awt.Insets insets = getAttributes().getInsets();
        Dimension dimension = new Dimension(
                insetSize.width + (insets.left + insets.right),
                insetSize.height + (insets.top + insets.bottom));

        if (dimension.width < stabilizedWidth) {
            dimension.width = stabilizedWidth;
        } else if (dimension.width > stabilizedWidth) {
            stabilizedWidth = dimension.width;
            attributes.setSize(new Dimension(dimension.width, 0));
        }

        if (dimension.height > dc.getView().getViewport().getHeight() - Constant.TIMELINE_TOP) {
            dimension.height = (int) dc.getView().getViewport().getHeight() - Constant.TIMELINE_TOP;
        }

        return dimension;
    }

    @Override
    protected java.awt.Dimension adjustSizeToText(DrawContext dc, int width, int height) {
        AnnotationAttributes attribs = getAttributes();

        String text = getWrappedText(dc, width, height, getText(), attribs.getFont(), attribs.getTextAlign());
        java.awt.Rectangle textBounds = getTextBounds(dc, getText(), attribs.getFont(), attribs.getTextAlign());

        // If the attributes specify to fit the annotation to the wrapped text width, then set the inset width to
        // the wrapped text width.
        if (attribs.getAdjustWidthToText().equals(AVKey.SIZE_FIT_TEXT) && !text.isEmpty()) {
            width = textBounds.width;
        }

        // If the inset height is less than or equal to zero, then override the inset height with the the wrapped
        // text height.
        if (height <= 0) {
            height = textBounds.height;
        }

        return new java.awt.Dimension(width, height);
    }

    public IEntity getEntity() {
        return entity;
    }

    public void setEntity(IEntity entity) {
        this.entity = entity;
    }
}
