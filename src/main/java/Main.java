import inspector.DuplicateStringsInspector;
import inspector.InspectionException;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

public class Main {

    private static final PrintStream OUT = System.out;

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (InspectionException e) {
            OUT.println(e.getMessage());
            e.printStackTrace(OUT);
        }
    }

    private void run() throws InspectionException{
        try {
            Heap heap = HeapFactory.createHeap(new File("C:\\Users\\denis\\Documents\\programming\\tmp\\new_dump.hprof"));
            DuplicateStringsInspector duplicateStringsInspector = new DuplicateStringsInspector(heap);
            duplicateStringsInspector.inspect();
        } catch (FileNotFoundException e) {
            throw new InspectionException("HProf file not found: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new InspectionException("I/O error when working with HProf file" + e.getMessage(), e);
        }
    }
}
