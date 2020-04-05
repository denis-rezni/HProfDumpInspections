package inspector;

import org.netbeans.lib.profiler.heap.*;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Inspector, which finds all duplicate {@link String}s in a given heap.
 * Human-readable inspection message is written to a given {@link Writer}.
 * If the {@link Writer} isn't specified, writes to System.out.
 *
 * @author Denis Reznichenko
 */
@SuppressWarnings("unchecked")
public class DuplicateStringsInspector implements Inspector {

    /**
     * Represents a class name for class {@link String}.
     */
    private static final String STRING = "java.lang.string";

    /**
     * Represents line separator, which is used when writing.
     */
    private static final String SEPARATOR = System.lineSeparator();

    /**
     * Heap, which is being inspected
     */
    private Heap heap;

    /**
     * Stores {@link String} frequencies.
     * Entries are of such kind: < String from an instance from heap, it's frequency in the whole heap >.
     */
    private Map<String, Long> frequencies;

    /**
     * {@link Writer}, to which the inspection message is printed.
     */
    private Writer out;

    /**
     * Specifies the threshold, from which number of
     * String duplicates becomes significant and is printed to the inspection message.
     */
    private long threshold = 100;

    /**
     * @param heap heap for inspection
     */
    @SuppressWarnings("unused")
    public DuplicateStringsInspector(Heap heap) {
        this.heap = Objects.requireNonNull(heap);
        out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        frequencies = new HashMap<>();
    }

    /**
     * @param heap   heap for inspection
     * @param writer inspection message is printed there
     */
    public DuplicateStringsInspector(Heap heap, Writer writer) {
        this.heap = Objects.requireNonNull(heap);
        out = Objects.requireNonNull(writer);
        frequencies = new HashMap<>();
    }

    /**
     * @param heap      heap for inspection
     * @param writer    inspection message is printed there
     * @param threshold threshold from which a number of duplicates counts as significant
     */
    @SuppressWarnings("unused")
    public DuplicateStringsInspector(Heap heap, Writer writer, long threshold) {
        new DuplicateStringsInspector(heap, writer);
        this.threshold = threshold;
    }


    /**
     * Searches for duplicate strings in a specified heap, then writes an inspection message.
     * In case of an exception, prints it with a cause, then prints stackTrace.
     */
    public void inspect() {
        fillFrequencies();
        try {
            writeInspection();
        } catch (InspectionException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Writes a human-readable inspection message using a specified {@link Writer}.
     *
     * @throws InspectionException if writing fails due to a I/O error.
     */
    private void writeInspection() throws InspectionException {
        try {
            out.append(SEPARATOR)
                    .append("DuplicateStringInspector inspection")
                    .append(SEPARATOR);
            boolean hasDuplicates = false;
            for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
                if (entry.getValue() >= threshold) {
                    hasDuplicates = true;
                    out.append(entry.getKey())
                            .append(" : ")
                            .append(String.valueOf(entry.getValue()))
                            .append(" times");
                    out.append(SEPARATOR);
                }
            }
            if (!hasDuplicates) {
                out.append("No significant amounts of duplicates found").append(SEPARATOR);
            }
        } catch (IOException e) {
            throw new InspectionException("I/O exception occurred while writing the inspection message: " + e.getMessage(), e);
        }

    }

    /**
     * Fills {@code frequencies}. Walks through the heap using {@link Instance} iterator.
     */
    private void fillFrequencies() {
        Iterator<Instance> iterator = heap.getAllInstancesIterator();
        while (iterator.hasNext()) {
            String className = iterator.next().getJavaClass().getName();
            if (className.equals(STRING)) {
                frequencies.putIfAbsent(className, 0L);
                frequencies.computeIfPresent(className, (k, v) -> v + 1);
            }
        }
    }

}
