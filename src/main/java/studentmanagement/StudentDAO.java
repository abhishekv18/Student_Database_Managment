package studentmanagement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * DAO = Data Access Object.
 *
 * This is the ONLY class that contains SQL. The user interface calls these
 * methods instead of talking to the database directly.
 *
 * Every method opens a connection, runs one SQL query and closes the
 * connection again. If something goes wrong, the SQLException is passed
 * to the caller (the UI), which shows a friendly message.
 */
public class StudentDAO {

    /**
     * Inserts a new student. Returns true if one row was inserted.
     * If the roll number already exists, MySQL throws an error (duplicate primary key).
     */
    public boolean addStudent(Student student) throws SQLException {
        String sql = "INSERT INTO students (roll_number, full_name, course, email, marks) VALUES (?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Each ? is replaced by a value. The numbers 1-5 are the positions of the ? marks.
            statement.setInt(1, student.getRollNumber());
            statement.setString(2, student.getFullName());
            statement.setString(3, student.getCourse());
            statement.setString(4, student.getEmail());
            statement.setDouble(5, student.getMarks());

            // executeUpdate() is used for INSERT, UPDATE and DELETE. It returns the number of rows changed.
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        }
    }

    /**
     * Returns every student in the table, ordered by roll number.
     * Returns an empty list if the table has no rows.
     */
    public ArrayList<Student> getAllStudents() throws SQLException {
        String sql = "SELECT roll_number, full_name, course, email, marks FROM students ORDER BY roll_number";
        ArrayList<Student> students = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            // resultSet.next() moves to the next row and returns false when there are no more rows.
            while (resultSet.next()) {
                students.add(createStudentFromRow(resultSet));
            }
        }

        return students;
    }

    /**
     * Finds one student by roll number.
     * Returns the Student, or null if no student has that roll number.
     */
    public Student getStudentByRollNumber(int rollNumber) throws SQLException {
        String sql = "SELECT roll_number, full_name, course, email, marks FROM students WHERE roll_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, rollNumber);

            // executeQuery() is used for SELECT. It returns a ResultSet containing the rows found.
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return createStudentFromRow(resultSet);
                }
            }
        }

        return null;
    }

    /**
     * Updates the name, course, email and marks of an existing student.
     * The roll number is used to find the student and is never changed.
     * Returns true if a student with that roll number was found and updated.
     */
    public boolean updateStudent(Student student) throws SQLException {
        String sql = "UPDATE students SET full_name = ?, course = ?, email = ?, marks = ? WHERE roll_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, student.getFullName());
            statement.setString(2, student.getCourse());
            statement.setString(3, student.getEmail());
            statement.setDouble(4, student.getMarks());
            statement.setInt(5, student.getRollNumber());

            int rowsUpdated = statement.executeUpdate();
            return rowsUpdated > 0;
        }
    }

    /**
     * Deletes the student with the given roll number.
     * Returns true if a student was deleted.
     */
    public boolean deleteStudent(int rollNumber) throws SQLException {
        String sql = "DELETE FROM students WHERE roll_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, rollNumber);

            int rowsDeleted = statement.executeUpdate();
            return rowsDeleted > 0;
        }
    }

    /**
     * Converts the current row of a ResultSet into a Student object.
     * Used by getAllStudents() and getStudentByRollNumber() so this code is not repeated.
     */
    private Student createStudentFromRow(ResultSet resultSet) throws SQLException {
        int rollNumber = resultSet.getInt("roll_number");
        String fullName = resultSet.getString("full_name");
        String course = resultSet.getString("course");
        String email = resultSet.getString("email");
        double marks = resultSet.getDouble("marks");

        return new Student(rollNumber, fullName, course, email, marks);
    }
}
