package gov.mil.otc._3dvis.data.tapets;

import gov.mil.otc._3dvis.datamodel.TspiData;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TapetsLogFile extends File {

    private final Earth earth = new Earth();
    private final List<TapetsMessage> tapetsMessages = new ArrayList<>();
    private final List<TspiData> tspiDataList = new ArrayList<>();
    private int unitId = 0;
    private long startTime = Long.MAX_VALUE;
    private long stopTime = Long.MIN_VALUE;
    private boolean isProcessed = false;

    public TapetsLogFile(File file) {
        super(file.getAbsolutePath());
    }

    public List<TapetsMessage> getTapetsMessages() {
        return tapetsMessages;
    }

    public List<TspiData> getTspiDataList() {
        return tspiDataList;
    }

    public int getUnitId() {
        return unitId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public boolean isProcessed() {
        return isProcessed;
    }

    public boolean process() {
        if (!isProcessed) {
            double previousX = 0;
            double previousY = 0;
            double previousZ = 0;
            try {
                byte[] bytes = Files.readAllBytes(toPath());
                ByteBuffer buffer = ByteBuffer.wrap(bytes);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                while (buffer.hasRemaining()) {
                    TapetsMessage tapetsMessage = new TapetsMessage();
                    tapetsMessage.deserialize(buffer);
                    tapetsMessages.add(tapetsMessage);

                    double x = tapetsMessage.getPayload().getEcefX();
                    double y = tapetsMessage.getPayload().getEcefY();
                    double z = tapetsMessage.getPayload().getEcefZ();
                    if (previousX == x && previousY == y && previousZ == z) {
                        continue;
                    }
                    previousX = x;
                    previousY = y;
                    previousZ = z;

                    Position position = earth.computePositionFromPoint(new Vec4(y, z, x));
                    tspiDataList.add(new TspiData(tapetsMessage.getTimestamp(), position));

                    unitId = tapetsMessage.getUnitId();

                    if (tapetsMessage.getTimestamp() < startTime) {
                        startTime = tapetsMessage.getTimestamp();
                    }

                    if (tapetsMessage.getTimestamp() > stopTime) {
                        stopTime = tapetsMessage.getTimestamp();
                    }
                }
            } catch (IOException e) {
                String message = String.format("Error processing TAPETS file %s.", getAbsolutePath());
                Logger.getGlobal().log(Level.WARNING, message, e);
            }
            isProcessed = true;
        }
        return !tapetsMessages.isEmpty();
    }
}
