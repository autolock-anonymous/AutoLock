package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.slf4j.LoggerFactory;
import top.liebes.entity.Pair;
import top.liebes.env.Env;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liebes
 */
public class CompilationUnitASTRequestor extends FileASTRequestor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(CompilationUnitASTRequestor.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    private List<Pair<String, CompilationUnit>> fileList = new ArrayList<>();

    public List<Pair<String, CompilationUnit>> getFileList() {
        return fileList;
    }

    @Override
    public void acceptAST(String sourceFilePath, CompilationUnit ast) {
        fileList.add(Pair.make(sourceFilePath, ast));
        super.acceptAST(sourceFilePath, ast);
    }
}
