package gov.mil.otc._3dvis.data.jbcp;

import java.util.Objects;

/**
 * Class representation of a row entry from the UTO file.
 */
public class UtoRecord {

    /**
     * The URN.
     */
    private final int urn;

    /**
     * The name.
     */
    private final String name;

    /**
     * The symbol.
     */
    private final String milStd2525Symbol;

    /**
     * Private constructor.
     *
     * @param urn              The URN of the record from the column.
     * @param name             The name of the entry in the UTO.
     * @param milStd2525Symbol The symbol code of the entry in the UTO.
     */
    UtoRecord(int urn, String name, String milStd2525Symbol) {
        this.urn = urn;
        this.name = name;
        this.milStd2525Symbol = milStd2525Symbol;
    }

    /**
     * Gets the URN.
     *
     * @return The URN.
     */
    public int getUrn() {
        return urn;
    }

    /**
     * Gets the name.
     *
     * @return The name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the symbol.
     *
     * @return The symbol.
     */
    public String getMilStd2525Symbol() {
        return milStd2525Symbol;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(urn, name, milStd2525Symbol);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UtoRecord utoRecord = (UtoRecord) o;
        return urn == utoRecord.urn
                && Objects.equals(name, utoRecord.name)
                && Objects.equals(milStd2525Symbol, utoRecord.milStd2525Symbol);
    }
}
