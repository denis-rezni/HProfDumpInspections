package inspector;

/**
 * Abstract class for Inspector classes, which inspect a certain heap, searching for memory misuses.
 */
abstract class Inspector {

    String SEPARATOR = System.lineSeparator();

    /**
     * Inspects a given a heap, then writes an inspection message.
     */
    abstract void inspect() throws InspectionException;

    protected String getInspectionHeader(String inspector) {
        return SEPARATOR + inspector + " inspection" + SEPARATOR;
    }

}
