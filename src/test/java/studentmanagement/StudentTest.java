package studentmanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Checks that the Student constructor stores the values and the getters return them.
 */
public class StudentTest {

    @Test
    public void constructorStoresAllValues() {
        Student student = new Student(101, "Riya Sharma", "BCA", "riya@example.com", 88.5);

        assertEquals(101, student.getRollNumber());
        assertEquals("Riya Sharma", student.getFullName());
        assertEquals("BCA", student.getCourse());
        assertEquals("riya@example.com", student.getEmail());
        assertEquals(88.5, student.getMarks());
    }
}
