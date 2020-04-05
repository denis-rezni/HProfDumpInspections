package inspector;

/**
 * Internal {@link Exception}, thrown when inspecting fails.
 */
public class InspectionException extends Exception {
    public InspectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
