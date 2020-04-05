package inspector;

/**
 * Internal {@link Exception}, thrown when inspecting fails.
 */
class InspectionException extends Exception {
    InspectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
