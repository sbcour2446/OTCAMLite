package gov.mil.otc._3dvis.datamodel;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility to manage queries to the entity_type database.
 */
public class EntityTypeUtility {

    private static final String COLUMN_NAME_KIND = "kind";
    private static final String COLUMN_NAME_DOMAIN = "domain";
    private static final String COLUMN_NAME_COUNTRY = "country";
    private static final String COLUMN_NAME_CATEGORY = "category";
    private static final String COLUMN_NAME_SUBCATEGORY = "subcategory";
    private static final String COLUMN_NAME_SPECIFIC = "specific";
    private static final String COLUMN_NAME_EXTRA = "extra";
    private static final String COLUMN_NAME_DESCRIPTION = "description";
    private static final String UNKNOWN_NULL = "Unknown: null";
    private static final Map<String, String> DESCRIPTIONS = new ConcurrentHashMap<>();

    /**
     * Entity Type Format String
     */
    private static final String ENTITY_TYPE_FORMAT = "%d.%d.%d.%d.%d.%d.%d";

    /**
     * The entity type database name.
     */
    private static final String DATABASE_NAME = "/entity_type.db";

    /**
     * The database URL.
     */
    private static String databaseUrl = null;

    /**
     * The initialization flag that is true when this class is initialized.
     */
    private static boolean initialized = false;

    /**
     * The shutdown flag that is true when this class is shutdown.
     */
    private static boolean shutdown = false;

    private static final Object shutdownMutex = new Object();

    /**
     * The database connection through language drivers.
     */
    private static Connection databaseConnection = null;

    /**
     * Constructor.
     */
    private EntityTypeUtility() {
    }

    /**
     * Gets the database connection.
     *
     * @return The database connection.
     * @throws SQLException If a connection could not be created with the SQLite database.
     */
    private static synchronized Connection getConnection() throws SQLException {
        if (shutdown) {
            return null;
        }
        if (!initialized) {
            initialize();
        }
        if (databaseConnection == null || databaseConnection.isClosed()) {
            databaseConnection = DriverManager.getConnection(databaseUrl);
        }
        return databaseConnection;
    }

