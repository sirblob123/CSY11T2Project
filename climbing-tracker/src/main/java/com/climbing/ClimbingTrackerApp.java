package com.climbing;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Main Swing GUI for the Rock Climbing Tracker.
 *
 * Tabs:
 *   1. Log Climb  – input form for a new entry
 *   2. View Climbs – sortable / filterable table
 *   3. Stats       – summary statistics
 */
public class ClimbingTrackerApp extends JFrame {

    // ── Palette ───────────────────────────────────────────────
    private static final Color BG      = new Color(18,  18,  24);
    private static final Color PANEL   = new Color(28,  28,  38);
    private static final Color CARD    = new Color(38,  38,  52);
    private static final Color ACCENT  = new Color(255, 90,  50);   // bold orange-red
    private static final Color TEXT    = new Color(240, 235, 228);
    private static final Color MUTED   = new Color(130, 120, 140);
    private static final Color SUCCESS = new Color(80,  200, 120);

    // ── Core components ───────────────────────────────────────
    private final DatabaseManager db;
    private JTabbedPane tabbedPane;

    // Log Climb tab
    private JTextField   dateField;
    private JTextField   gradeField;
    private JComboBox<String> settingCombo;
    private JTextField   locationField;
    private JComboBox<String> climbTypeCombo;
    private JTextField   climbTypeCustom;
    private JTextArea    notesArea;
    private JLabel       statusLabel;

    // View Climbs tab
    private JTable       climbTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> sortCombo;
    private JComboBox<String> filterTypeCombo;
    private JTextField   filterValueField;
    private JButton      deleteButton;

    // Stats tab
    private JPanel statsPanel;

    // ══════════════════════════════════════════════════════════
    //  Constructor
    // ══════════════════════════════════════════════════════════
    public ClimbingTrackerApp(DatabaseManager db) {
        this.db = db;
        buildFrame();
        buildTabs();
        refreshTable("date");
        refreshStats();
    }

