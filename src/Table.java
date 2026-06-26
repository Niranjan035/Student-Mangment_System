// =============================================================================
// FILE: Table.java
// PACKAGE: Table
// =============================================================================
//
// WHY THIS CLASS EXISTS:
// ----------------------
// A JTable in Swing does NOT store its own data. It delegates all data
// management to a "model" — specifically a TableModel. The DefaultTableModel
// is the standard implementation.
//
// This class is responsible for:
//   1. Creating the DefaultTableModel with the correct column headers
//   2. Creating and configuring the JTable
//   3. Fetching all student rows from MySQL and loading them into the model
//   4. Refreshing the table whenever data changes (add/update/delete)
//
// By isolating table logic here, AppGUI.java stays focused on layout and
// user interaction — clean separation of concerns.
//
// =============================================================================

package Table; // Belongs to the 'Table' package (src/Table/ folder)

// -----------------------------------------------------------------------------
// IMPORTS
// -----------------------------------------------------------------------------

import dbConnect.DBConnect;
// WHY: We import our own DBConnect class so Table can get a database connection
// to fetch student records. Every JDBC operation starts with a Connection.

import java.sql.Connection;
// WHY: Represents an open session with the MySQL database.
// We use it to create PreparedStatement objects that execute SQL queries.

import java.sql.PreparedStatement;
// WHY: PreparedStatement is used to execute parameterized SQL queries.
// It is safer than Statement because it prevents SQL injection.
// Even though our SELECT * query here has no parameters, we use it
// consistently throughout the project to establish good habits.

import java.sql.ResultSet;
// WHY: ResultSet holds the rows returned by a SELECT query.
// We iterate over it with next() to read each student record.
// Think of it as a cursor that starts before the first row.

import java.sql.SQLException;
// WHY: Thrown by JDBC methods when a database operation fails.
// We catch it to display meaningful error messages instead of crashing.

import javax.swing.JTable;
// WHY: JTable is the Swing component that visually displays tabular data.
// It renders rows and columns based on whatever model we give it.
// We configure it here (selection mode, column widths, etc.).

import javax.swing.JScrollPane;
// WHY: JTable does NOT include scrollbars by itself.
// We must wrap JTable inside a JScrollPane to get scrolling behavior
// when there are more rows than the visible area can show.
// JScrollPane also draws the column headers automatically.

import javax.swing.table.DefaultTableModel;
// WHY: DefaultTableModel is Swing's built-in TableModel implementation.
// It stores data in a 2D Vector internally.
// Key methods we use:
//   setRowCount(0)  → clears all rows (used before re-loading)
//   addRow(Object[]) → adds a new row of data
//   getValueAt(row, col) → reads a cell value (used when row is clicked)

import javax.swing.ListSelectionModel;
// WHY: Controls how rows can be selected in the JTable.
// We set SINGLE_SELECTION mode so only one student row is selected at a time.
// This prevents confusion when populating the form fields from a click.

// =============================================================================
// CLASS DECLARATION
// =============================================================================

public class Table {
    // This class is NOT a JComponent itself. It's a helper that creates and
    // manages a JTable. AppGUI will call getScrollPane() to get the visual
    // component it places in the window layout.

    // -------------------------------------------------------------------------
    // INSTANCE VARIABLES
    // -------------------------------------------------------------------------

    private JTable table;
    // WHY: The visual Swing component. AppGUI will call addMouseListener on this
    // to detect when a user clicks a row.
    // 'private' → only this class can access the raw JTable. External classes
    //             use our getter methods instead.

    private DefaultTableModel model;
    // WHY: The data model behind the JTable. We call model.addRow() to insert
    // data rows, and model.setRowCount(0) to clear them before refreshing.
    // Separating model from view (JTable) is Swing's MVC (Model-View-Controller)
    // design pattern.

    private JScrollPane scrollPane;
    // WHY: The container that wraps JTable and provides scrollbars.
    // AppGUI retrieves this via getScrollPane() and places it in the window.

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    // PURPOSE: Initialize the model, configure the JTable, wrap it in a
    //          JScrollPane, and load all students from the database.
    // -------------------------------------------------------------------------

    public Table() {

        // STEP 1: Define column headers
        // These Strings become the column header labels shown at the top of the table.
        // The order here MUST match the order we add data in loadStudents() below.
        String[] columns = {
            "Student ID",
            "First Name",
            "Last Name",
            "Major",
            "Phone",
            "GPA",
            "DOB"
        };
        // Array of 7 Strings — one for each column we want displayed.

        // STEP 2: Create the DefaultTableModel
        model = new DefaultTableModel(columns, 0) {
            // 'columns' → the header labels defined above
            // '0'       → start with ZERO rows (we'll add rows from the database)
            //
            // We use an anonymous subclass (the { } after the constructor) to
            // override one method: isCellEditable(). This prevents users from
            // directly editing cells in the table — all edits go through our form.

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
                // WHY: Return false for ALL cells in ALL rows.
                // By default, DefaultTableModel allows editing. We disable it
                // because we want users to edit data only through the form fields,
                // not by clicking directly on the table. This enforces validation.
            }
        };

