package top.liebes.ast;

import org.eclipse.jdt.core.dom.*;
import top.liebes.util.ASTUtil;
import top.liebes.util.ExperimentUtil;

public class ExperimentVisitor extends ASTVisitor {
    public String className = null;



    @Override
    public boolean visit(TypeDeclaration node) {
        ExperimentUtil.increaseClass();
        this.className = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        ExperimentUtil.increaseMethod();
        return super.visit(node);
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        if(ASTUtil.isLockStatement(node, true)){
            ExperimentUtil.increaseTotalLockInsertion();
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if(node.getType() instanceof SimpleType){
            if(((SimpleType)node.getType()).getName().toString().equals("ReentrantReadWriteLock")){
                ExperimentUtil.increaseTotalLockDeclaration();
            }
        }
        ExperimentUtil.increaseField(className);
        return super.visit(node);
    }
}
