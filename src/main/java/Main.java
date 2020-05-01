import inspector.DuplicateStringsInspector;
import inspector.InspectionException;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.HeapFactory;

import java.io.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Main class for HProfDumpInspections
 * Provides a command-line utility to inspect hprof dumps.
 * @author Denis Reznichenko
 */
public class Main {
    private final static String SEPARATOR = System.lineSeparator();
    private final static String USAGE_MESSAGE = "Usage: java -jar program.jar [-help | path inspection1 [inspection2 ...]]. " +
            "To get the list of inspections use -help.";
    private final static String HELP_MESSAGE = "Inspects Hprof dumps. Prints inspection message to standard output. List of inspections:" + SEPARATOR +
            "-ds : Duplicate Strings inspection. Searches for duplicate strings in the dump." +
            " Amounts less than threshold (10 by default) are ignored.";

    private final static PrintStream OUT = System.out;
    private final static Map<String, Inspection> argToInspection = new HashMap<>();
    static {
        argToInspection.put("-ds", Inspection.DUPLICATE_STRINGS);
    }

    private static final EnumSet<Inspection> inspections = EnumSet.noneOf(Inspection.class);
    private static String path;

    /** Main method for inspecting dumps.
     * Command line utility for inspecting Hprof dumps. <br>
     * {@value USAGE_MESSAGE} <br>
     * path - full path to the dump file <br>
     * inspections - list of inspections a user wants to see.
     * <uL>
     *     <li> -ds : duplicate Strings inspection</li>
     * </uL>
     *
     *
     * Inspection is written to the {@code OUT} {@link PrintStream} (System.out by default).
     * In case of any exception writes the exception message to the {@code OUT} {@link PrintStream}.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            new Main().run(args);
        } catch (InspectionException e) {
            OUT.println(e.getMessage());
            e.printStackTrace(OUT);
        }
    }

    /**
     * Runs the command line utility. A non-static main method.
     * @param args command line args
     * @throws InspectionException if the inspection fails
     */
    private void run(String[] args) throws InspectionException {
        try {
            if (checkUsageOrHelp(args)) {
                return;
            }
            parseArgs(args);
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
        } catch (IllegalArgumentException e) {
            throw new InspectionException("Command line arguments are invalid: "
                    + e.getMessage() + SEPARATOR + USAGE_MESSAGE, e);
        }
    }

    /**
     * Method that check if a user wants a usage method or -help message.
     * @param args command line arguments
     * @return {@code true}, if usage or -help messages are intended. {@code false}, otherwise.
     */
    private boolean checkUsageOrHelp(String[] args) {
        if (args == null || args.length == 0) {
            OUT.println(USAGE_MESSAGE);
            return true;
        }
        if (args.length == 1 && args[0] != null && args[0].equals("-help")) {
            OUT.println(HELP_MESSAGE);
            return true;
        }
        return false;
    }

    /**
     * Parses the given args array.
     * @param args command line arguments
     * @throws IllegalArgumentException if the given args array isn't a valid argument array
     */
    private void parseArgs(String[] args) throws IllegalArgumentException, InspectionException {
        if (args == null || args.length < 2) {
            throw new InspectionException(USAGE_MESSAGE);
        }
        if (args[0] == null || args[0].isEmpty()) {
            throw new IllegalArgumentException("Non-null and non-empty path expected as first argument");
        }
        path = args[0];
        for (int i = 1; i < args.length; i++) {
            String arg = args[i];
            Inspection inspection = argToInspection.get(arg);
            if (inspection != null) {
                if (!inspections.contains(inspection)) {
                    inspections.add(inspection);
                } else {
                    throw new IllegalArgumentException("Same inspections are not allowed: " + arg);
                }
            } else {
                throw new IllegalArgumentException("Invalid inspection: " + arg);
            }
        }
    }

    /**
     * Class enumerating inspections.
     */
    private enum Inspection {
        DUPLICATE_STRINGS
    }
}
