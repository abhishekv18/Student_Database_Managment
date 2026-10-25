package studentmanagement;

/**
 * Represents one student record.
 *
 * Each Student object holds the same five values that one row of the
 * "students" table in MySQL holds.
 */
public class Student {

    // The fields are private so other classes can only read them through getters (encapsulation).
    private int rollNumber;
    private String fullName;
    private String course;
    private String email;
    private double marks;

    /**
     * Creates a new Student object with all its details.
     * "this.rollNumber" means the field of this object, while "rollNumber" is the parameter.
     */
    public Student(int rollNumber, String fullName, String course, String email, double marks) {
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.course = course;
        this.email = email;
        this.marks = marks;
    }

    public int getRollNumber() {
        return rollNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public String getCourse() {
        return course;
    }

    public String getEmail() {
        return email;
    }

    public double getMarks() {
        return marks;
    }
}
