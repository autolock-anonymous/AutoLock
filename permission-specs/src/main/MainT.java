package main;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import top.liebes.util.FileUtil;

public class MainT {

    public static void main(String[] args) {

        String folder = "/Users/liebes/Downloads/sip4j/runtime-sip4j-application/benchmarks/src/aeminium/fibonacci";
//        File root = new File(folder);
//        List<File> files = Main.getFiles(root, new String[]{"java"});
//        for(File file : files){
//
//        }
        File file = new File(folder + "/Fibonacci.java");
//        String str = Util.readFileToString(file);

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setBindingsRecovery(true);

        Map options = JavaCore.getOptions();
        parser.setCompilerOptions(options);

        String unitName = "Apple.java";
        parser.setUnitName(unitName);

        String[] sources = { "/Users/liebes/Downloads/sip4j/runtime-sip4j-application/benchmarks/src" };
        String[] classpath = {"/Library/Java/JavaVirtualMachines/jdk1.8.0_192.jdk/Contents/Home/jre/lib/rt.jar"};

        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(FileUtil.getFileContents(file));

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);

        if (cu.getAST().hasBindingsRecovery()) {
            System.out.println("Binding activated.");
        }
        System.out.println(cu);
        TypeFinderVisitor v = new TypeFinderVisitor();
        cu.accept(v);
    }

}

class TypeFinderVisitor extends ASTVisitor{

    @Override
    public boolean visit(VariableDeclarationStatement node){
        for (Iterator iter = node.fragments().iterator(); iter.hasNext();) {
            System.out.println("------------------");

            VariableDeclarationFragment fragment = (VariableDeclarationFragment) iter.next();
            IVariableBinding binding = fragment.resolveBinding();

            System.out.println("binding variable declaration: " +binding.getVariableDeclaration());
            System.out.println("binding: " +binding);
        }
        return true;
    }

    @Override
    public boolean visit(TypeDeclaration node){
        if(node.resolveBinding() != null && ! node.isInterface()){
            ITypeBinding binding = node.resolveBinding();
            System.out.println(binding);
        }
        return super.visit(node);
    }
}