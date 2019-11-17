package top.liebes.graph.pdg;

import org.eclipse.jdt.core.dom.*;
import org.junit.Test;
import top.liebes.env.Env;

import static org.junit.Assert.*;

public class GraphTest {

    @Test
    public void test(){
        String s = "            stream.read();\n" +
                "            stream.markSupported();\n" +
                "            stream.available();\n" +
                "            stream.mark(100);\n" +
                "            stream.reset();\n" +
                "            stream.skip(1000);\n" +
                "            stream.read3(bytes, 0, 10000);\n" +
                "            stream.read3(bytes, 30000, 70000);\n" +
                "            stream.read3(bytes, 7, 40000);\n" +
                "            stream.read3(bytes, 90000, 99000);";
        String[] arr = s.split(";\n");
        String res = "";
        for(int i = 0; i < arr.length; i ++){
            res += "case " + i + ":\n" + arr[i].trim() + ";\nbreak;\n";
        }
        res += "default:\nbreak;\n";
        res = String.format("int id = random.nextInt(%d);\nswitch (id){\n%s}", arr.length, res);
        System.out.println(res);
    }

    @Test
    public void print() {
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        String s = "package working_examples;\n" +
                "\n" +
                "public class Test{\n" +
                "\n" +
                "    public void test(){\n" +
                "        int a = 1, d = 10;\n" +
                "        int b = 2;\n" +
                "        int c = 2;\n" +
                "        a = b;\n" +
                "        while(a != 0){\n" +
                "            c ++;\n" +
                "        }\n" +
                "        if(c == 2){\n" +
                "            a = c;\n" +
                "        }\n" +
                "        else{\n" +
                "            a = d;\n" +
                "        }\n" +
                "    }\n" +
                "}";
        parser.setSource(s.toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setResolveBindings(true);
        String unitName = "Test.java";
        parser.setUnitName(unitName);
        String[] sources = { "/Users/liebes/project/laboratory/Sip4J/benchmarks/src/working_examples" };
        parser.setEnvironment(Env.CLASSPATH, sources, new String[]{"UTF-8"}, true);
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//        System.out.println(cu);
        MethodDeclaration declaration = (MethodDeclaration)((TypeDeclaration) cu.types().get(0)).bodyDeclarations().get(0);;
//        System.out.println(declaration);
        Graph graph = GraphFactory.createGraphFromMethod(declaration);
        graph.print();
    }
}