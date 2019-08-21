package top.liebes.ast;

import org.eclipse.jdt.core.dom.*;
import top.liebes.util.ASTUtil;
import top.liebes.util.ExperimentUtil;

public class ExperimentVisitor extends ASTVisitor {


    @Override
    public boolean visit(TypeDeclaration node) {
        ExperimentUtil.increaseClass();
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
        return super.visit(node);
    }
}
