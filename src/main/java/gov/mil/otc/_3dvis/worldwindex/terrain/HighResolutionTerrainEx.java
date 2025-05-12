package gov.mil.otc._3dvis.worldwindex.terrain;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.terrain.HighResolutionTerrain;

import java.util.List;

public class HighResolutionTerrainEx extends HighResolutionTerrain {

    public HighResolutionTerrainEx(Globe globe, Double targetResolution) {
        super(globe, targetResolution);
    }

    public HighResolutionTerrainEx(Globe globe, Sector sector, Double targetResolution, Double verticalExaggeration) {
        super(globe, sector, targetResolution, verticalExaggeration);
    }

    @Override
    protected void getElevations(Sector sector, List<LatLon> latlons, double[] targetResolution, double[] elevations)
            throws InterruptedException {
        if (useCachedElevationsOnly) {
            getCachedElevations(latlons, elevations);
            return;
        }

        double[] actualResolution = globe.getElevations(sector, latlons, targetResolution, elevations);

        while (!resolutionsMeetCriteria(actualResolution, targetResolution)) {
            // Give the system a chance to retrieve data from the disk cache or the server. Also catches interrupts
            // and throws interrupt exceptions.
            Thread.yield();

            if (startTime.get() != null && timeout != null &&
                    (System.currentTimeMillis() - startTime.get()) > timeout) {
                break;
            }

            actualResolution = globe.getElevations(sector, latlons, targetResolution, elevations);
        }
    }
}
