package com.climbing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations using SQLite via JDBC.
 *
 * ╔══════════════════════════════════════════════════════════╗
 * ║  ALL SQL STATEMENTS ARE STORED IN  sql/queries.sql      ║
 * ║  Each String constant below is labelled with its        ║
 * ║  SQL_BLOCK name so you can find the matching SQL in     ║
 * ║  that file and paste / modify it here.                  ║
 * ╚══════════════════════════════════════════════════════════╝
 */
public class DatabaseManager {

    // ── Database file path ────────────────────────────────────
    private static final String DB_URL = "jdbc:sqlite:climbing_tracker.db";

    // ══════════════════════════════════════════════════════════
    //  SQL STATEMENTS  (see sql/queries.sql for full annotated
    //  versions with comments and parameter documentation)
    // ══════════════════════════════════════════════════════════

    // SQL_BLOCK: CREATE_TABLE
    private static final String SQL_CREATE_TABLE =
        "CREATE TABLE IF NOT EXISTS climbs (" +
        "    id         INTEGER PRIMARY KEY AUTOINCREMENT," +
        "    date       TEXT    NOT NULL," +
        "    grade      INTEGER NOT NULL," +
        "    setting    TEXT    NOT NULL CHECK(setting IN ('Indoors', 'Outdoors'))," +
        "    location   TEXT    NOT NULL," +
        "    climb_type TEXT    NOT NULL," +
        "    notes      TEXT" +
        ");";

    // SQL_BLOCK: INSERT_CLIMB
    private static final String SQL_INSERT =
        "INSERT INTO climbs (date, grade, setting, location, climb_type, notes) " +
        "VALUES (?, ?, ?, ?, ?, ?);";

    // SQL_BLOCK: SELECT_ALL
    private static final String SQL_SELECT_ALL =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs ORDER BY date DESC;";

    // SQL_BLOCK: SELECT_SORTED_BY_GRADE
    private static final String SQL_SORT_GRADE =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs ORDER BY grade DESC, date DESC;";

    // SQL_BLOCK: SELECT_SORTED_BY_DATE
    private static final String SQL_SORT_DATE =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs ORDER BY date DESC;";

    // SQL_BLOCK: SELECT_SORTED_BY_LOCATION
    private static final String SQL_SORT_LOCATION =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs ORDER BY location ASC, date DESC;";

    // SQL_BLOCK: SELECT_FILTERED_BY_SETTING  (WHERE setting = ?)
    // SQL_BLOCK: SELECT_FILTERED_BY_TYPE     (WHERE climb_type = ?)
    // SQL_BLOCK: SELECT_FILTERED_BY_GRADE    (WHERE grade = ?)
    // These three share the same template; the column name is swapped at runtime.
    private static final String SQL_FILTER_SETTING =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs WHERE setting = ? ORDER BY date DESC;";

    private static final String SQL_FILTER_TYPE =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs WHERE climb_type = ? ORDER BY date DESC;";

    private static final String SQL_FILTER_GRADE =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs WHERE grade = ? ORDER BY date DESC;";

    // SQL_BLOCK: SELECT_STATS
    private static final String SQL_STATS =
        "SELECT COUNT(*) AS total_climbs, MAX(grade) AS highest_grade, " +
        "MIN(grade) AS lowest_grade, ROUND(AVG(grade),1) AS average_grade, " +
        "SUM(CASE WHEN setting='Indoors'  THEN 1 ELSE 0 END) AS indoor_count, " +
        "SUM(CASE WHEN setting='Outdoors' THEN 1 ELSE 0 END) AS outdoor_count " +
        "FROM climbs;";

    // SQL_BLOCK: DELETE_CLIMB
    private static final String SQL_DELETE =
        "DELETE FROM climbs WHERE id = ?;";

    // SQL_BLOCK: SELECT_BY_ID
    private static final String SQL_SELECT_BY_ID =
        "SELECT id, date, grade, setting, location, climb_type, notes " +
        "FROM climbs WHERE id = ?;";

    // ══════════════════════════════════════════════════════════
    //  Public Methods
    // ══════════════════════════════════════════════════════════

