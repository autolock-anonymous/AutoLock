package liebes.top.util;

import liebes.top.ast.AddLockVisitor;
import liebes.top.ast.AddPermissionVisitor;
import liebes.top.ast.VarFindVisitor;
import liebes.top.controller.JFileController;
import liebes.top.entity.JFile;
import liebes.top.entity.Pair;
import liebes.top.env.Env;
import sip4j.graphstructure.E_ClassGraphs;
import sip4j.graphstructure.E_MVertice;
import sip4j.graphstructure.E_MethodGraph;
import org.eclipse.jdt.core.dom.*;

import java.io.*;
import java.util.*;

/**
 * @author liebes
 */
public class GraphUtil {
    private TypeDeclaration newLockBlock;

    public static void func(){

    }

    public static void test(){
        String folder = Env.SOURCE_FOLDER;
        File root = new File(folder);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});

        // Read java files from folder
        for(File file : files){
            JFile jFile = JFileController.get(file.getName());
            // get information from sip4j
            Map<String, Pair<String, String>> permissionMap = new HashMap<>();
            Map<String, Pair<String, String>> permissionForMethodMap = new HashMap<>();
            for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
                for(E_MethodGraph methodGraph : classGraph.getMethodgraphs()){
                    List<String> prePermissionList = new ArrayList<>();
                    List<String> postPermissionList = new ArrayList<>();
                    for(E_MVertice vertex : methodGraph.getVertices()){
                        if("foo".equals(vertex.getVName()) || "context".equals(vertex.getVName())){
                            continue;
                        }
                        String s = classGraph.getClassGraphName() + "."
                                + methodGraph.getMgraphName() + "."
                                + vertex.getVName();
                        permissionMap.put(s,
                                Pair.make(vertex.getPre_permissions(), vertex.getPost_permissions()));

                        prePermissionList.add(vertex.getPre_permissions() + "(" + vertex.getVName() + ")");
                        postPermissionList.add(vertex.getPost_permissions() + "(" + vertex.getVName() + ")");
                    }
                    permissionForMethodMap.put(
                            classGraph.getClassGraphName() + "." + methodGraph.getMgraphName(),
                            Pair.make(
                                    String.join(" * ", prePermissionList),
                                    String.join(" * ", postPermissionList)
                            )
                    );
                }
            }
            // get permission information done

            // get compilation unit for each file
            final CompilationUnit cu = ASTUtil.getCompilationUnit(file);

            // get all class member
            VarFindVisitor visitor = new VarFindVisitor();
            try {
                cu.accept(visitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

            // store each class member appearance in each method
            AddLockVisitor lockVisitor = new AddLockVisitor();
            try {
                cu.accept(lockVisitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

            // add permission annotation
            AddPermissionVisitor permissionVisitor = new AddPermissionVisitor(permissionForMethodMap);
            try {
                cu.accept(permissionVisitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }

            // add lock for each class member variable according to the map obtained above
            Set<String> lockDeclarationMarkSet = new HashSet<>();
            for(String s : AddLockVisitor.fieldAccessMap.keySet()){
                // used to add lock in the smallest block
                ASTNode firstStatement = null;
                ASTNode lastStatement = null;
                // <parent, <firstStatement, lastStatement>
                Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair = getParent(AddLockVisitor.fieldAccessMap.get(s));

                String[] tmp = s.split("\\.");
                // should be like className.methodName.varName
                if(tmp.length != 3){
                    System.err.println("name error");
                    continue;
                }
                String className = tmp[0];
                String methodName = tmp[1];
                String varName = tmp[2];
                String lockName = varName + "Lock";
                Pair<String, String> permissionPair = permissionMap.get(s);
                if(permissionPair == null){
                    System.out.println(s + " has no permission");
                    continue;
                }

                if(! lockDeclarationMarkSet.contains(className + "." + varName)){
                    boolean flag = ASTUtil.addLockDeclaration(parentPair.getV1(), lockName);
                    if(flag){
                        lockDeclarationMarkSet.add(className + "." + varName);
                    }
                }

                if(permissionPair.getV1() == null){
                    permissionPair.setV1("immutable");
                }
                if("immutable".equals(permissionPair.getV1() )){
                    // do nothing
                    continue;
                }
                else if ("pure".equals(permissionPair.getV1() )){
                    // add read lock
                    ASTUtil.surroundedByLock(parentPair, ASTUtil.READ_LOCK, varName, firstStatement, lastStatement);
                }
                else if ("share".equals(permissionPair.getV1() ) || "full".equals(permissionPair.getV1() )){
                    ASTUtil.surroundedByLock(parentPair, ASTUtil.WRITE_LOCK, varName, firstStatement, lastStatement);
                    // add write lock
                }
                else if ("unique".equals(permissionPair.getV1() )){
                    ASTUtil.surroundedByLock(parentPair, ASTUtil.READ_WRITE_LOCK, varName, firstStatement, lastStatement);
                    // add sync block
                }
            }
            String folderName = VarFindVisitor.packageName.replace(".", "/");
            String targetFolderPath = Env.TARGET_FOLDER + "/" + folderName;
            File targetFolder = new File(targetFolderPath);
            if(! targetFolder.exists()){
                targetFolder.mkdirs();
            }

            String filename = folderName + "/" + file.getName();
            String targetFilePath = Env.TARGET_FOLDER + "/" + filename;
            File targetFile = new File(targetFilePath);
            if(targetFile.exists()){
                targetFile.delete();
            }
            try{
                FileWriter fw = new FileWriter(targetFile);
                fw.write(ASTUtil.format(cu.toString()));
                fw.close();
            }
            catch(IOException e){
                System.err.println("write result to file error");
                e.printStackTrace();
            }
        }
    }

    private static Pair<ASTNode, Pair<ASTNode, ASTNode>> getParent(Set<ASTNode> nodeSet){
        ASTNode parent = null;
        List<ASTNode> parentList = new ArrayList<>();
        boolean isFirstIter = true;
        int start = 0;
        Set<ASTNode> statementSet = new HashSet<>();
        for(ASTNode node : nodeSet){
            if(isFirstIter){
                while(node.getParent() != null){
                    node = node.getParent();
                    parentList.add(node);
                    if(node instanceof Statement && ! (node instanceof Block)){
                        statementSet.add(node);
                    }
                }
                isFirstIter = false;
            }
            else{
                boolean flag = false;
                while(node.getParent() != null){
                    if(node.getParent() instanceof MethodDeclaration){
                        System.out.println("Woo");
                    }
                    node = node.getParent();
                    if(node instanceof Statement && !(node instanceof Block)){
                        statementSet.add(node);
                    }
                    for(int i = start; i < parentList.size(); i ++){
                        // compare address
                        if(node == parentList.get(i)){
                            parent = parentList.get(i);
                            start = i;
                            flag = true;
                            break;
                        }
                    }
                    if(flag){
                        break;
                    }
                }
            }
        }
        // only have one node, or error happens
        if(parent == null){
            if(nodeSet.size() == 1){
                for(ASTNode tmp : nodeSet){
                    parent = tmp.getParent();
                }
            }
            // error happens, two nodes in one method have no same parent
            else{
                System.err.println("error happens when find ancestor of two nodes in one method");
            }
        }
        ASTNode firstStatement = null;
        ASTNode lastStatement = null;
        if(parent instanceof Block){
            Block block = (Block) parent;
            for(int i = 0; i < block.statements().size(); i ++){
                Statement statement = (Statement) block.statements().get(i);
                if(statementSet.contains(statement)){
                    firstStatement = statement;
                    break;
                }
            }
            for(int i = block.statements().size() - 1; i >= 0; i --){
                Statement statement = (Statement) block.statements().get(i);
                if(statementSet.contains(statement)){
                    lastStatement = statement;
                    break;
                }
            }
        }

        Pair<ASTNode, Pair<ASTNode, ASTNode>> res = Pair.make(parent, Pair.make(firstStatement, lastStatement));
        return res;
    }

    public static void clear(){
        JFileController.clear();
        ASTUtil.clearMap();
        AddLockVisitor.clear();
        VarFindVisitor.clear();
    }
}
