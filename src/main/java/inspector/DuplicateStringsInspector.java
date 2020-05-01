package inspector;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * Inspector, which finds all duplicate {@link String}s in a given {@link Heap}.
 * Human-readable inspection message is written to a given {@link Writer}.
 * If the {@link Writer} isn't specified, writes to System.out.
 *
 * @author Denis Reznichenko
 */
@SuppressWarnings("unchecked")
public class DuplicateStringsInspector extends Inspector {

    /**
     * Represents a class name for class {@link String}.
     */
    private static final String STRING = "java.lang.String";

    /**
     * Represents line separator, which is used when writing.
     */
    private static final String SEPARATOR = System.lineSeparator();

    /**
     * Heap, which is being inspected
     */
    private final Heap heap;

    /**
     * Stores {@link String} frequencies.
     * Entries are of such kind: < String from an instance from heap, it's frequency in the whole heap >.
     */
    private final Map<String, Long> frequencies = new HashMap<>();

    /**
     * {@link Writer}, to which the inspection message is printed.
     */
    private final Writer out;

    /**
     * Specifies the threshold, from which number of
     * String duplicates becomes significant and is printed to the inspection message.
     */
    private long threshold = 100;

    /**
     * @param heap heap for inspection
     * @throws NullPointerException if any of the arguments is null
     */
    @SuppressWarnings("unused")
    public DuplicateStringsInspector(Heap heap) {
        this.heap = Objects.requireNonNull(heap);
        out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
    }

    /**
     * @param heap   heap for inspection
     * @param writer inspection message is printed there
     * @throws NullPointerException if any of the arguments is null
     */
    public DuplicateStringsInspector(Heap heap, Writer writer) {
        this.heap = Objects.requireNonNull(heap);
        this.out = Objects.requireNonNull(writer);
    }

    /**
     * @param heap      heap for inspection
     * @param writer    inspection message is printed there
     * @param threshold threshold from which a number of duplicates counts as significant
     * @throws InspectionException if threshold is negative
     * @throws NullPointerException if any of the arguments is null
     */
    @SuppressWarnings("unused")
    public DuplicateStringsInspector(Heap heap, Writer writer, long threshold) throws InspectionException {
        checkThreshold(threshold);
        this.heap = Objects.requireNonNull(heap);
        this.out = Objects.requireNonNull(writer);
        this.threshold = threshold;
    }

    /**
     * Sets {@code threshold} field.
     * @param threshold value to be set
     * @throws InspectionException if parameter is negative
     */
    @SuppressWarnings("unused")
    public void setThreshold(long threshold) throws InspectionException {
        checkThreshold(threshold);
        this.threshold = threshold;
    }

    private void checkThreshold(long threshold) throws InspectionException {
        if (threshold < 0) {
            throw new InspectionException("Non-negative number expected as threshold");
        }
    }


    public void inspect() throws InspectionException {
        try (out) {
            fillFrequencies();
            writeInspection();
        } catch (IOException e) {
            throw new InspectionException("IO error when closing writer: " + e.getMessage(), e);
        }
    }

    /**
     * Writes a human-readable inspection message using an earlier specified {@link Writer}.
     *
     * @throws InspectionException if writing fails due to a I/O error.
     */
    private void writeInspection() throws InspectionException {
        try {
            out.append(getInspectionHeader("DuplicateStringInspector"));
            boolean hasDuplicates = false;
            for (Map.Entry<String, Long> entry : frequencies.entrySet()) {
                if (entry.getValue() >= threshold) {
                    hasDuplicates = true;
                    out.append("\"")
                            .append(entry.getKey())
                            .append("\" : ")
                            .append(String.valueOf(entry.getValue()))
                            .append(" times");
                    out.append(SEPARATOR);
                }
            }
            if (!hasDuplicates) {
                out.append("No significant amounts of duplicates found").append(SEPARATOR);
            }
        } catch (IOException e) {
            throw new InspectionException("I/O exception occurred while writing the DuplicateStringInspector inspection message: " + e.getMessage(), e);
        }

    }

    /**
     * Fills {@code frequencies}. Walks through the heap using {@link Instance} iterator.
     */
    private void fillFrequencies() {
        Iterator<Instance> iterator = heap.getAllInstancesIterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            String className = instance.getJavaClass().getName();
            if (className.equals(STRING)) {
                PrimitiveArrayInstance byteValue = (PrimitiveArrayInstance) instance.getValueOfField("value");
                String corresponding = listToString((List<String>) byteValue.getValues());
                frequencies.putIfAbsent(corresponding, 0L);
                frequencies.computeIfPresent(corresponding, (k, v) -> v + 1);
            }
        }
    }

    /**
     * Converts a given char list to a string.
     * Made specifically for {@code getValues} method in {@link PrimitiveArrayInstance}.
     *
     * @param list list of {@link String}s, where each string is a numeric value of some {@code char}.
     * @return {@link String} representation of the list of chars
     */
    private String listToString(List<String> list) {
        char[] result = new char[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = (char) Integer.parseInt(list.get(i));
        }
        return new String(result);
    }

}
