package gov.mil.otc._3dvis;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.LogRecord;

public class AsyncFileHandler extends FileHandler {

    private final Queue<LogRecord> publishQueue = new LinkedList<>();
    private boolean isRunning = true;

    public AsyncFileHandler(String pattern) throws IOException, SecurityException {
        super(pattern);

        new Thread(this::logger).start();
    }

    public void shutdown() {
        isRunning = false;
        synchronized (publishQueue) {
            publishQueue.notify();
        }
    }

    @Override
    public void publish(LogRecord record) {
        if (!isRunning) {
            return;
        }

        synchronized (publishQueue) {
            publishQueue.add(record);
            publishQueue.notify();
        }
    }

    private void logger() {
        while (isRunning) {
            LogRecord record = null;

            synchronized (publishQueue) {
                if (!publishQueue.isEmpty()) {
                    record = publishQueue.remove();
                }
            }

            if (record != null) {
                super.publish(record);
            }

            synchronized (publishQueue) {
                try {
                    publishQueue.wait(1000);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }

        synchronized (publishQueue) {
            while (!publishQueue.isEmpty()) {
                LogRecord record = publishQueue.remove();

                if (record != null) {
                    super.publish(record);
                }
            }
        }
    }
}
