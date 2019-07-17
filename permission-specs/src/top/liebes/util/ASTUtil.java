package top.liebes.util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import top.liebes.env.Env;

import java.io.File;

/**
 * @author Liebes Wong
 */
public class ASTUtil {

    public static CompilationUnit getCompilationUnit(File file){
        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

        parser.setUnitName(FileUtil.removeSuffix(file.getName()));

        String[] sources = { Env.SOURCE_FOLDER};
        String[] classpath = Env.CLASSPATH;

        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(FileUtil.getFileContents(file));

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        return cu;
    }
}
