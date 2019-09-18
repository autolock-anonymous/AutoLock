package top.liebes.controller;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.ast.AddLockVisitor;
import top.liebes.ast.AddPermissionVisitor;
import top.liebes.ast.FieldFindVisitor;
import top.liebes.ast.RefactorLockVisitor;
import top.liebes.entity.JFile;
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
        long startTime = System.currentTimeMillis();
        String folder = Env.SOURCE_FOLDER;
        File root = new File(folder);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});

        // Read java files from folder
        for(File file : files){
            JFile jFile = JFileController.get(file.getName());

            // get compilation unit for each file
            final CompilationUnit cu = ASTUtil.getCompilationUnit(file, null);

            // @see LockVisitor.classMembers
            FieldFindVisitor fieldFindVisitor = new FieldFindVisitor();
            try{
                cu.accept(fieldFindVisitor);
            }
            catch (IllegalArgumentException e){
                e.printStackTrace();
            }

            // get information from sip4j
            // map : {class.method.var -> (pre permission, post permission)}
            Map<String, Pair<String, String>> permissionMap = GraphUtil.getPermissionForVar(jFile);

            // map : {class.method -> (pre permission, post permission)}
            Map<String, Pair<String, String>> permissionForMethodMap = GraphUtil.getPermissionForMethod(jFile, fieldFindVisitor.classMembers);

            // map : {var -> lockName} two members write in one function should have same lock
            Map<String, String> varLockMap = GraphUtil.getLockForVar(jFile, fieldFindVisitor.classMembers);


            // store each class member appearance in each method
            //  LockVisitor.fieldAccessMap
            AddLockVisitor lockVisitor = new AddLockVisitor(fieldFindVisitor.classMembers, permissionMap);
            try {
                cu.accept(lockVisitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

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
            // sort by key hashcode
            Map<String, Set<ASTNode>> sortMap = new TreeMap<>(new GraphUtil.MapKeyComparator(varLockMap));
            sortMap.putAll(lockVisitor.fieldAccessMap);

            // record if a variable has been locked by previous visited variable that has same lock.
            Set<String> unionLockMarkSet = new HashSet<>();

            // {lockName  -> set(ast node which should be locked by this lock)}
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

            ExperimentUtil.increaseInferLockTime(System.currentTimeMillis() - startTime);
            startTime = System.currentTimeMillis();

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

                // get if a node is static for static field should be protected by static lock.
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

                // get parent node. (parentNode, (first statement, last statement))
                Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair = GraphUtil.getParentNode(nodeSet);
                // add lock declaration
                if(! lockDeclarationMarkSet.contains(className + "." + lockName)){
                    boolean flag = ASTUtil.addLockDeclaration(parentPair.getV1(), lockName, isStatic);
                    if(flag){
                        lockDeclarationMarkSet.add(className + "." + lockName);
                    }
                }
                ASTUtil.addLock(permissionPair, parentPair, lockName);
            }

            // add all unlock statement to finally block if there is a return statement of throw statement between lock pairs.
            cu.accept(new RefactorLockVisitor());

            // remove useless lock scope
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
            ExperimentUtil.increaseApplyLockTime(System.currentTimeMillis() - startTime);
//            PdfUtil.generatePdfFile(Env.TARGET_FOLDER + "/pdf/" + folderName + "/" + FileUtil.removeSuffix(file.getName()) + ".pdf", cu.toString());
        }

    }

}
