package studentmanagement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the form validation rules. These tests do not need MySQL.
 */
public class InputValidatorTest {

    @Test
    public void emptyTextIsDetected() {
        assertTrue(InputValidator.isEmpty(""));
        assertTrue(InputValidator.isEmpty("   "));
        assertTrue(InputValidator.isEmpty(null));
        assertFalse(InputValidator.isEmpty("Riya"));
    }

    @Test
    public void validRollNumbersAreAccepted() {
        assertTrue(InputValidator.isValidRollNumber("1"));
        assertTrue(InputValidator.isValidRollNumber("101"));
        assertTrue(InputValidator.isValidRollNumber(" 202 "));
    }

    @Test
    public void invalidRollNumbersAreRejected() {
        assertFalse(InputValidator.isValidRollNumber(""));
        assertFalse(InputValidator.isValidRollNumber("0"));
        assertFalse(InputValidator.isValidRollNumber("-5"));
        assertFalse(InputValidator.isValidRollNumber("abc"));
        assertFalse(InputValidator.isValidRollNumber("10.5"));
        assertFalse(InputValidator.isValidRollNumber("99999999999")); // too big for an int
    }

    @Test
    public void marksFromZeroToHundredAreAccepted() {
        assertTrue(InputValidator.isValidMarks("0"));
        assertTrue(InputValidator.isValidMarks("72.5"));
        assertTrue(InputValidator.isValidMarks("100"));
    }

    @Test
    public void marksOutsideTheRangeAreRejected() {
        assertFalse(InputValidator.isValidMarks("-1"));
        assertFalse(InputValidator.isValidMarks("100.5"));
        assertFalse(InputValidator.isValidMarks("ninety"));
        assertFalse(InputValidator.isValidMarks(""));
    }

    @Test
    public void emailIsOptionalButMustLookValid() {
        assertTrue(InputValidator.isValidEmail(""));
        assertTrue(InputValidator.isValidEmail("riya@example.com"));
        assertFalse(InputValidator.isValidEmail("riya.example.com"));
        assertFalse(InputValidator.isValidEmail("@example.com"));
        assertFalse(InputValidator.isValidEmail("riya@example"));
        assertFalse(InputValidator.isValidEmail("riya@.com"));
        assertFalse(InputValidator.isValidEmail("riya @example.com"));
    }

    @Test
    public void tooLongTextIsDetected() {
        assertFalse(InputValidator.isTooLong("Riya", 100));
        assertTrue(InputValidator.isTooLong("a".repeat(101), 100));
    }
}
