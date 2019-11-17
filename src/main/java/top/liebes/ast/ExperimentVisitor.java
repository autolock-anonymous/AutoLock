package top.liebes.ast;

import org.eclipse.jdt.core.dom.*;
import top.liebes.util.ASTUtil;
import top.liebes.util.ExperimentUtil;

public class ExperimentVisitor extends ASTVisitor {
    public String className = null;
    public int count;
    public boolean flag = true;

    @Override
    public boolean visit(TypeDeclaration node) {
        ExperimentUtil.increaseClass();
        this.className = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        ExperimentUtil.increaseMethod();
        count = 0;
        flag = false;
//        node.accept(new ASTVisitor() {
//            @Override
//            public boolean visit(ExpressionStatement tnode) {
//                if(flag){
//                    return super.visit(tnode);
//                }
//                if(! ASTUtil.isLockStatement(tnode, true)){
//                    count ++;
//                }
//                else{
//                    if(count >= 4){
//                        System.out.println(className + "." + ASTUtil.getUniquelyIdentifiers(node) + " : " + count);
//                    }
//                    flag = true;
//                }
//                return super.visit(tnode);
//            }
//        });

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
                ExperimentUtil.increaseTotalLockDeclaration(className);
            }
        }
        ExperimentUtil.increaseField(className);
        return super.visit(node);
    }
}
