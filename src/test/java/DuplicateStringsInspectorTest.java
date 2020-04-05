import inspector.DuplicateStringsInspector;
import inspector.InspectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.eq;

import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DuplicateStringsInspectorTest {


    private static final int SOME_INT = 1;
    private static final Random random = new Random();
    private static final String SEPARATOR = System.lineSeparator();
    private static final String INSPECTION_HEADER = "DuplicateStringInspector inspection";


    private Heap heapMock = mock(Heap.class);
    private Writer writerMock = mock(Writer.class);

    @BeforeEach
    public void initMocks() {
        heapMock = mock(Heap.class);
        writerMock = mock(Writer.class);
    }

    @Test
    public void nullToConstructors() {
        assertThrows(NullPointerException.class, () -> new DuplicateStringsInspector(null));
        assertThrows(NullPointerException.class, () -> new DuplicateStringsInspector(heapMock, null));
        assertThrows(NullPointerException.class, () -> new DuplicateStringsInspector(null, writerMock));
        assertThrows(NullPointerException.class, () -> new DuplicateStringsInspector(null, null, SOME_INT));
        assertThrows(NullPointerException.class, () -> new DuplicateStringsInspector(heapMock, null, SOME_INT));
        assertThrows(NullPointerException.class, () -> new DuplicateStringsInspector(null, writerMock, SOME_INT));
    }

    @Test
    public void NonNegativeThreshold() {
        assertDoesNotThrow(() -> new DuplicateStringsInspector(heapMock, writerMock, 0));
        assertDoesNotThrow(() -> new DuplicateStringsInspector(heapMock, writerMock, 1));
        assertDoesNotThrow(() -> new DuplicateStringsInspector(heapMock, writerMock, random.nextInt(1000) + 100));
        assertThrows(InspectionException.class, () -> new DuplicateStringsInspector(heapMock, writerMock, -1));
        assertThrows(InspectionException.class, () -> new DuplicateStringsInspector(heapMock, writerMock, -random.nextInt(1000) + 100));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void emptyHeapGet() {
        StringWriter writer = new StringWriter();
        Iterator<Instance> iteratorMock = mock(Iterator.class);
        when(iteratorMock.hasNext()).thenReturn(false);
        when(heapMock.getAllInstancesIterator()).thenReturn(iteratorMock);
        DuplicateStringsInspector inspector = new DuplicateStringsInspector(heapMock, writer);
        assertDoesNotThrow(inspector::inspect);
        String expected = SEPARATOR + INSPECTION_HEADER + SEPARATOR + "No significant amounts of duplicates found" + SEPARATOR;
        assertEquals(expected, writer.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noStringsHeap() {
        StringWriter writer = new StringWriter();
        Iterator<Instance> iteratorMock = (Iterator<Instance>) mock(Iterator.class);
        Instance instanceMock = mock(Instance.class);
        JavaClass javaClassMock = mock(JavaClass.class);

        when(iteratorMock.hasNext()).thenReturn(true, true, true, false);
        when(iteratorMock.next()).thenReturn(instanceMock);
        when(instanceMock.getJavaClass()).thenReturn(javaClassMock);
        when(javaClassMock.getName()).thenReturn("not string class");
        when(heapMock.getAllInstancesIterator()).thenReturn(iteratorMock);

        DuplicateStringsInspector inspector = new DuplicateStringsInspector(heapMock, writer);
        assertDoesNotThrow(inspector::inspect);
        String expected = SEPARATOR + INSPECTION_HEADER + SEPARATOR + "No significant amounts of duplicates found" + SEPARATOR;
        assertEquals(expected, writer.toString());
    }


}
