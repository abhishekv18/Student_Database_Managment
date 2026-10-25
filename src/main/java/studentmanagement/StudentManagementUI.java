package studentmanagement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * The main window of the application.
 *
 * This class builds the Swing interface and handles the button clicks.
 * It does not contain any SQL: all database work is done by StudentDAO.
 */
public class StudentManagementUI extends JFrame {

    // ---------- Colors and fonts used in the window ----------
    private static final Color NAVY = new Color(25, 42, 86);
    private static final Color BLUE = new Color(41, 98, 168);
    private static final Color RED = new Color(192, 57, 43);
    private static final Color GRAY = new Color(108, 117, 125);
    private static final Color LIGHT_GRAY = new Color(236, 240, 245);
    private static final Color BORDER_GRAY = new Color(210, 215, 222);
    private static final Color SELECTED_ROW = new Color(204, 220, 245);

    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 17);
    private static final Font NORMAL_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 14);

    // Maximum lengths, matching the VARCHAR sizes in database.sql
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_COURSE_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 150;

    // ---------- Form fields ----------
    private JTextField rollNumberField;
    private JTextField fullNameField;
    private JTextField courseField;
    private JTextField emailField;
    private JTextField marksField;
    private JTextField searchField;

    // ---------- Buttons ----------
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton searchButton;
    private JButton showAllButton;

    // ---------- Table and status bar ----------
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;

    // The students currently shown in the table. Row 0 of the table is displayedStudents.get(0), and so on.
    private ArrayList<Student> displayedStudents = new ArrayList<>();

    // Roll number of the student selected in the table. -1 means "no student selected".
    private int selectedRollNumber = -1;

    // Object that runs all the SQL queries
    private StudentDAO studentDAO = new StudentDAO();

    /**
     * Builds the whole window. Called once from Main.
     */
    public StudentManagementUI() {
        setTitle("Student Database Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150, 650);
        setMinimumSize(new Dimension(950, 550));
        setLocationRelativeTo(null); // open in the centre of the screen

        // The middle area holds the form (left) and the table (right).
        JPanel contentPanel = new JPanel(new BorderLayout(15, 0));
        contentPanel.setBackground(LIGHT_GRAY);
        contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPanel.add(createFormPanel(), BorderLayout.WEST);
        contentPanel.add(createTablePanel(), BorderLayout.CENTER);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        mainPanel.add(createStatusPanel(), BorderLayout.SOUTH);
        setContentPane(mainPanel);

        connectButtonsToActions();
    }

    // =====================================================================
    //  Part 1: Building the interface
    // =====================================================================

    /**
     * The navy blue bar at the top with the project name.
     */
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(NAVY);
        headerPanel.setBorder(new EmptyBorder(16, 20, 16, 20));

        JLabel titleLabel = new JLabel("Student Database Management System");
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Add, view, search, update and delete student records");
        subtitleLabel.setFont(NORMAL_FONT);
        subtitleLabel.setForeground(new Color(200, 210, 230));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        return headerPanel;
    }

    /**
     * The white card on the left with the input fields and the four main buttons.
     */
    private JPanel createFormPanel() {
        // GridBagLayout lines up the labels in one column and the text fields in another.
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        fieldsPanel.setBackground(Color.WHITE);

        rollNumberField = createTextField();
        fullNameField = createTextField();
        courseField = createTextField();
        emailField = createTextField();
        marksField = createTextField();

        addFormRow(fieldsPanel, 0, "Roll Number *", rollNumberField);
        addFormRow(fieldsPanel, 1, "Full Name *", fullNameField);
        addFormRow(fieldsPanel, 2, "Course *", courseField);
        addFormRow(fieldsPanel, 3, "Email", emailField);
        addFormRow(fieldsPanel, 4, "Marks (0-100) *", marksField);

        JLabel hintLabel = new JLabel("* Required field. Select a table row to edit it.");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(GRAY);

        JPanel topPart = new JPanel(new BorderLayout(0, 10));
        topPart.setBackground(Color.WHITE);
        topPart.add(createSectionTitle("Student Details"), BorderLayout.NORTH);
        topPart.add(fieldsPanel, BorderLayout.CENTER);
        topPart.add(hintLabel, BorderLayout.SOUTH);

        // Four buttons of the same size in a 2 x 2 grid
        addButton = createButton("Add Student", NAVY);
        updateButton = createButton("Update Student", BLUE);
        deleteButton = createButton("Delete Student", RED);
        clearButton = createButton("Clear", GRAY);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(clearButton);

        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(createCardBorder());
        formPanel.setPreferredSize(new Dimension(380, 0));
        formPanel.add(topPart, BorderLayout.NORTH);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        return formPanel;
    }

    /**
     * Adds one "label + text field" row to the form.
     */
    private void addFormRow(JPanel panel, int row, String labelText, JTextField field) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridy = row;
        constraints.insets = new Insets(7, 0, 7, 10);

        // Column 0: the label
        JLabel label = new JLabel(labelText);
        label.setFont(BOLD_FONT);
        label.setForeground(NAVY);
        constraints.gridx = 0;
        constraints.anchor = GridBagConstraints.WEST;
        panel.add(label, constraints);

        // Column 1: the text field, stretched to fill the remaining width
        constraints.gridx = 1;
        constraints.weightx = 1.0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(7, 0, 7, 0);
        panel.add(field, constraints);
    }

    /**
     * The white card on the right with the search bar and the student table.
     */
    private JPanel createTablePanel() {
        // ----- Search bar -----
        searchField = createTextField();
        searchField.setColumns(8);
        searchButton = createButton("Search", NAVY);
        showAllButton = createButton("Show All", GRAY);
        searchButton.setPreferredSize(new Dimension(100, 34));
        showAllButton.setPreferredSize(new Dimension(100, 34));

        JLabel searchLabel = new JLabel("Search by Roll No:");
        searchLabel.setFont(BOLD_FONT);
        searchLabel.setForeground(NAVY);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.add(createSectionTitle("Student Records"), BorderLayout.WEST);
        topBar.add(searchPanel, BorderLayout.EAST);

        // ----- Table -----
        String[] columnNames = {"Roll Number", "Full Name", "Course", "Email", "Marks"};

        // The table model stores the rows. We override isCellEditable() so the
        // user cannot type inside the table; changes are made through the form instead.
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentTable = new JTable(tableModel);
        studentTable.setFont(NORMAL_FONT);
        studentTable.setRowHeight(30);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentTable.setSelectionBackground(SELECTED_ROW);
        studentTable.setSelectionForeground(Color.BLACK);
        studentTable.setGridColor(BORDER_GRAY);
        studentTable.setShowVerticalLines(false);

        JTableHeader header = studentTable.getTableHeader();
        header.setFont(BOLD_FONT);
        header.setBackground(NAVY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(0, 34));
        header.setReorderingAllowed(false);

        // Suggested column widths (they still stretch when the window is resized)
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(110);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(200);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(70);

        // JScrollPane adds scroll bars when there are more rows than fit on screen.
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 12));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(createCardBorder());
        tablePanel.add(topBar, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        return tablePanel;
    }

    /**
     * The status bar at the bottom of the window.
     */
    private JPanel createStatusPanel() {
        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(NORMAL_FONT);
        statusLabel.setForeground(NAVY);

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(Color.WHITE);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_GRAY),
                new EmptyBorder(8, 20, 8, 20)));
        statusPanel.add(statusLabel, BorderLayout.WEST);
        return statusPanel;
    }

    // ----- Small helper methods so every field and button looks the same -----

    private JTextField createTextField() {
        JTextField field = new JTextField(15);
        field.setFont(NORMAL_FONT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                new EmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(BOLD_FONT);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(150, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JLabel createSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(SECTION_FONT);
        label.setForeground(NAVY);
        return label;
    }

    private Border createCardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_GRAY),
                new EmptyBorder(18, 18, 18, 18));
    }

    // =====================================================================
    //  Part 2: Connecting buttons to methods (event handling)
    // =====================================================================

    /**
     * Tells each button which method to run when it is clicked.
     * "e -> addStudent()" is a lambda: a short way of writing an ActionListener.
     */
    private void connectButtonsToActions() {
        addButton.addActionListener(e -> addStudent());
        updateButton.addActionListener(e -> updateStudent());
        deleteButton.addActionListener(e -> deleteStudent());
        clearButton.addActionListener(e -> clearForm());
        searchButton.addActionListener(e -> searchStudent());
        showAllButton.addActionListener(e -> showAllStudents());

        // Pressing Enter inside the search box also searches.
        searchField.addActionListener(e -> searchStudent());

        // When the user clicks a row in the table, copy that student into the form.
        studentTable.getSelectionModel().addListSelectionListener(e -> fillFormFromSelectedRow());
    }

    // =====================================================================
    //  Part 3: What each button does
    // =====================================================================

    /**
     * "Add Student" button: validate the form, then insert a new row in MySQL.
     */
    private void addStudent() {
        if (selectedRollNumber != -1) {
            showWarning("You have selected an existing student.\nClick \"Clear\" first to enter a new student.");
            return;
        }

        if (!isFormValid()) {
            return;
        }

        Student student = readStudentFromForm();

        try {
            boolean added = studentDAO.addStudent(student);

            if (added) {
                clearForm(); // also reloads the table
                setStatus("Student with roll number " + student.getRollNumber() + " was added successfully.");
                showInfo("Student added successfully.");
            } else {
                showError("The student could not be added. Please try again.");
            }
        } catch (SQLException e) {
            showDatabaseError("adding the student", e);
        }
    }

    /**
     * "Update Student" button: save the edited details of the selected student.
     */
    private void updateStudent() {
        if (selectedRollNumber == -1) {
            showWarning("Please select a student from the table first.");
            return;
        }

        if (!isFormValid()) {
            return;
        }

        // The roll number field is locked while a student is selected,
        // so this Student object keeps the original roll number.
        Student student = readStudentFromForm();

        try {
            boolean updated = studentDAO.updateStudent(student);

            if (updated) {
                clearForm();
                setStatus("Student with roll number " + student.getRollNumber() + " was updated successfully.");
                showInfo("Student updated successfully.");
            } else {
                showError("No student found with roll number " + student.getRollNumber()
                        + ".\nIt may have been deleted. Click \"Show All\" to refresh the list.");
            }
        } catch (SQLException e) {
            showDatabaseError("updating the student", e);
        }
    }

    /**
     * "Delete Student" button: ask for confirmation, then delete the selected student.
     */
    private void deleteStudent() {
        if (selectedRollNumber == -1) {
            showWarning("Please select a student from the table first.");
            return;
        }

        int rollNumberToDelete = selectedRollNumber;

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the student with roll number " + rollNumberToDelete + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        // Do nothing unless the user clicked "Yes".
        if (choice != JOptionPane.YES_OPTION) {
            setStatus("Delete cancelled.");
            return;
        }

        try {
            boolean deleted = studentDAO.deleteStudent(rollNumberToDelete);

            if (deleted) {
                clearForm();
                setStatus("Student with roll number " + rollNumberToDelete + " was deleted.");
                showInfo("Student deleted successfully.");
            } else {
                showError("No student found with roll number " + rollNumberToDelete
                        + ".\nIt may already have been deleted.");
                loadAllStudents();
            }
        } catch (SQLException e) {
            showDatabaseError("deleting the student", e);
        }
    }

    /**
     * "Search" button: show only the student with the roll number typed in the search box.
     */
    private void searchStudent() {
        String searchText = searchField.getText().trim();

        if (InputValidator.isEmpty(searchText)) {
            showWarning("Please enter a roll number to search for.");
            return;
        }

        if (!InputValidator.isValidRollNumber(searchText)) {
            showWarning("Roll number must be a positive whole number, for example 101.");
            return;
        }

        int rollNumber = Integer.parseInt(searchText);

        try {
            Student student = studentDAO.getStudentByRollNumber(rollNumber);

            if (student == null) {
                setStatus("No student found with roll number " + rollNumber + ".");
                showInfo("No student found with roll number " + rollNumber + ".");
            } else {
                ArrayList<Student> searchResult = new ArrayList<>();
                searchResult.add(student);
                showStudentsInTable(searchResult);
                setStatus("Showing search result for roll number " + rollNumber + ". Click \"Show All\" to see everyone.");
            }
        } catch (SQLException e) {
            showDatabaseError("searching for the student", e);
        }
    }

    /**
     * "Show All" button: clear the search box and show every student again.
     */
    private void showAllStudents() {
        searchField.setText("");
        loadAllStudents();
    }

    /**
     * "Clear" button: empty the form, forget the selected student and show all students.
     * Also called after a successful add, update or delete.
     */
    private void clearForm() {
        rollNumberField.setText("");
        fullNameField.setText("");
        courseField.setText("");
        emailField.setText("");
        marksField.setText("");
        searchField.setText("");

        // Unlock the roll number field again so a new student can be entered.
        rollNumberField.setEditable(true);
        selectedRollNumber = -1;
        studentTable.clearSelection();

        loadAllStudents();
    }

    // =====================================================================
    //  Part 4: Helper methods used by the buttons
    // =====================================================================

    /**
     * Reads all students from MySQL and shows them in the table.
     * Public because Main calls it once when the application starts.
     */
    public void loadAllStudents() {
        try {
            ArrayList<Student> students = studentDAO.getAllStudents();
            showStudentsInTable(students);

            if (students.isEmpty()) {
                setStatus("No student records found. Fill in the form and click \"Add Student\" to add one.");
            } else {
                setStatus("Showing all students: " + students.size() + " record(s).");
            }
        } catch (SQLException e) {
            showStudentsInTable(new ArrayList<>());
            showDatabaseError("loading the student records", e);
        }
    }

    /**
     * Replaces the rows of the table with the given list of students.
     */
    private void showStudentsInTable(ArrayList<Student> students) {
        tableModel.setRowCount(0); // remove all old rows
        displayedStudents = students;

        for (Student student : students) {
            Object[] row = {
                student.getRollNumber(),
                student.getFullName(),
                student.getCourse(),
                student.getEmail(),
                student.getMarks()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * Runs when the user selects a row in the table.
     * Copies that student's details into the form so they can be edited or deleted.
     */
    private void fillFormFromSelectedRow() {
        int selectedRow = studentTable.getSelectedRow();

        // -1 means no row is selected (for example, right after the table was refreshed).
        if (selectedRow == -1) {
            return;
        }

        Student student = displayedStudents.get(selectedRow);
        selectedRollNumber = student.getRollNumber();

        rollNumberField.setText(String.valueOf(student.getRollNumber()));
        fullNameField.setText(student.getFullName());
        courseField.setText(student.getCourse());
        emailField.setText(student.getEmail());
        marksField.setText(String.valueOf(student.getMarks()));

        // The roll number identifies the student, so it cannot be changed during an update.
        rollNumberField.setEditable(false);

        setStatus("Selected roll number " + selectedRollNumber + ". Edit the details and click \"Update Student\", or click \"Delete Student\".");
    }

    /**
     * Checks every form field. Shows a warning and returns false if something is wrong.
     */
    private boolean isFormValid() {
        String rollNumberText = rollNumberField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String course = courseField.getText().trim();
        String email = emailField.getText().trim();
        String marksText = marksField.getText().trim();

        if (InputValidator.isEmpty(rollNumberText) || InputValidator.isEmpty(fullName)
                || InputValidator.isEmpty(course) || InputValidator.isEmpty(marksText)) {
            showWarning("Please fill in all required fields:\nRoll Number, Full Name, Course and Marks.");
            return false;
        }

        if (!InputValidator.isValidRollNumber(rollNumberText)) {
            showWarning("Roll Number must be a positive whole number, for example 101.");
            return false;
        }

        if (!InputValidator.isValidMarks(marksText)) {
            showWarning("Marks must be a number between 0 and 100, for example 85 or 72.5.");
            return false;
        }

        if (!InputValidator.isValidEmail(email)) {
            showWarning("Please enter a valid email address (for example name@example.com),\nor leave the Email field empty.");
            return false;
        }

        if (InputValidator.isTooLong(fullName, MAX_NAME_LENGTH)
                || InputValidator.isTooLong(course, MAX_COURSE_LENGTH)
                || InputValidator.isTooLong(email, MAX_EMAIL_LENGTH)) {
            showWarning("Full Name and Course can have at most 100 characters,\nand Email at most 150 characters.");
            return false;
        }

        return true;
    }

    /**
     * Creates a Student object from the form. Call isFormValid() first,
     * otherwise parseInt() or parseDouble() could fail.
     */
    private Student readStudentFromForm() {
        int rollNumber = Integer.parseInt(rollNumberField.getText().trim());
        String fullName = fullNameField.getText().trim();
        String course = courseField.getText().trim();
        String email = emailField.getText().trim();
        double marks = Double.parseDouble(marksField.getText().trim());

        return new Student(rollNumber, fullName, course, email, marks);
    }

    /**
     * Turns a database error into a message the user can understand.
     * The technical details are printed to the terminal for the developer.
     */
    private void showDatabaseError(String action, SQLException e) {
        System.err.println("Database error while " + action + ": " + e.getMessage()
                + " [error code " + e.getErrorCode() + ", SQL state " + e.getSQLState() + "]");

        String sqlState = e.getSQLState();
        String message;

        if (e.getErrorCode() == 1062) {
            message = "A student with this roll number already exists.\nPlease use a different roll number.";
        } else if (e.getErrorCode() == 1045) {
            message = "MySQL rejected the username or password.\nCheck db.username and db.password in db.properties.";
        } else if (e.getErrorCode() == 1049) {
            message = "The database 'student_management' does not exist.\nRun the database.sql script in MySQL first.";
        } else if (e.getErrorCode() == 1146) {
            message = "The table 'students' does not exist.\nRun the database.sql script in MySQL first.";
        } else if (sqlState != null && sqlState.startsWith("08")) {
            // SQL states starting with "08" mean the connection itself failed.
            message = "Cannot connect to MySQL.\nMake sure the MySQL server is running and db.url in db.properties is correct.";
        } else {
            message = "Database error: " + e.getMessage();
        }

        setStatus("Error while " + action + ". See the message for details.");
        showError(message);
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Please Check", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
