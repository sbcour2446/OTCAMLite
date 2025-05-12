package gov.mil.otc._3dvis;

import gov.mil.otc._3dvis.settings.SettingsManager;
import gov.mil.otc._3dvis.ui.main.ErrorApplication;
import gov.mil.otc._3dvis.ui.main.MainApplication;
import gov.mil.otc._3dvis.utility.InstanceChecker;
import javafx.application.Application;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.logging.*;



public class Main {

    public static boolean isLiteMode() {
        return liteMode;
    }

    private static boolean liteMode = true;

    static {
        System.setProperty("gov.nasa.worldwind.app.config.document",
                "gov/mil/otc/_3dvis/config/worldwindow.worldwind.xml");
        System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
    }

    private static boolean startLogger() {
        File logFileDirectory = null;

        try {
            logFileDirectory = new File(System.getProperty("user.home") + File.separator +
                    ".3dvis" + File.separator + "logger");
            if (!logFileDirectory.exists() && logFileDirectory.mkdirs()) {
                Logger.getGlobal().log(Level.SEVERE, "Unable to create logger directory.");
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, "Main::startLogger", e);
        }

        if (logFileDirectory == null || !logFileDirectory.exists()) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select log file directory");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
            if (fileChooser.showDialog(fileChooser, null) != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            logFileDirectory = fileChooser.getSelectedFile();
        }

        String logFilename = String.format("%s\\3dvis_log(%s).log", logFileDirectory.getAbsolutePath(),
                new SimpleDateFormat("yyyyMMdd_HHmmssSSS").format(new GregorianCalendar().getTime()));

        boolean isDebug = java.lang.management.ManagementFactory.
                getRuntimeMXBean().
                getInputArguments().toString().contains("jdwp");
        Level level;

        if (isDebug) {
            level = Level.FINEST;
        } else {
            level = SettingsManager.getSettings().getLogLevel();
        }

        try {
            Logger.getGlobal().setLevel(level);
            FileHandler fileHandler = new AsyncFileHandler(logFilename);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(level);
            Logger.getGlobal().addHandler(fileHandler);
        } catch (IOException | SecurityException e) {
            Logger.getGlobal().log(Level.SEVERE, "Main::startLogger", e);
            return false;
        }

        Logger.getGlobal().setLevel(Level.INFO);
        Logger.getGlobal().log(Level.INFO, "Version: " + gov.mil.otc._3dvis.generated.BuildInformation.VERSION);
        Logger.getGlobal().setLevel(level);

        return true;
    }

    private static void shutdownLogger() {
        for (Handler handler : Logger.getGlobal().getHandlers()) {
            if (handler instanceof AsyncFileHandler asyncFileHandler) {
                asyncFileHandler.shutdown();
            }
        }
    }

    public static void main(String[] args) {
        liteMode = args.length == 0 || !args[0].equalsIgnoreCase("fullMode");

        if (!SettingsManager.initialize()) {
            Application.launch(ErrorApplication.class, "--error=Failed to load settings");
            System.exit(-1);
        }

        if (!startLogger()) {
            Application.launch(ErrorApplication.class, "--error=Failed to start logger");
            System.exit(-1);
        }

        if (InstanceChecker.isInstanceRunning()) {
            Application.launch(ErrorApplication.class, "--error=An instance of 3DVis is already executing.");
            System.exit(-1);
        }

        Application.launch(MainApplication.class, args);

        SettingsManager.save();
        shutdownLogger();
        InstanceChecker.release();
        System.exit(0);
    }
}
