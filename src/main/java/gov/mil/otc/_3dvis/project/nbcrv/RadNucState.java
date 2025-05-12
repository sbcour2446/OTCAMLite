package gov.mil.otc._3dvis.project.nbcrv;

import gov.mil.otc._3dvis.datamodel.timed.TimedData;
import gov.mil.otc._3dvis.utility.Utility;

public class RadNucState extends TimedData {

    private final int quadrant;
    private final double q1Measurement;
    private final String q1Criticality;
    private final double q2Measurement;
    private final String q2Criticality;
    private final double q3Measurement;
    private final String q3Criticality;
    private final double q4Measurement;
    private final String q4Criticality;

    public RadNucState(long timestamp, int quadrant, double q1Measurement, String q1Criticality,
                       double q2Measurement, String q2Criticality, double q3Measurement,
                       String q3Criticality, double q4Measurement, String q4Criticality) {
        super(timestamp);

        this.quadrant = quadrant;
        this.q1Measurement = q1Measurement;
        this.q1Criticality = q1Criticality;
        this.q2Measurement = q2Measurement;
        this.q2Criticality = q2Criticality;
        this.q3Measurement = q3Measurement;
        this.q3Criticality = q3Criticality;
        this.q4Measurement = q4Measurement;
        this.q4Criticality = q4Criticality;
    }

    public int getQuadrant() {
        return quadrant;
    }

    public double getQ1Measurement() {
        return q1Measurement;
    }

    public String getQ1Criticality() {
        return q1Criticality;
    }

    public double getQ2Measurement() {
        return q2Measurement;
    }

    public String getQ2Criticality() {
        return q2Criticality;
    }

    public double getQ3Measurement() {
        return q3Measurement;
    }

    public String getQ3Criticality() {
        return q3Criticality;
    }

    public double getQ4Measurement() {
        return q4Measurement;
    }

    public String getQ4Criticality() {
        return q4Criticality;
    }
}
