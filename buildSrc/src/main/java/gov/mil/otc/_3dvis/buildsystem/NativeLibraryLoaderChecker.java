package gov.mil.otc._3dvis.buildsystem;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

/**
 * A class that attempts to determine if all external native libraries can be found.
 */
public class NativeLibraryLoaderChecker extends DefaultTask {
    /**
     * The libraries to check.  This should be the name of the library without a path or file suffix.
     */
    @Input
    public List<String> librariesToCheck = new ArrayList<>();

    /**
     * Additional locations for libraries.
     */
    @Input
    public List<String> additionalSearchPaths = new ArrayList<>();

    /**
     * Whether a warning or exception should be thrown upon results.
     */
    @Input
    public boolean shouldThrowException = true;

    /**
     * Native library suffix, either Windows or Linux style.
     */
    private String nativeLibrarySuffix;

    /**
     * Validated search paths from the additional search paths;
     */
    private final Map<String, String> searchPathModifier = new HashMap<>();

    /**
     * Default constructor.
     */
    public NativeLibraryLoaderChecker() {
        super();
    }

    private void initialize() {
        // Don't support mac os
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac os")) {
            throw new GradleException("mac OS is not supported");
        }

        // Check for valid search paths
        var validAdditionalSearchPaths = new ArrayList<String>();
        if (additionalSearchPaths != null) {
            for (var path : additionalSearchPaths) {
                var file = new File(path);
                if (file.exists() && file.isDirectory()) {
                    validAdditionalSearchPaths.add(file.getAbsolutePath());
                }
            }
        }

        // Set the native suffix and conditionally insert additional search paths based on library.
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            nativeLibrarySuffix = ".dll";

            if (!validAdditionalSearchPaths.isEmpty()) {
                var joinedModifier = String.join(File.pathSeparator, validAdditionalSearchPaths);
                searchPathModifier.put("PATH", joinedModifier);
            }
        } else {
            nativeLibrarySuffix = ".so";

            if (!validAdditionalSearchPaths.isEmpty()) {
                var joinedModifier = String.join(File.pathSeparator, validAdditionalSearchPaths);
                searchPathModifier.put("LD_LIBRARY_PATH", joinedModifier);
            }
        }
    }

    /**
     * The action performed by the task.
     */
    @TaskAction
    private void checkLibraries() {
        initialize();
        // Don't do anything if libraries haven't been provided.
        if (librariesToCheck == null || librariesToCheck.isEmpty()) {
            throw new GradleException("NativeLibraryLoaderChecker needs something to check");
        }

        var couldNotLoadList = new ArrayList<String>();

        for (var library : librariesToCheck) {
            if (library != null && !library.isEmpty()) {
                // Filter out bad values. System.loadLibrary() won't accept file suffixes or paths of any kind.
                var lowerCaseName = library.toLowerCase(Locale.ROOT);
                if (lowerCaseName.contains(nativeLibrarySuffix)) {
                    throw new IllegalArgumentException("Remove the suffix from the library name called " + library);
                }
                if (lowerCaseName.contains("\\") || lowerCaseName.contains("/")) {
                    throw new IllegalArgumentException("Remove path from the library name called " + library);
                }

                // Now, try to load the libraries
                boolean loadLibrary = doAttemptToLoadLibrary(library);
                if (!loadLibrary) {
                    couldNotLoadList.add(library);
                }
            }
        }

        // If something couldn't be loaded, present the message to the user.
        if (!couldNotLoadList.isEmpty()) {
            var libraryList = String.join("\n    ", couldNotLoadList);
            var message = "Could not load or find all of the dependencies for the following libraries:\n"
                    + libraryList;
            if (shouldThrowException) {
                throw new GradleException(message);
            } else {
                getLogger().error(message);
            }
        }
    }

    /**
     * Attempts to load the library.  This process is operating system dependent. Refer to the OS's library load pathing
     * hierarchy if this method returns false.
     *
     * @param libraryName The name of the library.
     * @return True if the library AND ALL of its dependencies were found, false otherwise.
     */
    private boolean doAttemptToLoadLibrary(String libraryName) {
        try {
            // Resolves to the path of the buildSrc jar created by gradle in its cache folder
            var classPath = new File(MainLibraryChecker.class.getProtectionDomain().getCodeSource()
                    .getLocation().getPath()).getAbsolutePath();
            // Name of the path, fully qualified by package.
            var className = MainLibraryChecker.class.getName();
            ProcessBuilder processBuilder = new ProcessBuilder(
                    System.getProperty("java.home") + "/bin/java",
                    "-cp", classPath,
                    className, libraryName)
                    .inheritIO();
            // Add additional search paths to the environment variables of the process.
            processBuilder.environment().putAll(searchPathModifier);
            Process process = processBuilder.start();
            var exitCode = process.waitFor();
            return exitCode >= 0;
        } catch (Exception e) {
            return false;
        }
    }
}
