/*
 * Software module written by GaN Corporation for US Army Operational Test Command.
 */
package vmf;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * a wrapper fo the old C++ VMF parser DLL
 *
 * Example command to create header file: "C:\Program
 * Files\Java\jdk1.8.0_201\bin\javah.exe" -cp "C:\Users\Public\TTSS
 * Code\PcapPlugin\build\classes" -d "C:\Users\hansen\Documents\Visual Studio
 * 2015\Projects" vmf.MsgBodyParser"
 *
 * version information can be found in the Globals.h in the parser solution.
 *
 * @author hansen
 */
public class MsgBodyParser {

    public static final String DECODE_FAIL = "No Decode Available. (VMF DLL not loaded)";

    static native String parse(int version, int fad, int msgNum, byte[] data, boolean verbose);
    static boolean isSupported = false;

    static {
        try {
            String filename = "external/VmfParser.dll";
            File file = new File(filename);
            if (file.exists()) {
                System.load(file.getAbsolutePath());
                isSupported = true;
            }
        } catch (Error e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
    }

    static public String parseVmf(int version, int fad, int msgNum, byte[] data, boolean verbose) {

        if (isSupported) {
            try {
                return parse(version, fad, msgNum, data, verbose);
            } catch (java.lang.UnsatisfiedLinkError ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
                return DECODE_FAIL;
            }
        } else {
            return DECODE_FAIL;
        }
    }

}