        // STEP 3: Create the JTable with our model
        table = new JTable(model);
        // We pass our custom model so the JTable uses it for all data operations.

        // STEP 4: Configure the JTable appearance and behavior

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // WHY: Restrict to single-row selection. When the user clicks a row,
        // exactly one row is highlighted and its data populates the form fields.
        // Without this, the user could select multiple rows (which we don't need).

        table.setRowHeight(22);
        // WHY: Set each row's height to 22 pixels for comfortable readability.
        // The default is around 16px which feels cramped.

        table.getTableHeader().setReorderingAllowed(false);
        // WHY: Prevent users from dragging column headers to reorder columns.
        // If columns were reordered, the getValueAt() indices in AppGUI's
        // MouseListener would break (column 0 would no longer be Student ID).

        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // WHY: Distribute available width evenly across all columns.
        // This prevents horizontal scrolling and uses space efficiently.

        // STEP 5: Configure column widths
        // Set preferred widths for each column (index 0–6)
        table.getColumnModel().getColumn(0).setPreferredWidth(80);   // Student ID
        table.getColumnModel().getColumn(1).setPreferredWidth(90);   // First Name
        table.getColumnModel().getColumn(2).setPreferredWidth(90);   // Last Name
        table.getColumnModel().getColumn(3).setPreferredWidth(100);  // Major
        table.getColumnModel().getColumn(4).setPreferredWidth(90);   // Phone
        table.getColumnModel().getColumn(5).setPreferredWidth(50);   // GPA
        table.getColumnModel().getColumn(6).setPreferredWidth(90);   // DOB
        // These are 'preferred' widths — the layout manager may adjust them,
        // but they give the table a sensible starting appearance.

        // STEP 6: Wrap JTable in JScrollPane
        scrollPane = new JScrollPane(table);
        // WHY: JScrollPane adds vertical and horizontal scrollbars when needed.
        // CRITICAL: Without this wrapper, the column headers (Student ID, etc.)
        //           are NOT displayed. JScrollPane's viewport header holds them.

