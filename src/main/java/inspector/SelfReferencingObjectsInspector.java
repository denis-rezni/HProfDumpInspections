package inspector;

import org.netbeans.lib.profiler.heap.Field;
import org.netbeans.lib.profiler.heap.FieldValue;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Inspector, which finds self-referencing objects.
 * Finds self-referencing objects in a specified {@link Heap}.
 * Human-readable inspection message is written to a given {@link Writer}.
 * If the {@link Writer} isn't specified, writes to System.out.
 *
 * @author Denis Reznichenko
 */
@SuppressWarnings("unchecked")
public class SelfReferencingObjectsInspector extends Inspector {

    private static final String SEPARATOR = System.lineSeparator();

    private final Heap heap;

    private final Writer out;

    private final Map<String, Long> selfReferencingCount = new HashMap<>();

    private long threshold = 1;

    /**
     * Sets a value, amounts of self referencing objects over which are considered significant.
     * @param threshold value to be set
     * @throws InspectionException if parameter is negative
     */
    @SuppressWarnings("unused")
    public void setThreshold(long threshold) throws InspectionException {
        checkThreshold(threshold);
        this.threshold = threshold;
    }

    /**
     * Creates an instance of {@link SelfReferencingObjectsInspector}.
     * Results are printed to standard output, threshold is 1 by default.
     * @param heap {@link Heap} to be inspected.
     * @throws NullPointerException if any of the arguments is null
     */
    @SuppressWarnings("unused")
    public SelfReferencingObjectsInspector(Heap heap) {
        this.heap = Objects.requireNonNull(heap);
        this.out = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
    }

    /**
     * Creates an instance of {@link SelfReferencingObjectsInspector}.
     * Results are printed to the specified {@link Writer}, threshold is 1 by default.
     * @param heap {@link Heap} to be inspected
     * @throws NullPointerException if any of the arguments is null
     */
    @SuppressWarnings("unused")
    public SelfReferencingObjectsInspector(Heap heap, Writer writer) {
        this.heap = Objects.requireNonNull(heap);
        this.out = Objects.requireNonNull(writer);
    }

    /**
     * Creates an instance of {@link SelfReferencingObjectsInspector}.
     * Results are printed to the specified {@link Writer}, threshold is also specified.
     * @param heap {@link Heap} to be inspected
     * @throws InspectionException if threshold value is negative
     * @throws NullPointerException if any of the arguments is null
     */
    @SuppressWarnings("unused")
    public SelfReferencingObjectsInspector(Heap heap, Writer writer, long threshold) throws InspectionException {
        checkThreshold(threshold);
        this.heap = Objects.requireNonNull(heap);
        this.out = Objects.requireNonNull(writer);
        this.threshold = threshold;
    }

    private void checkThreshold(long threshold) throws InspectionException {
        if (threshold < 0) {
            throw new InspectionException("Non-negative number expected as threshold");
        }
    }

    @Override
    public void inspect() throws InspectionException {
        try (out) {
            fillMap();
            writeInspection();
        } catch (IOException e) {
            throw new InspectionException("IO error when closing writer: " + e.getMessage(), e);
        }
    }

    private void writeInspection() throws InspectionException {
        try {
            out.append(getInspectionHeader("SelfReferencingObjectsInspector"));
            boolean hasSelfRefs = false;
            for (Map.Entry<String, Long> entry : selfReferencingCount.entrySet()) {
                if (entry.getValue() >= threshold) {
                    hasSelfRefs = true;
                    out.append("\"")
                            .append(entry.getKey())
                            .append("\"")
                            .append(" class has ")
                            .append(String.valueOf(entry.getValue()))
                            .append(" self references");
                    out.append(SEPARATOR);
                }
            }
            if (!hasSelfRefs) {
                out.append("No significant amounts of self referencing objects found").append(SEPARATOR);
            }
        } catch (IOException e) {
            throw new InspectionException("I/O exception occurred while writing the SelfReferencingObjectsInspector inspection message: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void fillMap() {
        Iterator<Instance> iterator = heap.getAllInstancesIterator();
        while (iterator.hasNext()) {
            Instance instance = iterator.next();
            List<Field> fieldValues = ((List<FieldValue>) instance.getFieldValues())
                    .stream()
                    .map(FieldValue::getField)
                    .collect(Collectors.toList());
            for (Field value : fieldValues) {
                if (value.getDeclaringClass().getName().equals(instance.getJavaClass().getName())) {
                    Object valueOfFieldObject = instance.getValueOfField(value.getName());
                    if (valueOfFieldObject instanceof Instance) {
                        Instance valueOfField = (Instance) valueOfFieldObject;
                        if (valueOfField.getInstanceId() == instance.getInstanceId()) {
                            String className = instance.getJavaClass().getName();
                            selfReferencingCount.putIfAbsent(className, 0L);
                            selfReferencingCount.compute(className, (k, v) -> v + 1);
                        }
                    }
                }
            }
        }
    }
}
