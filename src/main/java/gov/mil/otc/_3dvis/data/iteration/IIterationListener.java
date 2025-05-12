package gov.mil.otc._3dvis.data.iteration;

import java.util.List;

public interface IIterationListener {

    void onIterationUpdate(List<Iteration> iterationList);
}
