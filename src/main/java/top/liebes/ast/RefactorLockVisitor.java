package top.liebes.ast;

import org.eclipse.jdt.core.dom.*;
import top.liebes.util.ASTUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RefactorLockVisitor extends ASTVisitor {
    private Stack<Stack<Statement>> methodStack = new Stack<>();

    @Override
    public boolean visit(MethodDeclaration node) {
        methodStack.push(new Stack<>());
        return super.visit(node);
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        Stack<Statement> localStack = new Stack<>();
        Stack<Statement> statementStack = methodStack.pop();
        if(statementStack == null){
            return;
        }
        List<Statement> unlockList = new ArrayList<>();
        while(!statementStack.empty()){
            Statement lockStatement = statementStack.pop();
            while(!statementStack.empty() && !ASTUtil.isLockStatement(lockStatement, true)){
                localStack.push(lockStatement);
                lockStatement = statementStack.pop();
            }
            if(!ASTUtil.isLockStatement(lockStatement, true)){
                break;
            }
            boolean needTry = false;
            Statement unLockStatement = localStack.pop();
            while(!localStack.empty() && !ASTUtil.isLockPair(lockStatement, unLockStatement)){
                if(unLockStatement.getNodeType() == ASTNode.RETURN_STATEMENT || unLockStatement.getNodeType() == ASTNode.THROW_STATEMENT){
                    needTry = true;
                }
                statementStack.push(unLockStatement);
                unLockStatement = localStack.pop();
            }
            while(!localStack.empty() && ASTUtil.isLockStatement(localStack.peek(), false)){
                unlockList.add(unLockStatement);
                unLockStatement = localStack.pop();
            }
            if(!ASTUtil.isLockPair(lockStatement, unLockStatement)){
                localStack.push(unLockStatement);
                continue;
            }
            if(needTry){
                // add try block;
                Block block = (Block) lockStatement.getParent();

                TryStatement tryStatement = node.getAST().newTryStatement();
                Block tryBlock = tryStatement.getAST().newBlock();
                Block finallyBlock = tryStatement.getAST().newBlock();

                tryStatement.setBody(tryBlock);
                tryStatement.setFinally(finallyBlock);
                for(int k = 0; k < unlockList.size(); k ++){
                    unlockList.get(k).delete();
                }
                for(int i = 0; i < block.statements().size(); i ++){
                    if(block.statements().get(i) == lockStatement){
                        for(int j = i; j < block.statements().size(); j ++){
                            if(block.statements().get(j) == unLockStatement){
                                for(int k = 0; k < unlockList.size(); k ++){
                                    finallyBlock.statements().add(ASTNode.copySubtree(finallyBlock.getAST(), unlockList.get(k)));
                                }
                                finallyBlock.statements().add(ASTNode.copySubtree(finallyBlock.getAST(), unLockStatement));
                                block.statements().remove(j);
                                block.statements().add(j, ASTNode.copySubtree(block.getAST(), tryStatement));
                                unlockList.clear();
                                j --;
                                break;
                            }
                            else{
                                tryBlock.statements().add(ASTNode.copySubtree(tryBlock.getAST(), (Statement) block.statements().get(j)));
                                block.statements().remove(j);
                                j --;
                            }
                        }
                        break;
                    }
                }
            }
        }
        super.endVisit(node);
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ReturnStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ThrowStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(AssertStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(BreakStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ContinueStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(DoStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(EmptyStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(EnhancedForStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(ForStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(LabeledStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(IfStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(SwitchStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(SynchronizedStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(TryStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclarationStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(WhileStatement node) {
        if(!methodStack.empty()){
            methodStack.peek().push(node);
        }
        return super.visit(node);
    }
}