        // STEP 7: Load data from the database
        loadStudents();
        // Call our own method to fetch all records from MySQL and populate the model.
    }

    // -------------------------------------------------------------------------
    // METHOD: loadStudents()
    // -------------------------------------------------------------------------
    // PURPOSE:
    //   Clears all existing rows from the table model, then fetches every row
    //   from the 'students' table in MySQL and adds them to the model.
    //   Called once in the constructor, and re-called by refreshTable() after
    //   any add/update/delete operation.
    //
    // SQL QUERY EXPLAINED:
    //   SELECT Student_ID, first_name, last_name, major, Phone, GPA, DOB
    //   FROM students
    //
    //   We explicitly name each column (rather than SELECT *) so:
    //   1. The column ORDER is guaranteed — matches our table headers exactly
    //   2. If a new column is ever added to the DB, it won't unexpectedly appear
    //   3. Code is more readable and self-documenting
    // -------------------------------------------------------------------------

    public void loadStudents() {

        model.setRowCount(0);
        // WHY: Before fetching fresh data, wipe all existing rows.
        // setRowCount(0) removes all rows without removing the column headers.
        // This is the standard Swing pattern for "refresh" operations.
        // If we didn't do this, every refresh would APPEND rows, creating duplicates.

        // SQL query to fetch all students, ordered by Student_ID for consistency
        String sql = "SELECT Student_ID, first_name, last_name, major, Phone, GPA, DOB FROM students ORDER BY Student_ID";
        // ORDER BY Student_ID → Results sorted alphabetically by Student_ID.
        // This gives the user a predictable, consistent display order.

        Connection conn = null;
        // Declare outside try so we can close it in the finally block.
        // Declaring as null lets us check (conn != null) before closing.

        PreparedStatement pst = null;
        // PreparedStatement compiles the SQL on the MySQL server side.
        // For a SELECT with no parameters, this is equivalent to Statement,
        // but we use PreparedStatement universally for style consistency.

        ResultSet rs = null;
        // ResultSet holds the query results as a cursor-based row iterator.

        try {
            conn = DBConnect.getConnection();
            // Ask DBConnect for an open Connection to MySQL.
            // If this fails, DBConnect throws RuntimeException (which we catch below).

            pst = conn.prepareStatement(sql);
            // WHY: prepareStatement() sends the SQL to MySQL, which parses and
            // compiles it. The compiled query is stored in 'pst'.
            // For this SELECT, there are no ? placeholders, so no setXxx() calls.

            rs = pst.executeQuery();
            // WHY: executeQuery() runs the SELECT on MySQL and returns a ResultSet.
            // The cursor starts BEFORE the first row — we must call next() to move.
            // Use executeQuery() for SELECT statements.
            // Use executeUpdate() for INSERT, UPDATE, DELETE.

            while (rs.next()) {
                // rs.next() moves the cursor to the next row and returns:
                //   true  → there IS a row at the new position (enter the loop body)
                //   false → no more rows (exit the while loop)
                // On the first call, it moves from "before first row" to row 1.

                Object[] rowData = {
                    rs.getString("Student_ID"),
                    // getString("column_name") reads the VARCHAR column by its name.
                    // Using column names (not indices like rs.getString(1)) is safer
                    // — if column order ever changes in MySQL, our code still works.

                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("major"),
                    rs.getString("Phone"),

                    rs.getString("GPA"),
                    // GPA is DECIMAL(3,1) in MySQL. We read it as String here so
                    // it displays cleanly in the table (e.g. "3.8" not "3.800000").
                    // When we need to do math with GPA, we'd use rs.getDouble().

                    rs.getString("DOB")
                    // DOB is a DATE column. getString converts it to "YYYY-MM-DD" format.
                    // This matches the format we'll use in our validation and INSERT.
                };

                model.addRow(rowData);
                // WHY: addRow() appends this array as a new row in the DefaultTableModel.
                // The JTable automatically re-renders to show the new row.
                // We call this inside the loop, so each database row becomes a table row.
            }

        } catch (SQLException e) {
            // SQLExceptions from conn.prepareStatement() or pst.executeQuery()
            // are caught here. We print a message but don't crash the app —
            // the table simply shows no data if the DB is unavailable.
            System.err.println("Error loading students from database:");
            System.err.println("SQL State : " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Message   : " + e.getMessage());

        } finally {
            // The 'finally' block ALWAYS runs, whether or not an exception occurred.
            // This is where we MUST close JDBC resources to prevent "connection leaks."
            //
            // A connection leak means: the Connection to MySQL stays open even after
            // we're done with it. MySQL has a maximum connection limit. If we leak
            // connections, we'll eventually hit that limit and the app will stop
            // working for everyone.
            //
            // CLOSING ORDER: Always close in reverse order of creation: rs → pst → conn
            // This is because ResultSet depends on PreparedStatement, which depends on Connection.

            try {
                if (rs != null) rs.close();
                // Close ResultSet first. It releases the cursor on the MySQL server.
            } catch (SQLException e) {
                System.err.println("Failed to close ResultSet: " + e.getMessage());
            }

            try {
                if (pst != null) pst.close();
                // Close PreparedStatement. Releases the compiled query from MySQL's cache.
            } catch (SQLException e) {
                System.err.println("Failed to close PreparedStatement: " + e.getMessage());
            }

            try {
                if (conn != null) conn.close();
                // Close Connection LAST. This ends the TCP session with MySQL.
                // After this line, MySQL frees up the connection slot for others.
            } catch (SQLException e) {
                System.err.println("Failed to close Connection: " + e.getMessage());
            }
        }
    }

    // -------------------------------------------------------------------------
    // METHOD: refreshTable()
    // -------------------------------------------------------------------------
    // PURPOSE:
    //   Public method called by AppGUI after every Add, Update, or Delete.
    //   Simply delegates to loadStudents() to re-fetch all data from MySQL.
    //
    // WHY A SEPARATE METHOD?
    //   AppGUI doesn't need to know HOW the table refreshes — it just calls
    //   refreshTable(). This hides the implementation detail (loadStudents)
    //   behind a clear, intention-revealing name.
    // -------------------------------------------------------------------------

    public void refreshTable() {
        loadStudents();
        // Re-fetch all student records and rebuild the table display.
    }

    // -------------------------------------------------------------------------
    // METHOD: getTable()
    // -------------------------------------------------------------------------
    // PURPOSE:
    //   Returns the JTable component so AppGUI can attach a MouseListener to it.
    //   AppGUI needs the JTable reference to call getSelectedRow() and
    //   model.getValueAt() when the user clicks a row.
    // -------------------------------------------------------------------------

    public JTable getTable() {
        return table;
        // We expose the JTable so AppGUI can:
        //   table.addMouseListener(...)  → detect row clicks
        //   table.getSelectedRow()       → find which row is selected
    }

    // -------------------------------------------------------------------------
    // METHOD: getModel()
    // -------------------------------------------------------------------------
    // PURPOSE:
    //   Returns the DefaultTableModel so AppGUI can read cell values.
    //   When a user clicks a row, AppGUI calls model.getValueAt(row, col)
    //   to populate the form fields with that student's data.
    // -------------------------------------------------------------------------

    public DefaultTableModel getModel() {
        return model;
        // Exposing the model allows AppGUI to read values via getValueAt().
        // We chose not to expose setValueAt() because all data changes go
        // through JDBC operations, not directly through the model.
    }

    // -------------------------------------------------------------------------
    // METHOD: getScrollPane()
    // -------------------------------------------------------------------------
    // PURPOSE:
    //   Returns the JScrollPane that wraps the JTable.
    //   AppGUI calls this to get the component it adds to the JFrame layout.
    //   We return JScrollPane, NOT JTable, because the scroll pane is what
    //   contains the column headers and scrollbars.
    // -------------------------------------------------------------------------

    public JScrollPane getScrollPane() {
        return scrollPane;
        // AppGUI adds this to the JFrame:
        //   frame.add(tableInstance.getScrollPane());
        // The JScrollPane draws borders, headers, and scroll behavior.
    }
}
