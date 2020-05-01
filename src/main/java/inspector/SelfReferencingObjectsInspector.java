package inspector;

import org.netbeans.lib.profiler.heap.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SelfReferencingObjectsInspector implements Inspector{

    private static final String SEPARATOR = System.lineSeparator();

    private final Heap heap;

    private final Writer out;

    private final Map<String, Integer> selfReferencing= new HashMap<>();

    public SelfReferencingObjectsInspector(Heap heap, Writer writer) {
        this.heap = Objects.requireNonNull(heap);
        this.out = Objects.requireNonNull(writer);
    }

    //fot test
    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\denis\\Documents\\programming\\dumps\\dump_self.hprof";
        Heap givenHeap = HeapFactory.createHeap(new File(path));
        new SelfReferencingObjectsInspector(givenHeap, new OutputStreamWriter(System.out, StandardCharsets.UTF_8)).run();
    }

    private void run() {
        fillList();
    }

    @Override
    public void inspect() throws InspectionException {
        fillList();
        writeInspection();
    }

    private void writeInspection() {

    }

    @SuppressWarnings("ConstantConditions")
    private void fillList() {
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
                            selfReferencing.putIfAbsent(className, 0);
                            selfReferencing.compute(className, (k, v) -> v + 1);
                        }
                    }
                }
            }
        }
    }
}
