package studentmanagement;

import javax.swing.SwingUtilities;

/**
 * Starting point of the application.
 */
public class Main {

    public static void main(String[] args) {
        // Swing windows should be created on the Event Dispatch Thread (EDT).
        // invokeLater() asks Swing to run startApplication() on that thread.
        SwingUtilities.invokeLater(() -> startApplication());
    }

    private static void startApplication() {
        StudentManagementUI window = new StudentManagementUI();
        window.setVisible(true);

        // Load the student records from MySQL as soon as the window is shown.
        window.loadAllStudents();
    }
}
