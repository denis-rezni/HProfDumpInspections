package inspector;

/**
 * Abstract class for Inspector classes, which inspect a certain heap, searching for memory misuses.
 */
public interface Inspector {

    /**
     * Inspects a given a heap, then writes an inspection message.
     */
    void inspect();
}
