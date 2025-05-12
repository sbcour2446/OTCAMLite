package gov.mil.otc._3dvis.event.otcam;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.IconImageHelper;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;

import java.awt.*;
import java.awt.image.BufferedImage;

class EventIndicator extends PointPlacemark {

    EventIndicator(IEntity entity) {
        super(entity.getPosition());
        initialize(entity);
    }

    private void initialize(IEntity entity) {
        BufferedImage eventIndicatorImage = IconImageHelper.createEventIndicator(entity);
        activeAttributes.setLabelFont(Common.FONT_SMALL);
        activeAttributes.setLabelMaterial(Material.RED);
        activeAttributes.setLabelOffset(new Offset(eventIndicatorImage.getWidth() / 2d,
                (double) eventIndicatorImage.getHeight(), AVKey.PIXELS, AVKey.PIXELS));
        activeAttributes.setImageColor(new Color(1f, 1f, 1f, .5f));
        activeAttributes.setImageOffset(new Offset((eventIndicatorImage.getWidth() / 2d), 0d, AVKey.PIXELS, AVKey.PIXELS));
        activeAttributes.setImage(eventIndicatorImage);

        PointPlacemarkAttributes highlightAttributes = new PointPlacemarkAttributes();
        highlightAttributes.copy(activeAttributes);
        highlightAttributes.setImageColor(new Color(1f, 1f, 1f, 1f));

        setAttributes(activeAttributes);
        setHighlightAttributes(highlightAttributes);
    }
}
