package gov.mil.otc._3dvis.tena;

import TENA.GeocentricPosition.ImmutableLocalClass;
import TENA.LVC.Entity.SDOpointer;
import TENA.Middleware.ObjectID;
import TENA.UnsignedByte;
import TENA.UnsignedShort;
import gov.nasa.worldwind.geom.Position;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class with common TENA objects for consistency.
 */
public class TenaUtility {

    public static final long MILLI_NANO = 1000000;

    /**
     * The site ID of a entity that is undefined.
     */
    public static final UnsignedShort UNKNOWN_SITE_ID = UnsignedShort.valueOf(65534);

    /**
     * The app ID of a entity that is undefined.
     */
    public static final UnsignedShort UNKNOWN_APP_ID = UnsignedShort.valueOf(65534);

    /**
     * The object ID of a entity that is undefined.
     */
    public static final UnsignedShort UNKNOWN_ENTITY_ID = UnsignedShort.valueOf(65533);

    /**
     * The event ID of a entity that is undefined.
     */
    public static final UnsignedShort UNKNOWN_EVENT_ID = UnsignedShort.valueOf(65533);

    private static final ObjectID UNKNOWN_OBJECT_ID = new SDOpointer().getObjectID();

    private TenaUtility() {
    }

    /**
     * Creates an unknown Entity Reference.
     *
     * @return The unknown Entity Reference.
     */
    public static TENA.LVC.EntityReference.LocalClass createUnknownEntityReference() {
        return TENA.LVC.EntityReference.LocalClass.create(new SDOpointer(), createUnknownEntityId());
    }

