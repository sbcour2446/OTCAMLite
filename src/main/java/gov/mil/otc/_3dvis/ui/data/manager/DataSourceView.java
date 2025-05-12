package gov.mil.otc._3dvis.ui.data.manager;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.data.DataSource;
import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.ImageView;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DataSourceView {

    private final DataSource dataSource;
    private final ObjectProperty<ImageView> use = new SimpleObjectProperty<>();

    public DataSourceView(DataSource dataSource) {
        this.dataSource = dataSource;
        setUse(dataSource.isUse());
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public ImageView getUse() {
        return use.get();
    }

    public ObjectProperty<ImageView> useProperty() {
        return use;
    }

    public void setUse(boolean use) {
        setUse(use ? new ImageView(ImageLoader.getFxImage("/images/dot_red.png")) :
                new ImageView(ImageLoader.getFxImage("/images/dot_white.png")));
    }

    public void setUse(ImageView use) {
        this.use.set(use);
    }

    public String getName() {
        return dataSource.getName();
    }

    public String getStartTime() {
        LocalDateTime localDateTime = Instant.ofEpochMilli(dataSource.getStartTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
        return localDateTime.format(dateTimeFormatter);
    }

    public String getStopTime() {
        LocalDateTime localDateTime = Instant.ofEpochMilli(dataSource.getStopTime()).atZone(ZoneId.of("UTC")).toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(Common.DATE_TIME_WITH_MILLIS);
        return localDateTime.format(dateTimeFormatter);
    }
}
