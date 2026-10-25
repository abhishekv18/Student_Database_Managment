package studentmanagement;

/**
 * Simple checks for the text typed into the form.
 *
 * These methods do not use the database or the user interface, so they are
 * easy to test with JUnit (see InputValidatorTest).
 */
public class InputValidator {

    /**
     * Returns true if the text is missing or contains only spaces.
     */
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Returns true if the text is longer than the database column allows.
     */
    public static boolean isTooLong(String text, int maxLength) {
        return text != null && text.trim().length() > maxLength;
    }

    /**
     * A roll number must be a whole number greater than 0, for example "101".
     */
    public static boolean isValidRollNumber(String text) {
        if (isEmpty(text)) {
            return false;
        }

        try {
            int rollNumber = Integer.parseInt(text.trim());
            return rollNumber > 0;
        } catch (NumberFormatException e) {
            // The text was not a whole number, for example "abc" or "10.5".
            return false;
        }
    }

    /**
     * Marks must be a number from 0 to 100, for example "85" or "72.5".
     */
    public static boolean isValidMarks(String text) {
        if (isEmpty(text)) {
            return false;
        }

        try {
            double marks = Double.parseDouble(text.trim());
            return marks >= 0 && marks <= 100;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Email is optional, so an empty email is allowed.
     * Otherwise it needs an "@" with a "." somewhere after it, and no spaces.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return true;
        }

        String trimmedEmail = email.trim();
        int atPosition = trimmedEmail.indexOf('@');
        int lastDotPosition = trimmedEmail.lastIndexOf('.');

        if (trimmedEmail.contains(" ")) {
            return false;
        }

        // Something before "@", something between "@" and ".", and something after "."
        return atPosition > 0
                && lastDotPosition > atPosition + 1
                && lastDotPosition < trimmedEmail.length() - 1;
    }
}
