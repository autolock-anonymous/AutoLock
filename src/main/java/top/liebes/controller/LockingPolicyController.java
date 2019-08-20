package top.liebes.controller;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.ast.AddLockVisitor;
import top.liebes.ast.AddPermissionVisitor;
import top.liebes.entity.JFile;
import top.liebes.entity.LockStatementInfo;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;
import top.liebes.util.ExperimentUtil;
import top.liebes.util.FileUtil;
import top.liebes.util.GraphUtil;

import java.io.File;
import java.util.*;

public class LockingPolicyController {

    private static Logger logger = (Logger) LoggerFactory.getLogger(LockingPolicyController.class);

    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    private static LockingPolicyController lockingPolicyController = new LockingPolicyController();

    private LockingPolicyController(){

    }

    public static LockingPolicyController getInstance(){
        return lockingPolicyController;
    }

    public void generate(){
        String folder = Env.SOURCE_FOLDER;
        File root = new File(folder);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});

        // Read java files from folder
        for(File file : files){
            JFile jFile = JFileController.get(file.getName());

            // get compilation unit for each file
            final CompilationUnit cu = ASTUtil.getCompilationUnit(file, null);

            // store each class member appearance in each method
            AddLockVisitor lockVisitor = new AddLockVisitor();
            try {
                cu.accept(lockVisitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

            // get information from sip4j
            Map<String, Pair<String, String>> permissionMap = GraphUtil.getPermissionForVar(jFile);
            Map<String, Pair<String, String>> permissionForMethodMap = GraphUtil.getPermissionForMethod(jFile, lockVisitor.classMembers);
            Map<String, String> varLockMap = GraphUtil.getLockForVar(jFile, lockVisitor.classMembers);

//            for(Map.Entry<String, Pair<String, String> > entry : permissionForMethodMap.entrySet()){
//                logger.debug(entry.getKey() + " " + entry.getValue().getV1() + " " + entry.getValue().getV2());
//            }
            // get permission information done

            // add permission annotation
            AddPermissionVisitor permissionVisitor = new AddPermissionVisitor(permissionForMethodMap);
            try {
                cu.accept(permissionVisitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

            // add lock for each class member variable according to the map obtained above
            Set<String> lockDeclarationMarkSet = new HashSet<>();
            Map<String, Set<ASTNode>> sortMap = new TreeMap<>(new GraphUtil.MapKeyComparator());
            sortMap.putAll(lockVisitor.fieldAccessMap);

            Set<String> unionLockMarkSet = new HashSet<>();

            Map<String, Set<ASTNode>> nodeLockMap = new HashMap<>();
            for(Map.Entry<String, Set<ASTNode>> entry : sortMap.entrySet()){
                String s = entry.getKey();

                String[] tmp = s.split("(\\.\\{|}\\.)");
                // should be like className.methodName.varName
                if(tmp.length != 3){
                    logger.debug("name error : " + s);
                    continue;
                }
                String className = tmp[0];
                String methodName = "{" + tmp[1] + "}";
                String varName = tmp[2];
                if(varLockMap.containsKey(className + "." + varName)){
                    String key = className + "." + methodName + "." + varLockMap.get(className + "." + varName);
                    nodeLockMap.putIfAbsent(key , new HashSet<>());
                    nodeLockMap.get(key).addAll(entry.getValue());
                }
            }


            for(Map.Entry<String, Set<ASTNode>> entry : sortMap.entrySet()){
                String s = entry.getKey();

                String[] tmp = s.split("(\\.\\{|}\\.)");
                // should be like className.methodName.varName
                if(tmp.length != 3){
                    logger.debug("name error : " + s);
                    continue;
                }
                String className = tmp[0];
                String methodName = "{" + tmp[1] + "}";
                String varName = tmp[2];
                String lockName = varLockMap.getOrDefault(className + "." + varName, varName + "Lock");
                Set<ASTNode> nodeSet = entry.getValue();
                Pair<String, String> permissionPair = permissionMap.get(s);
                if(permissionPair == null){
                    logger.debug(s + " has no permission");
                    continue;
                }

                if ("share".equals(permissionPair.getV1())
                        || "full".equals(permissionPair.getV1())
                        || "unique".equals(permissionPair.getV1())
                ){
                    String key = className + "." + methodName + "." + lockName;
                    if(unionLockMarkSet.contains(key)){
                        // this variable has already been locked
                        continue;
                    }
                    unionLockMarkSet.add(key);

                    nodeSet = nodeLockMap.get(key);
                    if(nodeSet == null){
                        logger.error("errors");
                    }
                }

                boolean isStatic = false;
                for(ASTNode astNode : nodeSet){
                    if(astNode instanceof SimpleName){
                        if(((SimpleName)astNode).resolveBinding() instanceof IVariableBinding){
                            if(Modifier.isStatic(((SimpleName)astNode).resolveBinding().getModifiers())){{
                                isStatic = true;
                                break;
                            }}
                        }
                    }
                }

                Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair = GraphUtil.getParentNode(nodeSet);
                if(! lockDeclarationMarkSet.contains(className + "." + lockName)){
                    boolean flag = ASTUtil.addLockDeclaration(parentPair.getV1(), lockName, isStatic);
                    if(flag){
                        lockDeclarationMarkSet.add(className + "." + lockName);
                    }
                }
                ExperimentUtil.increase(className, permissionPair.getV1());
                ASTUtil.addLock(permissionPair, parentPair, lockName);
            }
            // add all unlock statement to finally block
            cu.accept(new ASTVisitor() {
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

            });

            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(Block node) {
                    for(int i = 0; i < node.statements().size() - 1; i ++){
                        if(ASTUtil.isLockPair((Statement)node.statements().get(i), (Statement) node.statements().get(i + 1))){
                            node.statements().remove(i + 1);
                            node.statements().remove(i);
                            i --;
                        }
                    }
                    return super.visit(node);
                }
            });

            // write result to file
            String filename = file.getAbsolutePath();
            filename = filename.replace("/entity/", "/entity/withlock/");
//            String folderName = permissionVisitor.getPackageName().replace(".", "/");
//            String targetFilePath = Env.TARGET_FOLDER + "/" + folderName + "/" + file.getName();
            FileUtil.writeToFile(filename, ASTUtil.format(cu.toString()));
//            PdfUtil.generatePdfFile(Env.TARGET_FOLDER + "/pdf/" + folderName + "/" + FileUtil.removeSuffix(file.getName()) + ".pdf", cu.toString());
        }

    }

}