    /** Creates the climbs table if it doesn't already exist. */
    public void initializeDatabase() {
        try (Connection conn = connect();
             Statement  stmt = conn.createStatement()) {
            stmt.execute(SQL_CREATE_TABLE);
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    /** Inserts a new climb record. Returns true on success. */
    public boolean addClimb(Climb climb) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {

            pstmt.setString(1, climb.getDate());
            pstmt.setInt   (2, climb.getGrade());
            pstmt.setString(3, climb.getSetting());
            pstmt.setString(4, climb.getLocation());
            pstmt.setString(5, climb.getClimbType());
            pstmt.setString(6, climb.getNotes());
            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding climb: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns all climbs sorted by the specified column.
     * @param sortBy  "grade" | "date" | "location"  (defaults to date)
     */
    public List<Climb> getClimbs(String sortBy) {
        String sql;
        switch (sortBy.toLowerCase()) {
            case "grade":    sql = SQL_SORT_GRADE;    break;
            case "location": sql = SQL_SORT_LOCATION; break;
            default:         sql = SQL_SORT_DATE;     break;
        }
        return runQuery(sql, null, null);
    }

    /**
     * Returns climbs filtered by a specific field value.
     * @param filterBy "setting" | "climb_type" | "grade"
     * @param value    the value to match
     */
    public List<Climb> getClimbsFiltered(String filterBy, String value) {
        String sql;
        switch (filterBy.toLowerCase()) {
            case "setting":    sql = SQL_FILTER_SETTING; break;
            case "climb_type": sql = SQL_FILTER_TYPE;    break;
            case "grade":      sql = SQL_FILTER_GRADE;   break;
            default:           sql = SQL_SELECT_ALL;     break;
        }
        return runQuery(sql, value, filterBy.equals("grade") ? "int" : "string");
    }

    /** Returns summary statistics as a formatted string array [label, value]. */
    public String[][] getStats() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(SQL_STATS)) {

            if (rs.next()) {
                return new String[][] {
                    {"Total Climbs",   String.valueOf(rs.getInt("total_climbs"))},
                    {"Highest Grade",  "V" + rs.getInt("highest_grade")},
                    {"Lowest Grade",   "V" + rs.getInt("lowest_grade")},
                    {"Average Grade",  "V" + rs.getDouble("average_grade")},
                    {"Indoor Climbs",  String.valueOf(rs.getInt("indoor_count"))},
                    {"Outdoor Climbs", String.valueOf(rs.getInt("outdoor_count"))}
                };
            }
        } catch (SQLException e) {
            System.err.println("Error fetching stats: " + e.getMessage());
        }
        return new String[0][0];
    }

    /** Deletes the climb with the given id. Returns true on success. */
    public boolean deleteClimb(int id) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE)) {
            pstmt.setInt(1, id);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting climb: " + e.getMessage());
            return false;
        }
    }

    /** Retrieves a single climb by id, or null if not found. */
    public Climb getClimbById(int id) {
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(SQL_SELECT_BY_ID)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.err.println("Error fetching climb: " + e.getMessage());
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════
    //  Private Helpers
    // ══════════════════════════════════════════════════════════

    /** Opens and returns a database connection. */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /**
     * Runs any SELECT query that optionally takes one parameter.
     * @param sql    the prepared SQL string
     * @param value  the parameter value (or null for no parameter)
     * @param type   "int" or "string" — how to bind value
     */
    private List<Climb> runQuery(String sql, String value, String type) {
        List<Climb> results = new ArrayList<>();
        try (Connection conn = connect()) {
            ResultSet rs;
            if (value == null) {
                rs = conn.createStatement().executeQuery(sql);
            } else {
                PreparedStatement pstmt = conn.prepareStatement(sql);
                if ("int".equals(type)) pstmt.setInt(1, Integer.parseInt(value));
                else                    pstmt.setString(1, value);
                rs = pstmt.executeQuery();
            }
            while (rs.next()) results.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Query error: " + e.getMessage());
        }
        return results;
    }

    /** Maps the current ResultSet row to a Climb object. */
    private Climb mapRow(ResultSet rs) throws SQLException {
        return new Climb(
            rs.getInt("id"),
            rs.getString("date"),
            rs.getInt("grade"),
            rs.getString("setting"),
            rs.getString("location"),
            rs.getString("climb_type"),
            rs.getString("notes") != null ? rs.getString("notes") : ""
        );
    }
}
