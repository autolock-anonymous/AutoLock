package top.liebes.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import top.liebes.util.ASTUtil;

public class ExperimentVisitor extends ASTVisitor {
    private int numberOfClass = 0;
    private int numberOfMethod = 0;
    private int numberOfLock = 0;

    public int getNumberOfClass() {
        return numberOfClass;
    }

    public int getNumberOfMethod() {
        return numberOfMethod;
    }

    public int getNumberOfLock() {
        return numberOfLock;
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        numberOfClass ++;
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        numberOfMethod ++;
        return super.visit(node);
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        if(ASTUtil.isLockStatement(node, true)){
            numberOfLock ++;
        }
        return super.visit(node);
    }
}
