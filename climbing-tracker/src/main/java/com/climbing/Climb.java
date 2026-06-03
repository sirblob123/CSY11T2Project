package com.climbing;

/**
 * Represents a single climbing record.
 * Stores all attributes of a climb session.
 */
public class Climb {

    // ── Fields ──────────────────────────────────────────────
    private int    id;
    private String date;
    private int    grade;
    private String setting;   // "Indoors" or "Outdoors"
    private String location;
    private String climbType;
    private String notes;

    // ── Constructors ─────────────────────────────────────────

    /** Used when creating a new climb (no id yet — assigned by DB). */
    public Climb(String date, int grade, String setting,
                 String location, String climbType, String notes) {
        this.date      = date;
        this.grade     = grade;
        this.setting   = setting;
        this.location  = location;
        this.climbType = climbType;
        this.notes     = notes;
    }

    /** Used when reading a climb back from the database. */
    public Climb(int id, String date, int grade, String setting,
                 String location, String climbType, String notes) {
        this(date, grade, setting, location, climbType, notes);
        this.id = id;
    }

    // ── Getters ──────────────────────────────────────────────
    public int    getId()        { return id; }
    public String getDate()      { return date; }
    public int    getGrade()     { return grade; }
    public String getSetting()   { return setting; }
    public String getLocation()  { return location; }
    public String getClimbType() { return climbType; }
    public String getNotes()     { return notes; }

    /** Returns the V-scale label, e.g. "V4". */
    public String getGradeLabel() { return "V" + grade; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | %s | %s | %s",
                id, date, getGradeLabel(), setting, location, climbType,
                notes.isEmpty() ? "(no notes)" : notes);
    }
}
