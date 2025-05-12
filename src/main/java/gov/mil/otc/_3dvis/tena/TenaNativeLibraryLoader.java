package gov.mil.otc._3dvis.tena;

import TENA.Middleware.LibraryUtility;

import java.util.logging.Level;
import java.util.logging.Logger;

public class TenaNativeLibraryLoader {

    private static boolean successfullyInitialized = false;

    private TenaNativeLibraryLoader() {
    }

    public static boolean loadTenaLibraries() {
        try {
            Logger.getGlobal().log(Level.INFO, "Loading TENA library...");

            // Set system property values that TENA needs, to avoid needing environment variables and TENA
            // barking a warning.
            System.setProperty("TENA_PLATFORM", "w10-vs2019-64");
            System.setProperty("TENA_VERSION", "6.0.8");

            /*
             * Load each denoted object model and its corresponding dependencies. If a dependency is missing,
             * an exception is thrown, and initialization will fail.
             *
             * java.library.path contains the directories these libraries will look for.
             */
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-Configuration-v0.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-DlmMessage-v0.0.3-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-OtcamEntity-v0.1.2-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-OtcamEventMessage-v0.0.8-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-OtcamFusedEntity-v0.0.2-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-OtcamStation-v0.0.3-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-Recovered-v0.0.8-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("OTC-OTCAM-VmfMessage-v0.0.2-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-exceptions-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Geospatial-PointOfInterest-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Geospatial-TSPIcovariance-v1.0.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Hardware-System-v1.0.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Instrumentation-PointableSystem-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Instrumentation-Track-AzimuthElevation-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Instrumentation-Track-Azimuth-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Instrumentation-Track-State3D-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-LVC-Common-v1.0.0-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility.
                    actualImplName("TENA-LVC-Engagement-v1.1.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-LVC-Entity-v1.1.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-LVC-EntityAppearanceCapabilities-v1.0.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-LVC-EntityType-v1.0.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-LVC-IDs-v1.0.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-LVC-Track-v1.1.0-StdImpl-v1.0.1-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-Time-v2-StdImpl-v1.1.4-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-TSPI-v5-StdImpl-v1.0.4-$(TENA_PLATFORM)-v$(TENA_VERSION)"));
            System.loadLibrary(LibraryUtility
                    .actualImplName("TENA-UniqueID-v3-StdImpl-v1.0.3-$(TENA_PLATFORM)-v$(TENA_VERSION)"));

            Logger.getGlobal().log(Level.INFO, "TENA library loaded successfully");
            successfullyInitialized = true;
        } catch (Error e) {
            Logger.getGlobal().log(Level.SEVERE, "Unable to load TENA libraries.", e);
            successfullyInitialized = false;
        }

        return successfullyInitialized;
    }

    public static boolean isInitialized() {
        return successfullyInitialized;
    }
}
