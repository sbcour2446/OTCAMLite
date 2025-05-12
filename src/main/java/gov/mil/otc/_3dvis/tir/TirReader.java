package gov.mil.otc._3dvis.tir;

import gov.mil.otc._3dvis.datamodel.TimedFile;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBufferedFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TirReader {

    private static final String DATE_FORMAT = "dd MMM yyyy HHmm ZZZ";

    private TirReader() {
    }

    public static void show(File file) {
        try (PDDocument doc = Loader.loadPDF(file)) {
            PDFRenderer renderer = new PDFRenderer(doc);
            BufferedImage image = renderer.renderImage(0);
            ImageIO.write(image, "PNG", new File("target", "custom-render.png"));
        } catch (Exception e) {

        }

        try {
            Desktop.getDesktop().open(file);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "TirReader::show", e);
        }
    }

    public static TimedFile processFile(File file) {
        long time = getTime(file);
        return new TimedFile(time, file, TimedFile.FileType.TIR, "Manual Data");
    }

    public static long getTime(File file) {
        try {
            String content = getText(file);
            String dateField = "40. Date & Time: ";
            int index = content.indexOf(dateField) + dateField.length();
            String dateString = content.substring(index, index + DATE_FORMAT.length()).trim();
            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            Date date = dateFormat.parse(dateString);
            return date.getTime();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "TirReader::getTime", e);
        }
        return 0;
    }

    public static String getTirNumber(File file) {
        try {
            String content = getText(file);
            String tirNumberField = "4. ";
            int index = content.indexOf(tirNumberField) + tirNumberField.length();
            return content.substring(index, index + 10).trim();
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, "TirReader::getTirNumber", e);
        }
        return "";
    }

    private static String getText(File file) throws IOException {
        try (PDDocument pdDocument = Loader.loadPDF(new RandomAccessReadBufferedFile(file))) {
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            return pdfTextStripper.getText(pdDocument);
        }
    }
}
