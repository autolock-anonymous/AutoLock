package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.slf4j.LoggerFactory;
import top.liebes.entity.LockStatementInfo;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;

import java.util.Iterator;
import java.util.Stack;

public class CombineLockVisitor extends ASTVisitor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(CombineLockVisitor.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    Block newlyAddedLockBlock;

    Stack<ExpressionStatement> visitedLockEStack = new Stack<>();

    public CombineLockVisitor(Block newlyAddedLockBlock){
        this.newlyAddedLockBlock = newlyAddedLockBlock;
    }

    @Override
    public boolean visit(ExpressionStatement node) {
        LockStatementInfo info = ASTUtil.getLockInfo(node);
        if(info != null){
            visitedLockEStack.push(node);
        }
        return super.visit(node);
    }

    public void optimize(){
        Stack<ExpressionStatement> localStack = new Stack<>();
        if(visitedLockEStack.size() == 0){
            return;
        }

        while(! visitedLockEStack.empty()){
            if(visitedLockEStack.size() < 2){
                logger.error("stack size should be more than 2");
                break;
            }

            ExpressionStatement first = visitedLockEStack.pop();
            ExpressionStatement second = visitedLockEStack.pop();
            if(ASTUtil.isLockPair(first, second)){
                if(! localStack.empty()){
                    for(ExpressionStatement parentLockStatement : localStack){
                        LockStatementInfo pinfo = ASTUtil.getLockInfo(parentLockStatement);
                        LockStatementInfo info = ASTUtil.getLockInfo(first);
                        if(pinfo != null && info != null){
                            // compare lock name
                            if(pinfo.getName().equals(info.getName())){
                                // compare lock type, write lock > read lock
                                if(pinfo.isWriteLock() || (pinfo.isReadLock() && info.isReadLock())){
                                    Block block = (Block) first.getParent();
                                    for(int i = block.statements().size() - 1; i >= 0; i --){
                                        if(first.equals(block.statements().get(i))){
                                            block.statements().remove(i);
                                        }
                                        else if(second.equals(block.statements().get(i))){
                                            block.statements().remove(i);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    visitedLockEStack.push(localStack.pop());
                }
            }
            else{
                localStack.push(first);
                visitedLockEStack.push(second);
            }
        }
    }
}
