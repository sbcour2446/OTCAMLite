package gov.mil.otc._3dvis.utility;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InstanceChecker {

    private static File file;
    private static FileLock lock;
    private static FileChannel fileChannel;

    private InstanceChecker() {
    }

    public static boolean isInstanceRunning() {
        String lockFolderPath = System.getProperty("user.home") + File.separator + ".3dvis";
        File folderFile = new File(lockFolderPath);
        if (!folderFile.exists() && !folderFile.mkdirs()) {
            Logger.getGlobal().log(Level.WARNING, "Could not create user directory.");
            return true;
        }
        String lockFilePath = lockFolderPath + File.separator + "lockFile.lock";
        file = new File(lockFilePath);
        try {
            fileChannel = FileChannel.open(file.toPath(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            lock = fileChannel.tryLock();
            if (lock == null) {
                return true;
            }
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return true;
        }
        return false;
    }

    public static void release() {
        try {
            lock.release();
            fileChannel.close();
            Files.delete(file.toPath());
        } catch (Exception e) {
            String message = "Error releasing lock file " + file.getAbsolutePath();
            Logger.getGlobal().log(Level.WARNING, message, e);
        }
    }
}
