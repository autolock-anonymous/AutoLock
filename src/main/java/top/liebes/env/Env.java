package top.liebes.env;

import ch.qos.logback.classic.Level;
import org.eclipse.jdt.core.dom.AST;

/**
 * @author liebes
 */
public class Env {
    public static String SOURCE_FOLDER =
            "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/pulse-new";

    public static String[] CLASSPATH = {
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/jre/lib/rt.jar",
            ".",
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/lib/dt.jar",
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/lib/tools.jar",
            "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/pulse-new/antlr-3.3-complete.jar",
            "/Users/liebes/project/laboratory/Sip4j/lib/plaid-annotations.jar",
            "/Users/liebes/.p2/pool/plugins/org.eclipse.core.resources_3.13.400.v20190505-1655.jar",
            "/Users/liebes/.p2/pool/plugins/org.eclipse.jdt.core_3.18.0.v20190522-0428.jar",
            "/Users/liebes/.p2/pool/plugins/org.eclipse.core.runtime_3.15.300.v20190508-0543.jar",
    };

    public final static String TARGET_FOLDER =
//            "/Users/liebes/project/laboratory/Sip4JTest/src";
            "/Users/liebes/project/laboratory/Sip4J/out";

    public final static Integer JAVA_VERSION = AST.JLS8;

    public static Level LOG_LEVEL = Level.OFF;

    public final static String PERM_IMPORT_DECL = "top.liebes.anno.Perm";
}
