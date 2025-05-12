package gov.mil.otc._3dvis.project.avcad;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AvcadConfiguration {

    public static AvcadConfiguration load(File file) {
        AvcadConfiguration avcadConfiguration = null;
        Gson gson = new Gson();
        if (file.exists() && file.canRead()) {
            try {
                byte[] fileContent = Files.readAllBytes(Paths.get(file.getPath()));
                String fileString = new String(fileContent);
                avcadConfiguration = gson.fromJson(fileString, AvcadConfiguration.class);
            } catch (Exception e) {
                String message = String.format("Unable to read preferences file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        if (avcadConfiguration == null) {
            avcadConfiguration = new AvcadConfiguration();
            if (file.exists()) {
                try {
                    Files.copy(file.toPath(), new File(file.getAbsolutePath() + ".bak").toPath());
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "AvcadConfiguration::load", e);
                }
            }
            try (FileWriter fileWriter = new FileWriter(file, false)) {
                Gson prettyGson = gson.newBuilder().setPrettyPrinting().create();
                String serializedObject = prettyGson.toJson(avcadConfiguration);
                fileWriter.write(serializedObject);
                fileWriter.flush();
            } catch (IOException e) {
                String message = String.format("Unable to save avcad configuration file %s", file.getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
        }
        return avcadConfiguration;
    }

    @SerializedName("sensor timezone offset")
    private int timeZonOffset;

    @SerializedName("alarms")
    private final List<String> alarmList = new ArrayList<>();

    @SerializedName("alerts")
    private final List<String> alertList = new ArrayList<>();

    // default values
    private AvcadConfiguration() {
        timeZonOffset = 0;

        alarmList.add("a-230");
        alarmList.add("a-232");
        alarmList.add("a-234");
        alarmList.add("at4");
        alarmList.add("dmmp");
        alarmList.add("ga");
        alarmList.add("gb");
        alarmList.add("gd");
        alarmList.add("gf");
        alarmList.add("hd");
        alarmList.add("hn3");
        alarmList.add("tep");
        alarmList.add("vr");
        alarmList.add("vs");

        alertList.add("startup error");
        alertList.add("startup radio frequency (rf) error");
        alertList.add("startup time out");
        alertList.add("seal failure");
        alertList.add("no targets");
        alertList.add("missing module");
        alertList.add("missing module");
        alertList.add("no scanner");
        alertList.add("configuration error");
        alertList.add("improper shutdown");
        alertList.add("storage integrity error");
        alertList.add("adjustment error");
        alertList.add("pressure time out");
        alertList.add("low pressure error");
        alertList.add("low pressure error");
        alertList.add("precon heater error");
        alertList.add("net error");
        alertList.add("manifold pressure error");
        alertList.add("connectivity error");
        alertList.add("ionizer error");
        alertList.add("ionizer error");
        alertList.add("ms detector error 1");
        alertList.add("ms detector error 2");
        alertList.add("inlet voltage error");
        alertList.add("ion trap rf error");
        alertList.add("startup rf error");
        alertList.add("startup rf error");
        alertList.add("startup rf error");
        alertList.add("rf delivery error");
        alertList.add("startup detector error");
        alertList.add("manifold pressure error");
        alertList.add("manifold pressure error");
        alertList.add("manifold pressure error");
        alertList.add("high pressure");
        alertList.add("flow sensor 1 error");
        alertList.add("flow sensor 2 error");
        alertList.add("sample flow error 1");
        alertList.add("sample flow error 2");
        alertList.add("desorb flow error 1");
        alertList.add("desorb flow error 2");
        alertList.add("processing fault");
        alertList.add("temperature fault");
        alertList.add("battery communication error");
        alertList.add("missing media");
        alertList.add("export failed");
        alertList.add("system overheating");
        alertList.add("system hardware fault");
        alertList.add("system hardware fault");
        alertList.add("internal error");
    }

    public int getTimeZonOffset() {
        return timeZonOffset;
    }

    public List<String> getAlarmList() {
        return alarmList;
    }

    public List<String> getAlertList() {
        return alertList;
    }
}
