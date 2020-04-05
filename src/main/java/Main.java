import inspector.DuplicateStringsInspector;
import inspector.InspectionException;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

public class Main {

    private static final PrintStream OUT = System.out;
    private static EnumSet<Inspection> inspections = EnumSet.of(Inspection.DUPLICATE_STRINGS);
    private static final String path = "";

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (InspectionException e) {
            OUT.println(e.getMessage());
            e.printStackTrace(OUT);
        }
    }

    private void run() throws InspectionException {
        try {
            Heap heap = HeapFactory.createHeap(new File(path));
            if (inspections.contains(Inspection.DUPLICATE_STRINGS)) {
                DuplicateStringsInspector duplicateStringsInspector
                        = new DuplicateStringsInspector(heap, new PrintWriter(OUT), 10L);
                duplicateStringsInspector.inspect();
            }
        } catch (FileNotFoundException e) {
            throw new InspectionException("HProf file not found: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new InspectionException("I/O error when working with HProf file" + e.getMessage(), e);
        }
    }

    enum Inspection {
        DUPLICATE_STRINGS
    }
}