    /**
     * Closes the database connection if its open.
     */
    public static synchronized void closeDatabase() {
        try {
            if (databaseConnection != null && !databaseConnection.isClosed()) {
                databaseConnection.close();
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Could not close the entity type database", e);
        }
    }

    /**
     * Copys a database from an input stream to a file.
     *
     * @param source       The input stream.
     * @param databaseFile The file to save the input stream to.
     * @return True if successful, false otherwise.
     */
    private static boolean copyDatabase(InputStream source, File databaseFile) {
        try (OutputStream outputStream = new FileOutputStream(databaseFile)) {
            outputStream.write(source.readAllBytes());
        } catch (IOException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
            return false;
        }
        return true;
    }

    /**
     * Initializes this object.
     *
     * @return True if the initialization occurred successfully.
     */
    public static synchronized boolean initialize() {
        if (databaseUrl == null) {
            File databasePathFile = new File(System.getProperty("user.home") + File.separator +
                    ".3dvis" + File.separator + "database");
            if (!databasePathFile.exists() && !databasePathFile.mkdirs()) {
                return false;
            }

            File databaseFile = new File(databasePathFile.getAbsolutePath() + DATABASE_NAME);
            if (!databaseFile.exists()) {
                try (InputStream inputStream = EntityTypeUtility.class.getResourceAsStream(DATABASE_NAME)) {
                    if (inputStream == null || !copyDatabase(inputStream, databaseFile)) {
                        return false;
                    }
                } catch (IOException e) {
                    Logger.getGlobal().log(Level.WARNING, null, e);
                    return false;
                }
            }

            databaseUrl = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
        }

        initialized = true;
        return true;
    }

    public static void shutdown() {
        shutdown = true;
        synchronized (shutdownMutex) {
            closeDatabase();
        }
    }

    public static boolean isShutdown() {
        return shutdown;
    }

    public static List<EntityType> getEntityTypes() {
        List<EntityType> results = new ArrayList<>();

        String sql = String.format("SELECT * FROM entity_type ORDER BY %s,%s,%s,%s,%s,%s,%s",
                COLUMN_NAME_KIND,
                COLUMN_NAME_DOMAIN,
                COLUMN_NAME_COUNTRY,
                COLUMN_NAME_CATEGORY,
                COLUMN_NAME_SUBCATEGORY,
                COLUMN_NAME_SPECIFIC,
                COLUMN_NAME_EXTRA);

        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                while (resultSet.next()) {
                    EntityType entityType = new EntityType(
                            resultSet.getInt(COLUMN_NAME_KIND),
                            resultSet.getInt(COLUMN_NAME_DOMAIN),
                            resultSet.getInt(COLUMN_NAME_COUNTRY),
                            resultSet.getInt(COLUMN_NAME_CATEGORY),
                            resultSet.getInt(COLUMN_NAME_SUBCATEGORY),
                            resultSet.getInt(COLUMN_NAME_SPECIFIC),
                            resultSet.getInt(COLUMN_NAME_EXTRA));
                    results.add(entityType);
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }

        return results;
    }

    /**
     * Gets a map of all the Entity Kinds and its description.
     *
     * @return The Entity Kind - description map.
     */
    public static Map<Integer, String> getKinds() {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM kind")) {
                while (resultSet.next()) {
                    int kind = resultSet.getInt(COLUMN_NAME_KIND);
                    results.put(kind, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    /**
     * Gets a map of all the Domains with its description for the provided Kind.
     *
     * @param kind The Entity Kind.
     * @return The Domain - description map.
     */
    public static Map<Integer, String> getDomains(int kind) {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM domain WHERE kind=?")) {
                preparedStatement.setInt(1, kind);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int domain = resultSet.getInt(COLUMN_NAME_DOMAIN);
                        results.put(domain, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    /**
     * Gets a map of all the Countries with its description.
     *
     * @return The Country - description map.
     */
    public static Map<Integer, String> getCountries() {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            try (ResultSet resultSet = connection.createStatement().executeQuery("SELECT * FROM country")) {
                while (resultSet.next()) {
                    int country = resultSet.getInt(COLUMN_NAME_COUNTRY);
                    results.put(country, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    /**
     * Gets a map of all the Categories with its description for the provided Kind, Domain.
     *
     * @param kind   The Entity Kind.
     * @param domain The Domain.
     * @return The Category - description map.
     */
    public static Map<Integer, String> getCategories(int kind, int domain) {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            try (PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM category WHERE kind=? AND domain=?")) {
                preparedStatement.setInt(1, kind);
                preparedStatement.setInt(2, domain);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        int category = resultSet.getInt(COLUMN_NAME_CATEGORY);
                        results.put(category, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    /**
     * Gets a map of all the Subcategories with its description for the provided Kind, Domain, Country, Category.
     *
     * @param kind     The Entity Kind.
     * @param domain   The Domain.
     * @param country  The Country.
     * @param category The Category.
     * @return The Subcategory - description map.
     */
    public static Map<Integer, String> getSubcategories(int kind, int domain, int country, int category) {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            if (country > 0) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM subcategory WHERE kind=? AND domain=? AND country=? AND category=?")) {
                    preparedStatement.setInt(1, kind);
                    preparedStatement.setInt(2, domain);
                    preparedStatement.setInt(3, country);
                    preparedStatement.setInt(4, category);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int subcategory = resultSet.getInt(COLUMN_NAME_SUBCATEGORY);
                            results.put(subcategory, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                        }
                    }
                }
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM subcategory WHERE kind=? AND domain=? AND category=?")) {
                    preparedStatement.setInt(1, kind);
                    preparedStatement.setInt(2, domain);
                    preparedStatement.setInt(3, category);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int subcategory = resultSet.getInt(COLUMN_NAME_SUBCATEGORY);
                            results.put(subcategory, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    /**
     * Gets a map of all the Specifics with its description for the provided Kind, Domain, Country, Category, Subcategory.
     *
     * @param kind        The Entity Kind.
     * @param domain      The Domain.
     * @param country     The Country.
     * @param category    The Category.
     * @param subcategory The Subcategory.
     * @return The Specific - description map.
     */
    public static Map<Integer, String> getSpecifics(int kind, int domain, int country, int category, int subcategory) {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            if (country > 0) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM specific WHERE kind=? AND domain=? AND country=? AND category=? AND subcategory=?")) {
                    preparedStatement.setInt(1, kind);
                    preparedStatement.setInt(2, domain);
                    preparedStatement.setInt(3, country);
                    preparedStatement.setInt(4, category);
                    preparedStatement.setInt(5, subcategory);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int specific = resultSet.getInt(COLUMN_NAME_SPECIFIC);
                            results.put(specific, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                        }
                    }
                }
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM specific WHERE kind=? AND domain=? AND category=? AND subcategory=?")) {
                    preparedStatement.setInt(1, kind);
                    preparedStatement.setInt(2, domain);
                    preparedStatement.setInt(3, category);
                    preparedStatement.setInt(4, subcategory);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int specific = resultSet.getInt(COLUMN_NAME_SPECIFIC);
                            results.put(specific, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    /**
     * Gets a map of all the Extras with its description for the provided Kind, Domain, Country, Category, Subcategory, Specific.
     *
     * @param kind        The Entity Kind.
     * @param domain      The Domain.
     * @param country     The Country.
     * @param category    The Category.
     * @param subcategory The Subcategory.
     * @param specific    The Specific.
     * @return The Extra - description map.
     */
    public static Map<Integer, String> getExtras(int kind, int domain, int country, int category, int subcategory, int specific) {
        Map<Integer, String> results = new LinkedHashMap<>();

        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            if (country > 0) {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM extra WHERE kind=? AND domain=? AND country=? AND category=? AND subcategory=? AND specific=?")) {
                    preparedStatement.setInt(1, kind);
                    preparedStatement.setInt(2, domain);
                    preparedStatement.setInt(3, country);
                    preparedStatement.setInt(4, category);
                    preparedStatement.setInt(5, subcategory);
                    preparedStatement.setInt(6, specific);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int extra = resultSet.getInt(COLUMN_NAME_EXTRA);
                            results.put(extra, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                        }
                    }
                }
            } else {
                try (PreparedStatement preparedStatement = connection.prepareStatement(
                        "SELECT * FROM extra WHERE kind=? AND domain=? AND category=? AND subcategory=? AND specific=?")) {
                    preparedStatement.setInt(1, kind);
                    preparedStatement.setInt(2, domain);
                    preparedStatement.setInt(3, category);
                    preparedStatement.setInt(4, subcategory);
                    preparedStatement.setInt(5, specific);
                    try (ResultSet resultSet = preparedStatement.executeQuery()) {
                        while (resultSet.next()) {
                            int extra = resultSet.getInt(COLUMN_NAME_EXTRA);
                            results.put(extra, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    public static Map<Integer, String> getDomainsWithQualified(int kind) {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            String sql = String.format(
                    "SELECT DISTINCT d.domain, d.description FROM entity_type e" +
                            " LEFT OUTER JOIN domain d ON d.kind=e.kind AND d.domain=e.domain" +
                            " WHERE e.kind=%d ORDER BY d.domain", kind);
            try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                while (resultSet.next()) {
                    int country = resultSet.getInt(COLUMN_NAME_DOMAIN);
                    results.put(country, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }

    public static Map<Integer, String> getCountriesWithQualified(int kind, int domain, int category, boolean alphabetize) {
        Map<Integer, String> results = new LinkedHashMap<>();
        try {
            Connection connection = getConnection();
            if (connection == null) {
                return results;
            }
            String sql = String.format(
                    "SELECT DISTINCT c.country, c.description FROM entity_type e" +
                            " LEFT OUTER JOIN country c ON c.country=e.country" +
                            " WHERE kind=%d AND domain=%d AND category=%d", kind, domain, category);
            if (alphabetize) {
                sql += " ORDER BY c.description";
            }
            try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                while (resultSet.next()) {
                    int country = resultSet.getInt(COLUMN_NAME_COUNTRY);
                    results.put(country, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, null, e);
        }
        return results;
    }


    public static Map<EntityType, String> getQualifiedEntityTypes(int kind, int domain, int country, int category) {
        Map<EntityType, String> results = new LinkedHashMap<>();

        String sql = String.format("SELECT * FROM entity_type WHERE kind=%d AND domain=%d AND country=%d AND category=%d" +
                        " ORDER BY kind, domain, country, category",
                kind, domain, country, category);

        synchronized (shutdownMutex) {
            try {
                Connection connection = getConnection();
                if (connection == null) {
                    return results;
                }
                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    while (!shutdown && resultSet.next()) {
                        EntityType entityType = new EntityType(
                                resultSet.getInt(COLUMN_NAME_KIND),
                                resultSet.getInt(COLUMN_NAME_DOMAIN),
                                resultSet.getInt(COLUMN_NAME_COUNTRY),
                                resultSet.getInt(COLUMN_NAME_CATEGORY),
                                resultSet.getInt(COLUMN_NAME_SUBCATEGORY),
                                resultSet.getInt(COLUMN_NAME_SPECIFIC),
                                resultSet.getInt(COLUMN_NAME_EXTRA));
                        results.put(entityType, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                    }
                }
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }

        return results;
    }

    /**
     * Create a Formatted Entity Type String from a TENA.LVC.EntityType Object
     *
     * @param entityType TENA.LVC.EntityType Object
     * @return a Formatted String in the form "1.2.345.6.7.8.9" or "Unknown"
     */
    public static String formatEntityType(TENA.LVC.EntityType.ImmutableLocalClass entityType) {
        return (entityType != null) ?
                String.format(ENTITY_TYPE_FORMAT,
                        entityType.get_kind().intValue(),
                        entityType.get_domain().intValue(),
                        entityType.get_country().intValue(),
                        entityType.get_category().intValue(),
                        entityType.get_subcategory().intValue(),
                        entityType.get_specific().intValue(),
                        entityType.get_extra().intValue())
                : "Unknown";
    }

    /**
     * Get a complete description (Entity Type Name & Entity Type) for a DIS EntityType.
     *
     * @param kind        The DIS EntityType Kind.
     * @param domain      The DIS EntityType Domain.
     * @param country     The DIS EntityType Country.
     * @param category    The DIS EntityType Category.
     * @param subcategory The DIS EntityType Sub Category.
     * @param specific    The DIS EntityType Specific.
     * @param extra       The DIS EntityType Extra.
     * @return The EntityType description in the form "M1A2 Bradley (2.1.225.1.2.3.4)".
     */
    public static String getFullDescription(int kind, int domain, int country, int category, int subcategory, int specific, int extra) {
        return getFullDescription(new EntityType(kind, domain, country, category, subcategory, specific, extra));
    }

    /**
     * Get a complete description (Entity Type Name & Entity Type) for a DIS EntityType.
     *
     * @param entityType The TENA.LVC.EntityType.
     * @return The EntityType description in the form "M1A2 Bradley (2.1.225.1.2.3.4)".
     */
    public static String getFullDescription(TENA.LVC.EntityType.ImmutableLocalClass entityType) {
        if (entityType == null) {
            return UNKNOWN_NULL;
        }

        String results = getDescription(entityType);

        if (results.contains("Unknown:")) {
            return results;
        }

        return String.format("%s (%s)", results, formatEntityType(entityType));
    }

    /**
     * Get a complete description (Entity Type Name & Entity Type) for a DIS EntityType.
     *
     * @param entityType The DISEntityType.
     * @return The EntityType description in the form "M1A2 Bradley (2.1.225.1.2.3.4)".
     */
    public static String getFullDescription(EntityType entityType) {
        if (entityType == null) {
            return UNKNOWN_NULL;
        }

        String results = getDescription(entityType);

        if (results.contains("Unknown:")) {
            return results;
        }

        return String.format("%s (%s)", results, entityType);
    }

    /**
     * Get the description for a DIS EntityType.
     *
     * @param kind        Entity Type Kind
     * @param domain      Entity Type Domain
     * @param country     Entity Type Country
     * @param category    Entity Type Category
     * @param subcategory Entity Type Sub Category
     * @param specific    Entity Type Specific
     * @param extra       Entity Type Extra
     * @return The EntityType description in the form "M1A2 Bradley" or "Unknown (2.1.225.1.2.3.4)".
     */
    public static String getDescription(int kind, int domain, int country, int category, int subcategory, int specific, int extra) {
        return getDescription(new EntityType(kind, domain, country, category, subcategory, specific, extra));
    }

    /**
     * Get the description for a DIS EntityType.
     *
     * @param entityType The TENA.LVC.EntityType.
     * @return The EntityType description in the form "M1A2 Bradley" or "Unknown (2.1.225.1.2.3.4)".
     */
    public static String getDescription(TENA.LVC.EntityType.ImmutableLocalClass entityType) {
        return getDescription(EntityType.fromTenaType(entityType));
    }

    /**
     * Get the description for a DIS EntityType.
     *
     * @param entityType The EntityType Object.
     * @return The EntityType description in the form "M1A2 Bradley" or "Unknown (2.1.225.1.2.3.4)".
     */
    public static String getDescription(EntityType entityType) {
        String results;

        if (entityType == null) {
            results = UNKNOWN_NULL;
        } else if (DESCRIPTIONS.containsKey(entityType.toString())) {
            results = DESCRIPTIONS.get(entityType.toString());
        } else {
            results = getDescriptionFromDatabase(entityType);
            if (results.isBlank()) {
                results = getDescriptionFromDatabaseWithoutCountry(entityType);
            }
            if (results.isBlank()) {
                results = String.format("Unknown: %s", entityType);
            }
            DESCRIPTIONS.put(entityType.toString(), results);
        }
        return results;
    }

    private static String getDescriptionFromDatabase(EntityType entityType) {
        String sql = String.format("SELECT description FROM entity_type WHERE"
                        + "   kind=%d" +
                        " AND domain=%d" +
                        " AND country=%d" +
                        " AND category=%d" +
                        " AND subcategory=%d" +
                        " AND specific=%d" +
                        " AND extra=%d",
                entityType.getKind(),
                entityType.getDomain(),
                entityType.getCountry(),
                entityType.getCategory(),
                entityType.getSubcategory(),
                entityType.getSpecific(),
                entityType.getExtra());

        synchronized (shutdownMutex) {
            try {
                Connection connection = getConnection();
                if (connection == null) {
                    return "";
                }
                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }
        return "";
    }

    private static String getDescriptionFromDatabaseWithoutCountry(EntityType entityType) {
        String results = "";
        String sql = String.format("SELECT description, country FROM entity_type WHERE"
                        + " kind=%d" +
                        " AND domain=%d" +
                        " AND category=%d" +
                        " AND subcategory=%d" +
                        " AND specific=%d" +
                        " AND extra=%d",
                entityType.getKind(),
                entityType.getDomain(),
                entityType.getCategory(),
                entityType.getSubcategory(),
                entityType.getSpecific(),
                entityType.getExtra());

        synchronized (shutdownMutex) {
            try {
                Connection connection = getConnection();
                if (connection == null) {
                    return results;
                }
                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    String temp = "";
                    while (!shutdown && resultSet.next()) {
                        int country = resultSet.getInt(COLUMN_NAME_COUNTRY);
                        results = resultSet.getString(COLUMN_NAME_DESCRIPTION);
                        if (country == 225) {
                            temp = "";
                            break;
                        } else if (country == 222) {
                            temp = results;
                        }
                    }

                    if (!temp.isEmpty()) {
                        results = temp;
                    }
                }
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }

        return results;
    }

    public static Map<EntityType, String> getDescriptions() {
        TreeMap<EntityType, String> results = new TreeMap<>();

        String sql = String.format("SELECT * FROM entity_type ORDER BY %s,%s,%s,%s,%s,%s,%s",
                COLUMN_NAME_KIND,
                COLUMN_NAME_DOMAIN,
                COLUMN_NAME_COUNTRY,
                COLUMN_NAME_CATEGORY,
                COLUMN_NAME_SUBCATEGORY,
                COLUMN_NAME_SPECIFIC,
                COLUMN_NAME_EXTRA);

        synchronized (shutdownMutex) {
            try {
                Connection connection = getConnection();
                if (connection == null) {
                    return results;
                }
                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    while (!shutdown && resultSet.next()) {
                        EntityType entityType = new EntityType(
                                resultSet.getInt(COLUMN_NAME_KIND),
                                resultSet.getInt(COLUMN_NAME_DOMAIN),
                                resultSet.getInt(COLUMN_NAME_COUNTRY),
                                resultSet.getInt(COLUMN_NAME_CATEGORY),
                                resultSet.getInt(COLUMN_NAME_SUBCATEGORY),
                                resultSet.getInt(COLUMN_NAME_SPECIFIC),
                                resultSet.getInt(COLUMN_NAME_EXTRA));
                        results.put(entityType, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                    }
                }
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }

        return results;
    }

    public static Map<EntityType, String> getDescriptions(EntityType.Kind kind) {
        TreeMap<EntityType, String> results = new TreeMap<>();

        String sql = "SELECT * FROM entity_type WHERE kind=" + kind.ordinal();

        synchronized (shutdownMutex) {
            try {
                Connection connection = getConnection();
                if (connection == null) {
                    return results;
                }
                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    while (!shutdown && resultSet.next()) {
                        EntityType entityType = new EntityType(
                                resultSet.getInt(COLUMN_NAME_KIND),
                                resultSet.getInt(COLUMN_NAME_DOMAIN),
                                resultSet.getInt(COLUMN_NAME_COUNTRY),
                                resultSet.getInt(COLUMN_NAME_CATEGORY),
                                resultSet.getInt(COLUMN_NAME_SUBCATEGORY),
                                resultSet.getInt(COLUMN_NAME_SPECIFIC),
                                resultSet.getInt(COLUMN_NAME_EXTRA));
                        results.put(entityType, resultSet.getString(COLUMN_NAME_DESCRIPTION));
                    }
                }
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }

        return results;
    }

    /**
     * Get the MIL-STD-2525 SIDC for the provided EntityType string value.
     *
     * @param entityType The TENA.LVC.EntityType.
     * @return The MIL-STD-2525 SIDC.
     */
    public static String getTacticalSymbol(TENA.LVC.EntityType.ImmutableLocalClass entityType) {
        return getTacticalSymbol(EntityType.fromTenaType(entityType));
    }

    /**
     * Get the MIL-STD-2525 SIDC for the provided EntityType string value.
     *
     * @param entityType The EntityType.
     * @return The MIL-STD-2525 SIDC.
     */
    public static String getTacticalSymbol(EntityType entityType) {
        if (entityType == null) {
            return "";
        }
        String sqlBase = "SELECT symbol_code FROM tactical_symbol WHERE entity_type LIKE '%s'";
        String entityTypeString = entityType.toString();
        String sql = String.format(sqlBase, entityTypeString);

        synchronized (shutdownMutex) {
            try {
                Connection connection = getConnection();
                if (connection == null) {
                    return "";
                }
                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }

                entityTypeString = String.format("%d.%d.%d.%d.%d.%d",
                        entityType.getKind(),
                        entityType.getDomain(),
                        entityType.getCountry(),
                        entityType.getCategory(),
                        entityType.getSubcategory(),
                        entityType.getSpecific());
                sql = String.format(sqlBase, entityTypeString);

                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }

                entityTypeString = String.format("%d.%d.%d.%d.%d",
                        entityType.getKind(),
                        entityType.getDomain(),
                        entityType.getCountry(),
                        entityType.getCategory(),
                        entityType.getSubcategory());
                sql = String.format(sqlBase, entityTypeString);

                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }

                entityTypeString = String.format("%d.%d.%d.%d",
                        entityType.getKind(),
                        entityType.getDomain(),
                        entityType.getCountry(),
                        entityType.getCategory());
                sql = String.format(sqlBase, entityTypeString);

                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }

                entityTypeString = String.format("%d.%d.%d",
                        entityType.getKind(),
                        entityType.getDomain(),
                        entityType.getCountry());
                sql = String.format(sqlBase, entityTypeString);

                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }

                entityTypeString = String.format("%d.%d.0",
                        entityType.getKind(),
                        entityType.getDomain());
                sql = String.format(sqlBase, entityTypeString);

                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }

                entityTypeString = String.format("%d.0.0",
                        entityType.getKind());
                sql = String.format(sqlBase, entityTypeString);

                try (ResultSet resultSet = connection.createStatement().executeQuery(sql)) {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    }
                }
            } catch (SQLException e) {
                Logger.getGlobal().log(Level.WARNING, null, e);
            }
        }

        return "";
    }
}
