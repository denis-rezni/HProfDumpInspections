import inspector.DuplicateStringsInspector;
import inspector.InspectionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.Instance;
import org.netbeans.lib.profiler.heap.JavaClass;
import org.netbeans.lib.profiler.heap.PrimitiveArrayInstance;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests are specified for current implementation, although might fall on a correct implementation of {@link DuplicateStringsInspector}
 */
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
    public void emptyHeapGet() throws InspectionException {
        StringWriter writer = new StringWriter();
        Iterator<Instance> iteratorMock = mock(Iterator.class);
        when(iteratorMock.hasNext()).thenReturn(false);
        when(heapMock.getAllInstancesIterator()).thenReturn(iteratorMock);
        DuplicateStringsInspector inspector = new DuplicateStringsInspector(heapMock, writer);
        inspector.inspect();
        String expected = SEPARATOR + INSPECTION_HEADER + SEPARATOR + "No significant amounts of duplicates found" + SEPARATOR;
        assertEquals(expected, writer.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void noStringsHeap() throws InspectionException {
        StringWriter writer = new StringWriter();
        Iterator<Instance> iteratorMock = mock(Iterator.class);
        Instance instanceMock = mock(Instance.class);
        JavaClass javaClassMock = mock(JavaClass.class);

        when(iteratorMock.hasNext()).thenReturn(true, true, true, true, false);
        when(iteratorMock.next()).thenReturn(instanceMock);
        when(instanceMock.getJavaClass()).thenReturn(javaClassMock);
        when(javaClassMock.getName()).thenReturn("not string class");
        when(heapMock.getAllInstancesIterator()).thenReturn(iteratorMock);

        DuplicateStringsInspector inspector = new DuplicateStringsInspector(heapMock, writer);
        inspector.inspect();
        String expected = SEPARATOR + INSPECTION_HEADER + SEPARATOR + "No significant amounts of duplicates found" + SEPARATOR;
        assertEquals(expected, writer.toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void oneStringOverThreshold() throws InspectionException {
        List<String> chars = List.of("115", "116", "114", "105", "110", "103");//"string"
        StringWriter writer = new StringWriter();
        Iterator<Instance> iteratorMock = mock(Iterator.class);
        Instance stringInstanceMock = mock(Instance.class);
        Instance noStringInstanceMock = mock(Instance.class);
        JavaClass stringJavaClassMock = mock(JavaClass.class);
        JavaClass noStringJavaClassMock = mock(JavaClass.class);
        PrimitiveArrayInstance arrayInstanceMock = mock(PrimitiveArrayInstance.class);


        when(iteratorMock.hasNext()).thenReturn(true, true, true, true, true, true, true, false);
        when(iteratorMock.next()).thenReturn(stringInstanceMock, stringInstanceMock, noStringInstanceMock, noStringInstanceMock,
                noStringInstanceMock, stringInstanceMock, stringInstanceMock);
        when(stringInstanceMock.getJavaClass()).thenReturn(stringJavaClassMock);
        when(noStringInstanceMock.getJavaClass()).thenReturn(noStringJavaClassMock);
        when(stringJavaClassMock.getName()).thenReturn("java.lang.String");
        when(noStringJavaClassMock.getName()).thenReturn("not java.lang.String");
        when(stringInstanceMock.getValueOfField("value")).thenReturn(arrayInstanceMock);
        when(arrayInstanceMock.getValues()).thenReturn(chars);
        when(heapMock.getAllInstancesIterator()).thenReturn(iteratorMock);

        DuplicateStringsInspector inspector = new DuplicateStringsInspector(heapMock, writer, 2);
        inspector.inspect();
        String expected = SEPARATOR + INSPECTION_HEADER + SEPARATOR + "\"string\" : 4 times" + SEPARATOR;
        assertEquals(expected, writer.toString());
    }


}