    /**
     * Creates an unknown Event ID. This is to be used until TENA fixes the
     * issue of not allowing a "0" ID.
     *
     * @return The unknown Event ID.
     */
    public static TENA.LVC.EventID.LocalClass createUnknownEventId() {
        try {
            return TENA.LVC.EventID.LocalClass.create(UNKNOWN_SITE_ID, UNKNOWN_APP_ID, UNKNOWN_EVENT_ID);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return null;
    }

    /**
     * Creates an unknown Entity ID. This is to be used until TENA fixes the
     * issue of not allowing a "0" ID.
     *
     * @return The unknown Entity ID.
     */
    public static TENA.LVC.EntityID.LocalClass createUnknownEntityId() {
        try {
            return TENA.LVC.EntityID.LocalClass.create(UNKNOWN_SITE_ID, UNKNOWN_APP_ID, UNKNOWN_ENTITY_ID);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return null;
    }

    /**
     * Creates an unknown TSPI object.
     *
     * @return The unknown TSPI.
     */
    public static TENA.TSPI.LocalClass createUnknownTspi() {
        try {
            return TENA.TSPI.LocalClass.create(
                    TENA.Time.LocalClass.create(0),
                    TENA.Position.LocalClass.create(
                            TENA.GeocentricPosition.LocalClass.create(
                                    TENA.GeocentricSRF.LocalClass.create(),
                                    0, 0, 0)));
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return null;
    }

    /**
     * Compares two Entity IDs (e.g., '1:6:111' -vs- '1:8:333').
     *
     * @param id1 First Entity ID.
     * @param id2 Second Entity ID.
     * @return True if the Entity ID contents are the same.
     */
    public static boolean isEqual(TENA.LVC.EntityID.ImmutableLocalClass id1,
                                  TENA.LVC.EntityID.ImmutableLocalClass id2) {
        return (id1.get_siteID().intValue() == id2.get_siteID().intValue())
                && (id1.get_applicationID().intValue() == id2.get_applicationID().intValue())
                && (id1.get_objectID().intValue() == id2.get_objectID().intValue());
    }

    /**
     * Compares two Event IDs (e.g., '1:1:1' -vs- '1:2:3').
     *
     * @param id1 First Event ID.
     * @param id2 Second Event ID.
     * @return True if the Event ID contents are the same.
     */
    public static boolean isEqual(TENA.LVC.EventID.ImmutableLocalClass id1,
                                  TENA.LVC.EventID.ImmutableLocalClass id2) {
        return (id1.get_siteID().intValue() == id2.get_siteID().intValue())
                && (id1.get_applicationID().intValue() == id2.get_applicationID().intValue())
                && (id1.get_eventID().intValue() == id2.get_eventID().intValue());
    }

    /**
     * Compares two Entity Types (e.g., '2:9:225:1:2:3:4' -vs-
     * '1:8:222:4:3:2:1').
     *
     * @param type1 First Entity Type.
     * @param type2 Second Entity Type.
     * @return True if the Entity Type contents are the same.
     */
    public static boolean isEqual(TENA.LVC.EntityType.ImmutableLocalClass type1,
                                  TENA.LVC.EntityType.ImmutableLocalClass type2) {
        return (type1.get_kind().intValue() == type2.get_kind().intValue())
                && (type1.get_domain().intValue() == type2.get_domain().intValue())
                && (type1.get_country().intValue() == type2.get_country().intValue())
                && (type1.get_category().intValue() == type2.get_category().intValue())
                && (type1.get_subcategory().intValue() == type2.get_subcategory().intValue())
                && (type1.get_specific().intValue() == type2.get_specific().intValue())
                && (type1.get_extra().intValue() == type2.get_extra().intValue());
    }

    /**
     * Compares the Munition Fire Event ID with the unknown ID values.
     *
     * @param eventId The Munition Fire Entity ID.
     * @return True if the Event ID is an unknown ID.
     */
    public static boolean isUnknown(TENA.LVC.Engagement.MunitionFireEventID.ImmutableLocalClass eventId) {
        return eventId.get_siteID().equals(UNKNOWN_SITE_ID)
                && eventId.get_applicationID().equals(UNKNOWN_APP_ID)
                && eventId.get_eventID().equals(UNKNOWN_EVENT_ID);
    }

    /**
     * Compares the Munition Detonation Event ID with the unknown ID values.
     *
     * @param eventId The Munition Detonation Entity ID.
     * @return True if the Event ID is an unknown ID.
     */
    public static boolean isUnknown(TENA.LVC.Engagement.MunitionDetonationEventID.ImmutableLocalClass eventId) {
        return eventId.get_siteID().equals(UNKNOWN_SITE_ID)
                && eventId.get_applicationID().equals(UNKNOWN_APP_ID)
                && eventId.get_eventID().equals(UNKNOWN_EVENT_ID);
    }

    /**
     * Compares the Engagement Results Event ID with the unknown ID values.
     *
     * @param eventId The Engagement Results Entity ID.
     * @return True if the Event ID is an unknown ID.
     */
    public static boolean isUnknown(TENA.LVC.Engagement.ResultsEventID.ImmutableLocalClass eventId) {
        return eventId.get_siteID().equals(UNKNOWN_SITE_ID)
                && eventId.get_applicationID().equals(UNKNOWN_APP_ID)
                && eventId.get_eventID().equals(UNKNOWN_EVENT_ID);
    }

    /**
     * Checks if Entity reference is an unknown reference.
     *
     * @param entityReference The Entity reference.
     * @return True if the Entity reference is an unknown ID.
     */
    public static boolean isUnknown(TENA.LVC.EntityReference.ImmutableLocalClass entityReference) {
        return entityReference.get_entity().getObjectID().equals(UNKNOWN_OBJECT_ID);
    }

    /**
     * Compares the Entity ID with the unknown ID values.
     *
     * @param entityId The Entity ID.
     * @return True if the Entity ID is an unknown ID.
     */
    public static boolean isUnknown(TENA.LVC.EntityID.ImmutableLocalClass entityId) {
        return entityId.get_siteID().equals(UNKNOWN_SITE_ID)
                && entityId.get_applicationID().equals(UNKNOWN_APP_ID)
                && entityId.get_objectID().equals(UNKNOWN_ENTITY_ID);
    }

    /**
     * Compares the EntityType with the unknown EntityType values
     * "0.0.0.0.0.0.0".
     *
     * @param entityType The EntityType.
     * @return True if the EntityType is an unknown EntityType.
     */
    public static boolean isUnknown(TENA.LVC.EntityType.ImmutableLocalClass entityType) {
        return entityType.get_kind().equals(UnsignedByte.ZERO)
                && entityType.get_domain().equals(UnsignedByte.ZERO)
                && entityType.get_country().equals(UnsignedShort.ZERO)
                && entityType.get_category().equals(UnsignedByte.ZERO)
                && entityType.get_subcategory().equals(UnsignedByte.ZERO)
                && entityType.get_specific().equals(UnsignedByte.ZERO)
                && entityType.get_extra().equals(UnsignedByte.ZERO);
    }

    /**
     * Validate Position against all zeros
     *
     * @param tspi TENA TSPI Object
     * @return True if valid (!0,0,0) otherwise false
     */
    public static boolean isValidTspi(TENA.TSPI.ImmutableLocalClass tspi) {
        if (tspi == null) {
            return false;
        }
        ImmutableLocalClass position = getGeocentricPosition(tspi.get_position());
        if (position == null) {
            return false;
        }
        return !(Double.compare(position.get_xInMeters(), 0) == 0
                && Double.compare(position.get_yInMeters(), 0) == 0
                && Double.compare(position.get_zInMeters(), 0) == 0);
    }

    /**
     * Creates an unknown DIS Entity Type.
     *
     * @return The unknown DIS Entity Type.
     */
    public static TENA.LVC.EntityType.LocalClass createUnknownEntityType() {
        try {
            return TENA.LVC.EntityType.LocalClass.create(
                    UnsignedByte.ZERO,
                    UnsignedByte.ZERO,
                    UnsignedShort.ZERO,
                    UnsignedByte.ZERO,
                    UnsignedByte.ZERO,
                    UnsignedByte.ZERO,
                    UnsignedByte.ZERO);
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return null;
    }

    /**
     * Creates a DIS Event ID from a formatted string "1:23:456".
     *
     * @param eventIdString The event ID string.
     * @return The TENA.LVC.EventID.
     */
    public static TENA.LVC.EventID.LocalClass createEventIdFromString(String eventIdString) {
        TENA.LVC.EventID.LocalClass results = null;

        try {
            String[] fields = eventIdString.split(":");
            if (fields.length == 3) {
                results = TENA.LVC.EventID.LocalClass.create(
                        UnsignedShort.valueOf(fields[0]),
                        UnsignedShort.valueOf(fields[1]),
                        UnsignedShort.valueOf(fields[2]));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        if (results == null) {
            results = TenaUtility.createUnknownEventId();
        }

        return results;
    }

    /**
     * Creates a DIS Entity ID from a formatted string "1:23:456".
     *
     * @param entityIdString The entity ID string.
     * @return The TENA.LVC.EntityID.
     */
    public static TENA.LVC.EntityID.LocalClass createEntityIdFromString(String entityIdString) {
        TENA.LVC.EntityID.LocalClass results = null;

        try {
            String[] fields = entityIdString.split(":");
            if (fields.length == 3) {
                results = TENA.LVC.EntityID.LocalClass.create(
                        UnsignedShort.valueOf(fields[0]),
                        UnsignedShort.valueOf(fields[1]),
                        UnsignedShort.valueOf(fields[2]));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        if (results == null) {
            results = TenaUtility.createUnknownEntityId();
        }

        return results;
    }

    /**
     * Creates a DIS Entity Type from a formatted string "0.0.0.0.0.0.0".
     *
     * @param entityTypeString The entity type string.
     * @return The TENA.LVC.EntityType.
     */
    public static TENA.LVC.EntityType.LocalClass createEntityTypeFromString(String entityTypeString) {
        TENA.LVC.EntityType.LocalClass results = null;

        try {
            String[] fields = entityTypeString.split("\\.");
            if (fields.length == 7) {
                results = TENA.LVC.EntityType.LocalClass.create(
                        UnsignedByte.valueOf(Integer.parseUnsignedInt(fields[0])),
                        UnsignedByte.valueOf(Integer.parseUnsignedInt(fields[1])),
                        UnsignedShort.valueOf(Integer.parseUnsignedInt(fields[2])),
                        UnsignedByte.valueOf(Integer.parseUnsignedInt(fields[3])),
                        UnsignedByte.valueOf(Integer.parseUnsignedInt(fields[4])),
                        UnsignedByte.valueOf(Integer.parseUnsignedInt(fields[5])),
                        UnsignedByte.valueOf(Integer.parseUnsignedInt(fields[6])));
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        if (results == null) {
            results = TenaUtility.createUnknownEntityType();
        }

        return results;
    }

    public static Position convertTenaPosition(TENA.Position.ImmutableLocalClass tenaPosition) {
        TENA.GeodeticPosition.ImmutableLocalClass geodeticPosition = getGeodeticPosition(tenaPosition);
        if (geodeticPosition != null) {
            return Position.fromDegrees(geodeticPosition.get_latitudeInDegrees(),
                    geodeticPosition.get_longitudeInDegrees(),
                    geodeticPosition.get_heightAboveEllipsoidInMeters());
        } else {
            return null;
        }
    }

    /**
     * Get the position as Geodetic reference.
     *
     * @param position The position.
     * @return The GeodeticPosition.
     */
    public static TENA.GeodeticPosition.ImmutableLocalClass getGeodeticPosition(TENA.Position.ImmutableLocalClass position) {
        TENA.GeodeticPosition.ImmutableLocalClass result = null;
        if (position != null) {
            if (position.is_geodetic_asTransmitted_set()) {
                result = position.get_geodetic_asTransmitted();
            } else if (position.is_geocentric_asTransmitted_set()) {
                if (isGeocentricValid(position.get_geocentric_asTransmitted())) {
                    try {
                        TENA.GeodeticSRF.LocalClass srf = TENA.GeodeticSRF.LocalClass.create();
                        result = TENA.GeodeticPosition.LocalClass.create(
                                srf, position);
                    } catch (Exception e) {
                        Logger.getGlobal().log(Level.WARNING, String.format("Unable to convert position: %s", formatPosition(position)), e);
                    }
                } else {
                    String message = String.format("Unable to convert position: %s", formatPosition(position));
                    Logger.getGlobal().log(Level.WARNING, message);
                }
            }
        }
        return result;
    }

    /**
     * Validate Geocentric Position.  Check for any values near zero.
     *
     * @param geocentricPosition The Geocentric Position.
     * @return True if valid, otherwise false.
     */
    private static boolean isGeocentricValid(TENA.GeocentricPosition.ImmutableLocalClass geocentricPosition) {
        return !((int) geocentricPosition.get_xInMeters() == 0 ||
                (int) geocentricPosition.get_yInMeters() == 0 ||
                (int) geocentricPosition.get_zInMeters() == 0);
    }

    /**
     * Convert to geocentric position.
     *
     * @param position TENA.Position.ImmutableLocalClass
     * @return TENA.GeocentricPosition.ImmutableLocalClass
     */
    public static TENA.GeocentricPosition.ImmutableLocalClass getGeocentricPosition(TENA.Position.ImmutableLocalClass position) {
        TENA.GeocentricPosition.ImmutableLocalClass result = null;
        if (position != null) {
            if (position.is_geocentric_asTransmitted_set()) {
                result = position.get_geocentric_asTransmitted();
            } else {
                try {
                    TENA.GeocentricSRF.LocalClass srf = TENA.GeocentricSRF.LocalClass.create();
                    result = TENA.GeocentricPosition.LocalClass.create(
                            srf, position);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "Unable to convert position", e);
                }
            }
        }
        return result;
    }

    /**
     * Convert to geocentric velocity.
     *
     * @param velocity TENA.Velocity.ImmutableLocalClass
     * @return TENA.GeocentricVelocity.ImmutableLocalClass
     */
    public static TENA.GeocentricVelocity.ImmutableLocalClass getGeocentricVelocity(TENA.Velocity.ImmutableLocalClass velocity) {
        TENA.GeocentricVelocity.ImmutableLocalClass result = null;
        if (velocity != null) {
            if (velocity.is_geocentric_asTransmitted_set()) {
                result = velocity.get_geocentric_asTransmitted();
            } else {
                try {
                    TENA.GeocentricSRF.LocalClass srf = TENA.GeocentricSRF.LocalClass.create();
                    result = TENA.GeocentricVelocity.LocalClass.create(
                            srf, velocity);
                } catch (Exception e) {
                    Logger.getGlobal().log(Level.WARNING, "Unable to convert velocity", e);
                }
            }
        }
        return result;
    }

    /**
     * Convert to DIS orientation or geocentric orientation with
     * WGS_1984_IDENTITY SRF.
     *
     * @param orientation TENA.Orientation.ImmutableLocalClass
     * @return TENA.DISorientation.ImmutableLocalClass
     */
    public static TENA.DISorientation.ImmutableLocalClass getDisOrientation(TENA.Orientation.ImmutableLocalClass orientation) {
        TENA.DISorientation.ImmutableLocalClass result;
        if (orientation != null) {
            result = TENA.DISorientation.LocalClass.create(orientation);
        } else {
            result = null;
        }
        return result;
    }

    /**
     * Format the position for logging.
     *
     * @param position The position.
     * @return The formatted string.
     */
    private static String formatPosition(TENA.Position.ImmutableLocalClass position) {
        if (position.is_geodetic_asTransmitted_set()) {
            return String.format("lat:%f lon:%f hae:%f",
                    position.get_geodetic_asTransmitted().get_latitudeInDegrees(),
                    position.get_geodetic_asTransmitted().get_longitudeInDegrees(),
                    position.get_geodetic_asTransmitted().get_heightAboveEllipsoidInMeters());
        } else if (position.is_geocentric_asTransmitted_set()) {
            return String.format("x:%f y:%f z:%f",
                    position.get_geocentric_asTransmitted().get_xInMeters(),
                    position.get_geocentric_asTransmitted().get_yInMeters(),
                    position.get_geocentric_asTransmitted().get_zInMeters());
        } else if (position.is_lstp_asTransmitted_set()) {
            return String.format("lstp az:%f el:%f range:%f",
                    position.get_lstp_asTransmitted().get_azimuthInRadians(),
                    position.get_lstp_asTransmitted().get_elevationInRadians(),
                    position.get_lstp_asTransmitted().get_rangeInMeters());
        } else if (position.is_ltpENU_asTransmitted_set()) {
            return String.format("ltpENU x:%f y:%f z:%f",
                    position.get_ltpENU_asTransmitted().get_xInMeters(),
                    position.get_ltpENU_asTransmitted().get_yInMeters(),
                    position.get_ltpENU_asTransmitted().get_zInMeters());
        } else {
            return "could not format position";
        }
    }
}
