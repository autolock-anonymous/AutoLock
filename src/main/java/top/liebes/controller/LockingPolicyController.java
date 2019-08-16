package top.liebes.controller;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.ast.AddLockVisitor;
import top.liebes.ast.AddPermissionVisitor;
import top.liebes.entity.JFile;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.*;

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

                Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair = GraphUtil.getParentNode(nodeSet);
                if(! lockDeclarationMarkSet.contains(lockName)){
                    boolean flag = ASTUtil.addLockDeclaration(parentPair.getV1(), lockName, true);
                    if(flag){
                        lockDeclarationMarkSet.add(lockName);
                    }
                }
                ExperimentUtil.increase(className, permissionPair.getV1());
                ASTUtil.addLock(permissionPair, parentPair, lockName);
            }

            // add all unlock statement to finally block
            cu.accept(new ASTVisitor() {
                @Override
                public void endVisit(Block node) {
                    Stack<Statement> tmpStack = new Stack<>();
                    Stack<Integer> idxStack = new Stack<>();
                    for(int i = 0; i < node.statements().size(); i ++){
                        Statement st = (Statement)node.statements().get(i);
                        if(ASTUtil.isLockStatement(st, true)){
                            tmpStack.push(st);
                            idxStack.push(i);
                        }
                        else if(ASTUtil.isLockStatement(st, false)){
                            for(int j = i + 1; j < node.statements().size(); j ++){
                                if(ASTUtil.isLockStatement((Statement)node.statements().get(j), false)){
                                    st = (Statement) node.statements().get(j);
                                    idxStack.pop();
                                    i ++;
                                }
                                else{
                                    break;
                                }
                            }
                            Statement top = tmpStack.pop();
                            boolean flag = false;
                            while(!tmpStack.isEmpty() && !ASTUtil.isLockPair(top, st)){
                                if(top.getNodeType() == ASTNode.RETURN_STATEMENT){
                                    flag = true;
                                }
                                top = tmpStack.pop();
                            }
                            int lockIdx = idxStack.pop();
                            if(flag){
                                TryStatement tryStatement = node.getAST().newTryStatement();
                                Block block = tryStatement.getAST().newBlock();
                                Block finallyBlock = tryStatement.getAST().newBlock();
                                for(int j = lockIdx; j <= i ; ){
                                    if(ASTUtil.isLockStatement((Statement) node.statements().get(j), false)){
                                        finallyBlock.statements().add(ASTNode.copySubtree(finallyBlock.getAST(), (ASTNode) node.statements().get(j)));
                                    }
                                    else{
                                        block.statements().add(ASTNode.copySubtree(block.getAST(), (ASTNode) node.statements().get(j)));
                                    }
                                    node.statements().remove(j);
                                    i --;
                                }
                                tryStatement.setBody(block);
                                tryStatement.setFinally((Block)ASTNode.copySubtree(tryStatement.getAST(), finallyBlock));
                                node.statements().add(i + 1, ASTNode.copySubtree(node.getAST(), tryStatement));
                            }
                        }
                        else{
                            if(tmpStack.size() > 0){
                                tmpStack.push(st);
                            }
                        }
                    }
                    super.endVisit(node);
                }
            });

            // write result to file
            String folderName = permissionVisitor.getPackageName().replace(".", "/");
            String targetFilePath = Env.TARGET_FOLDER + "/" + folderName + "/" + file.getName();
            FileUtil.writeToFile(targetFilePath, ASTUtil.format(cu.toString()));
//            PdfUtil.generatePdfFile(Env.TARGET_FOLDER + "/pdf/" + folderName + "/" + FileUtil.removeSuffix(file.getName()) + ".pdf", cu.toString());
        }

    }

}
