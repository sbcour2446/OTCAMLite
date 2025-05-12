package gov.mil.otc._3dvis.ui.widgets;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class CircularText extends Pane {

    private double circleWidth;
    private double circleHeight;
    private double textSize;
    private double textStartDegree;
    private double letterPos;
    private double textRotate;
    private double gapSpacing;
    private double offSetX;
    private double offSetY;
    private Font font;
    private Paint textFill;
    private String fontName;
    final String text;

    public CircularText(String text) {
        this.circleWidth = 250;
        this.circleHeight = 250;
        this.text = text;
        textSize = (this.circleWidth / this.text.length()) * 2;
        this.fontName = "consolas";
        this.font = new Font(this.fontName, textSize);
        this.textFill = Color.BLACK;
        this.textStartDegree = 240;
        this.gapSpacing = 0.975;
        this.letterPos = 360 / (text.length() / this.gapSpacing);
        this.textRotate = 90;
        this.offSetX = 4;
        this.offSetY = 3;
        paintText(this.text, this.font);
    }

    public CircularText(String text, double width, double height) {
        this.circleWidth = width;
        this.circleHeight = height;
        this.text = text;
        textSize = (this.circleWidth / (this.text.length()) * 2);
        this.fontName = "consolas";
        this.font = new Font(this.fontName, textSize);
        this.textFill = Color.BLACK;
        this.textStartDegree = 240;
        this.gapSpacing = 0.975;
        this.letterPos = 360 / (text.length() / this.gapSpacing);
        this.textRotate = 90;
        this.offSetX = 4;
        this.offSetY = 3;
        paintText(this.text, this.font);
    }

    public Paint getTextFill() {
        return textFill;
    }

    public void setTextFill(Paint textFill) {
        this.textFill = textFill;
        paintText(this.text, this.font);
    }

    public double getTextStartDegree() {
        return textStartDegree;
    }

    public void setTextStartDegree(double textStartDegree) {
        this.textStartDegree = textStartDegree;
        paintText(this.text, this.font);
    }

    public double getLetterPos() {
        return this.letterPos;
    }

    public void setLetterPos(double degrees) {
        this.letterPos = degrees / (text.length() / this.gapSpacing);
        paintText(this.text, this.font);
    }

    public double getTextRotate() {
        return textRotate;
    }

    public void setTextRotate(double textRotate, double textFlowControl) {
        this.textRotate = textRotate;
        setLetterPos(textFlowControl);
        paintText(this.text, this.font);
    }

    public double getGapSpacing() {
        return gapSpacing;
    }

    public void setGapSpacing(double gapSpacing) {
        this.gapSpacing = gapSpacing;
        this.letterPos = 360 / (text.length() / this.gapSpacing);
        paintText(this.text, this.font);
    }

    public Font getFont() {
        return this.font;
    }

    public void setFont(Font font) {
        this.font = font;
        paintText(this.text, this.font);
    }

    public void setFont(String name) {
        this.fontName = name;
        this.font = new Font(this.fontName, textSize);
        paintText(this.text, this.font);
    }

    public void setFont(String name, double textSize,
                        double offSetX, double offSetY) {
        this.textSize = textSize;
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.fontName = name;
        this.font = new Font(this.fontName, textSize);
        paintText(this.text, this.font);
    }

    public double getTextSize() {
        return this.textSize;
    }

    public void setTextSize(double textSize, double offSetX,
                            double offSetY) {
        this.textSize = textSize;
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.font = new Font(this.fontName, textSize);
        paintText(this.text, this.font);
    }

    public double getCircleWidth() {
        return circleWidth;
    }

    public void setCircleWidth(double w) {
        this.circleWidth = w;
        textSize = (this.circleWidth / this.text.length()) * 2;
        paintText(this.text, this.font);
    }

    public double getCircleHeight() {
        return circleHeight;
    }

    public void setCircleHeight(double h) {
        this.circleHeight = h;
        textSize = (this.circleWidth / this.text.length()) * 2;
        paintText(this.text, this.font);
    }

    protected void paintText(String text, Font font) {
        getChildren().clear();
        // Initialize parameters
        double radius = Math.min(circleWidth, circleHeight) * 0.8 * 0.5;
        double centerX = this.getWidth() / 2;
        double centerY = this.getHeight() / 2;

        // Place text in a circular pattern
        int i = 0;
        double degree = this.letterPos;
        for (double degrees = this.textStartDegree;
             i < text.length(); i++, degrees += degree) {
            double pointX = centerX + radius *
                    Math.cos(Math.toRadians(degrees));
            double pointY = centerY + radius *
                    Math.sin(Math.toRadians(degrees));
            pointX -= (font.getSize() / 3);
            pointY += (font.getSize() / 3);
            Text letter = new Text(pointX, pointY,
                    String.valueOf(text.charAt(i)));
            letter.setFont(font);
            letter.setFill(this.textFill);
            letter.setRotate(degrees + this.textRotate);

            getChildren().add(letter);
        }
    }
}