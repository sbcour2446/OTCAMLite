package gov.mil.otc._3dvis.ui;

import javafx.scene.control.Label;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class UiCommon {

    public static Label createTitleLabel(String title) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font(UiConstants.FONT_NAME, FontWeight.BOLD, 18));
        return titleLabel;
    }
}
