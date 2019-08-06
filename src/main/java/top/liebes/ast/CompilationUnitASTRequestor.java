package top.liebes.ast;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import top.liebes.entity.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liebes
 */
public class CompilationUnitASTRequestor extends FileASTRequestor {
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
