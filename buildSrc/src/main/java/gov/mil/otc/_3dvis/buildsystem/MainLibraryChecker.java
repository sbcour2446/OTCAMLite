package gov.mil.otc._3dvis.buildsystem;

/**
 * Simple main loop that takes a library name without pathing or a file suffix and returns a success or failure
 * post load.
 * <p>
 * Whether system.loadLibrary() can find a library depends on the native library loading schema of the operating system.
 * For example, PATH on Windows or LD_LIBRARY_PATH on Linux-based operating systems should contain the path of all
 * discoverable native libraries and their dependencies.
 */
public class MainLibraryChecker {
    /**
     * Main loop
     *
     * @param args The name of the library without file path or suffix.
     */
    public static void main(String[] args) {
        String libraryName = args[0];
        boolean retVal;
        try {
            System.loadLibrary(libraryName);
            retVal = true;
        } catch (SecurityException | UnsatisfiedLinkError e) {
            retVal = false;
        }

        System.exit((retVal) ? 0 : -1);
    }
}
