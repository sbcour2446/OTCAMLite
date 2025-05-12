package gov.mil.otc._3dvis.ui.tools.media;

import gov.mil.otc._3dvis.utility.ImageLoader;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.image.ImageView;

import java.io.File;

public class MediaFileView {

    private final File file;
    private final CheckBox willProcessCheckBox = new CheckBox();
    private final ObjectProperty<ImageView> processed = new SimpleObjectProperty<>();
    private final StringProperty status = new SimpleStringProperty();

    public MediaFileView(File file, boolean willProcess) {
        this.file = file;
        willProcessCheckBox.setSelected(willProcess);
    }

    public File getFile() {
        return file;
    }

    public CheckBox getWillProcess() {
        return willProcessCheckBox;
    }

    public ImageView getProcessed() {
        return processed.get();
    }

    public ObjectProperty<ImageView> processedProperty() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        setProcessed(processed ? new ImageView(ImageLoader.getFxImage("/images/dot_red.png")) :
                new ImageView(ImageLoader.getFxImage("/images/dot_white.png")));
    }

    public void setProcessed(ImageView processed) {
        this.processed.set(processed);
    }

    public String getStatus() {
        return status.get();
    }

    public StringProperty statusProperty() {
        return status;
    }

    public void setStatus(String status) {
        this.status.set(status);
    }
}