    // ── Frame setup ───────────────────────────────────────────
    private void buildFrame() {
        setTitle("🧗 Climbing Tracker");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 650);
        setMinimumSize(new Dimension(750, 500));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
    }

    // ── Tabs ─────────────────────────────────────────────────
    private void buildTabs() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(PANEL);
        tabbedPane.setForeground(TEXT);
        tabbedPane.setFont(new Font("Monospaced", Font.BOLD, 13));

        tabbedPane.addTab("  + Log Climb  ",  buildLogTab());
        tabbedPane.addTab("  ☰ View Climbs  ", buildViewTab());
        tabbedPane.addTab("  ◈ Stats  ",       buildStatsTab());

        add(tabbedPane);
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 1 — LOG CLIMB
    // ══════════════════════════════════════════════════════════
    private JPanel buildLogTab() {
        JPanel outer = darkPanel();
        outer.setLayout(new GridBagLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 2, true),
            BorderFactory.createEmptyBorder(24, 30, 24, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(6, 6, 6, 6);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // Title
        JLabel title = new JLabel("Log a Climb");
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 18, 0);
        form.add(title, gbc);
        gbc.gridwidth = 1;
        gbc.insets = new Insets(6, 6, 6, 6);
        row++;

        // Date
        addLabel(form, "Date (dd/MM/yyyy)", gbc, 0, row);
        dateField = styledField();
        dateField.setText(Validator.formatDate(LocalDate.now()));
        addField(form, dateField, gbc, 1, row++);

        // Grade
        addLabel(form, "Grade (V0–V17)", gbc, 0, row);
        gradeField = styledField();
        gradeField.setToolTipText("Enter grade like V4 or just 4");
        addField(form, gradeField, gbc, 1, row++);

        // Setting
        addLabel(form, "Setting", gbc, 0, row);
        settingCombo = styledCombo(Validator.SETTINGS);
        addField(form, settingCombo, gbc, 1, row++);

        // Location
        addLabel(form, "Location / Gym", gbc, 0, row);
        locationField = styledField();
        locationField.setToolTipText("Max 100 characters");
        addField(form, locationField, gbc, 1, row++);

        // Climb type
        addLabel(form, "Climb Type", gbc, 0, row);
        String[] typeOptions = appendOther(Validator.CLIMB_TYPES);
        climbTypeCombo = styledCombo(typeOptions);
        climbTypeCombo.addActionListener(e -> {
            boolean custom = "Other (type below)".equals(climbTypeCombo.getSelectedItem());
            climbTypeCustom.setVisible(custom);
        });
        addField(form, climbTypeCombo, gbc, 1, row++);

        // Custom type field (hidden until "Other" selected)
        addLabel(form, "Custom Type", gbc, 0, row);
        climbTypeCustom = styledField();
        climbTypeCustom.setVisible(false);
        addField(form, climbTypeCustom, gbc, 1, row++);

        // Notes
        addLabel(form, "Notes (optional)", gbc, 0, row);
        notesArea = new JTextArea(3, 20);
        styleTextArea(notesArea);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createLineBorder(MUTED));
        gbc.gridx = 1; gbc.gridy = row++;
        form.add(notesScroll, gbc);

        // Submit button
        JButton submitBtn = accentButton("  SAVE CLIMB  ");
        submitBtn.addActionListener(e -> handleSaveClimb());
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill   = GridBagConstraints.NONE;
        gbc.insets = new Insets(18, 0, 6, 0);
        form.add(submitBtn, gbc);

        // Status label
        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Monospaced", Font.ITALIC, 12));
        statusLabel.setForeground(SUCCESS);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = ++row;
        form.add(statusLabel, gbc);

        outer.add(form);
        return outer;
    }

    /** Reads and validates the form, then saves to the database. */
    private void handleSaveClimb() {
        statusLabel.setForeground(Color.RED);

        // ── Validate date ────────────────────────────────────
        LocalDate date = Validator.validateDate(dateField.getText());
        if (date == null) {
            statusLabel.setText("✗ Invalid date. Use dd/MM/yyyy and not a future date.");
            return;
        }

        // ── Validate grade ───────────────────────────────────
        int grade = Validator.validateGrade(gradeField.getText());
        if (grade == -1) {
            statusLabel.setText("✗ Invalid grade. Enter V0–V17 (e.g. V4 or 4).");
            return;
        }

        // ── Validate setting ─────────────────────────────────
        String setting = Validator.validateSetting(
                (String) settingCombo.getSelectedItem());
        if (setting == null) {
            statusLabel.setText("✗ Please select Indoors or Outdoors.");
            return;
        }

        // ── Validate location ────────────────────────────────
        String location = Validator.validateLocation(locationField.getText());
        if (location == null) {
            statusLabel.setText("✗ Location must be 1–100 characters.");
            return;
        }

        // ── Validate climb type ──────────────────────────────
        String rawType = "Other (type below)".equals(climbTypeCombo.getSelectedItem())
                ? climbTypeCustom.getText()
                : (String) climbTypeCombo.getSelectedItem();
        String climbType = Validator.validateClimbType(rawType);
        if (climbType == null) {
            statusLabel.setText("✗ Please enter a climb type.");
            return;
        }

        // ── Save ─────────────────────────────────────────────
        String notes = notesArea.getText().trim();
        Climb climb = new Climb(Validator.formatDate(date), grade,
                                setting, location, climbType, notes);
        boolean ok = db.addClimb(climb);

        if (ok) {
            statusLabel.setForeground(SUCCESS);
            statusLabel.setText("✓ Climb saved!");
            clearForm();
            refreshTable((String) sortCombo.getSelectedItem());
            refreshStats();
        } else {
            statusLabel.setText("✗ Database error — could not save climb.");
        }
    }

    private void clearForm() {
        dateField.setText(Validator.formatDate(LocalDate.now()));
        gradeField.setText("");
        settingCombo.setSelectedIndex(0);
        locationField.setText("");
        climbTypeCombo.setSelectedIndex(0);
        climbTypeCustom.setText("");
        climbTypeCustom.setVisible(false);
        notesArea.setText("");
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 2 — VIEW CLIMBS
    // ══════════════════════════════════════════════════════════
    private JPanel buildViewTab() {
        JPanel panel = darkPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        // ── Controls bar ─────────────────────────────────────
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        controls.setBackground(PANEL);

        controls.add(styledLabel("Sort by:"));
        sortCombo = styledCombo(new String[]{"date", "grade", "location"});
        sortCombo.addActionListener(e -> refreshTable((String) sortCombo.getSelectedItem()));
        controls.add(sortCombo);

        controls.add(Box.createHorizontalStrut(20));
        controls.add(styledLabel("Filter by:"));
        filterTypeCombo = styledCombo(new String[]{"none", "setting", "climb_type", "grade"});
        controls.add(filterTypeCombo);
        filterValueField = styledField();
        filterValueField.setPreferredSize(new Dimension(120, 28));
        filterValueField.setToolTipText("Type filter value then press Enter");
        controls.add(filterValueField);

        JButton applyFilter = ghostButton("Apply");
        applyFilter.addActionListener(e -> applyFilter());
        filterValueField.addActionListener(e -> applyFilter());
        controls.add(applyFilter);

        JButton clearFilter = ghostButton("Clear");
        clearFilter.addActionListener(e -> {
            filterValueField.setText("");
            filterTypeCombo.setSelectedIndex(0);
            refreshTable((String) sortCombo.getSelectedItem());
        });
        controls.add(clearFilter);

        controls.add(Box.createHorizontalStrut(20));
        deleteButton = ghostButton("Delete Selected");
        deleteButton.setForeground(new Color(255, 80, 80));
        deleteButton.addActionListener(e -> handleDelete());
        controls.add(deleteButton);

        panel.add(controls, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────
        String[] columns = {"ID", "Date", "Grade", "Setting", "Location", "Type", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        climbTable = new JTable(tableModel);
        styleTable(climbTable);

        JScrollPane scroll = new JScrollPane(climbTable);
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(CARD);
        scroll.setBorder(BorderFactory.createLineBorder(MUTED, 1));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void applyFilter() {
        String filterType  = (String) filterTypeCombo.getSelectedItem();
        String filterValue = filterValueField.getText().trim();
        if ("none".equals(filterType) || filterValue.isEmpty()) {
            refreshTable((String) sortCombo.getSelectedItem());
            return;
        }
        List<Climb> climbs = db.getClimbsFiltered(filterType, filterValue);
        populateTable(climbs);
    }

    private void handleDelete() {
        int selectedRow = climbTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                "Please select a row to delete.", "No Selection",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete climb #" + id + "? This cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            db.deleteClimb(id);
            refreshTable((String) sortCombo.getSelectedItem());
            refreshStats();
        }
    }

    /** Fetches sorted data from DB and populates the table. */
    private void refreshTable(String sortBy) {
        List<Climb> climbs = db.getClimbs(sortBy);
        populateTable(climbs);
    }

    private void populateTable(List<Climb> climbs) {
        tableModel.setRowCount(0);
        for (Climb c : climbs) {
            tableModel.addRow(new Object[]{
                c.getId(),
                c.getDate(),
                c.getGradeLabel(),
                c.getSetting(),
                c.getLocation(),
                c.getClimbType(),
                c.getNotes()
            });
        }
    }

    // ══════════════════════════════════════════════════════════
    //  TAB 3 — STATS
    // ══════════════════════════════════════════════════════════
    private JPanel buildStatsTab() {
        JPanel outer = darkPanel();
        outer.setLayout(new BorderLayout());
        outer.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Statistics");
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(ACCENT);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        outer.add(title, BorderLayout.NORTH);

        statsPanel = new JPanel();
        statsPanel.setBackground(BG);
        statsPanel.setLayout(new GridLayout(0, 3, 16, 16));
        outer.add(statsPanel, BorderLayout.CENTER);

        return outer;
    }

    private void refreshStats() {
        statsPanel.removeAll();
        String[][] stats = db.getStats();
        for (String[] stat : stats) {
            statsPanel.add(statCard(stat[0], stat[1]));
        }
        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private JPanel statCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT, 1, true),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        ));
        JLabel val = new JLabel(value, SwingConstants.CENTER);
        val.setFont(new Font("Monospaced", Font.BOLD, 32));
        val.setForeground(ACCENT);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(new Font("Monospaced", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        card.add(val, BorderLayout.CENTER);
        card.add(lbl, BorderLayout.SOUTH);
        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  Styling Helpers
    // ══════════════════════════════════════════════════════════

    private JPanel darkPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(BG);
        return p;
    }

    private JTextField styledField() {
        JTextField f = new JTextField(18);
        f.setBackground(PANEL);
        f.setForeground(TEXT);
        f.setCaretColor(ACCENT);
        f.setFont(new Font("Monospaced", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MUTED),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));
        return f;
    }

    private void styleTextArea(JTextArea ta) {
        ta.setBackground(PANEL);
        ta.setForeground(TEXT);
        ta.setCaretColor(ACCENT);
        ta.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ta.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
    }

    private <T> JComboBox<T> styledCombo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setBackground(PANEL);
        cb.setForeground(TEXT);
        cb.setFont(new Font("Monospaced", Font.PLAIN, 13));
        return cb;
    }

    private JButton accentButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Monospaced", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton ghostButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PANEL);
        b.setForeground(TEXT);
        b.setFont(new Font("Monospaced", Font.PLAIN, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(MUTED),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel styledLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        return l;
    }

    private void styleTable(JTable table) {
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(26);
        table.setGridColor(PANEL);
        table.setSelectionBackground(ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PANEL);
        header.setForeground(ACCENT);
        header.setFont(new Font("Monospaced", Font.BOLD, 12));

        // Narrow the ID column
        table.getColumnModel().getColumn(0).setMaxWidth(50);
    }

    private void addLabel(JPanel p, String text, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x; gbc.gridy = y;
        JLabel l = new JLabel(text + ":");
        l.setForeground(MUTED);
        l.setFont(new Font("Monospaced", Font.PLAIN, 12));
        p.add(l, gbc);
    }

    private void addField(JPanel p, JComponent c, GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x; gbc.gridy = y;
        p.add(c, gbc);
    }

    private String[] appendOther(String[] arr) {
        String[] result = new String[arr.length + 1];
        System.arraycopy(arr, 0, result, 0, arr.length);
        result[arr.length] = "Other (type below)";
        return result;
    }

    // ══════════════════════════════════════════════════════════
    //  Entry Point
    // ══════════════════════════════════════════════════════════
    public static void main(String[] args) {
        // Use the system look and feel as a base, then override colors
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}

        DatabaseManager db = new DatabaseManager();
        db.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            ClimbingTrackerApp app = new ClimbingTrackerApp(db);
            app.setVisible(true);
        });
    }
}
