package top.liebes.env;

import org.eclipse.jdt.core.dom.AST;

/**
 * @author liebes
 */
public class Env {
    public static String SOURCE_FOLDER =
//            "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/aeminium/fibonacci";
//            "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/working_examples";
//            "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/aeminium/blackscholes";
            "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/util/bitset";

    public final static String[] CLASSPATH = {
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/jre/lib/rt.jar",
            ".",
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/lib/dt.jar",
            "/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/lib/tools.jar",
    };

    public final static String TARGET_FOLDER =
            "/Users/liebes/project/laboratory/Sip4J/out/";

    public final static Integer JAVA_VERSION = AST.JLS3;

}
