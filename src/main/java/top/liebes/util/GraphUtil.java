package top.liebes.util;

import top.liebes.ast.AddLockVisitor;
import top.liebes.ast.AddPermissionVisitor;
import top.liebes.ast.VarFindVisitor;
import top.liebes.controller.JFileController;
import top.liebes.entity.JFile;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
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
                                + ASTUtil.getUniquelyIdentifiers(methodGraph) + "."
                                + vertex.getVName();
                        permissionMap.put(s,
                                Pair.make(vertex.getPre_permissions(), vertex.getPost_permissions()));

                        prePermissionList.add(vertex.getPre_permissions() + "(" + vertex.getVName() + ")");
                        postPermissionList.add(vertex.getPost_permissions() + "(" + vertex.getVName() + ")");
                    }
                    permissionForMethodMap.put(
                            classGraph.getClassGraphName() + "." + ASTUtil.getUniquelyIdentifiers(methodGraph),
                            Pair.make(
                                    String.join(" * ", prePermissionList),
                                    String.join(" * ", postPermissionList)
                            )
                    );
                }
            }
            System.out.println("--------------------");
            for(Map.Entry<String, Pair<String, String> > entry : permissionForMethodMap.entrySet()){
                System.out.println(entry.getKey() + " " + entry.getValue().getV1() + " " + entry.getValue().getV2());
            }
            System.out.println("--------------------");
            // get permission information done

            // get compilation unit for each file
            final CompilationUnit cu = ASTUtil.getCompilationUnit(file, null);

            // get all class member
//            VarFindVisitor visitor = new VarFindVisitor();
//            try {
//                cu.accept(visitor);
//            } catch (IllegalArgumentException ex) {
//                ex.printStackTrace();
//            }

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
            Map<String, Set<ASTNode>> sortMap = new TreeMap<>(new MapKeyComparator());
            sortMap.putAll(AddLockVisitor.fieldAccessMap);

            for(Map.Entry<String, Set<ASTNode>> entry : sortMap.entrySet()){
                String s = entry.getKey();

                // used to add lock in the smallest block
                ASTNode firstStatement = null;
                ASTNode lastStatement = null;
                // <parent, <firstStatement, lastStatement>
                Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair = getParent(entry.getValue());
                boolean isStatic = false;
                boolean isFinal = false;
                for(ASTNode astNode : entry.getValue()){
                    if(astNode instanceof SimpleName){
                        SimpleName simpleName = (SimpleName) astNode;
                        IBinding binding = simpleName.resolveBinding();
                        if(binding instanceof IVariableBinding){
                            isStatic = Modifier.isStatic(binding.getModifiers());
                            isFinal = Modifier.isFinal(binding.getModifiers());
                            break;
                        }
                    }
                }
                // final field does not need lock
                if(isFinal){
                    continue;
                }

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
                    boolean flag = ASTUtil.addLockDeclaration(parentPair.getV1(), lockName, isStatic);
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
                    ASTUtil.surroundedByLock(parentPair, ASTUtil.READ_LOCK, varName);
                }
                else if ("share".equals(permissionPair.getV1() ) || "full".equals(permissionPair.getV1() )){
                    ASTUtil.surroundedByLock(parentPair, ASTUtil.WRITE_LOCK, varName);
                    // add write lock
                }
                else if ("unique".equals(permissionPair.getV1() )){
                    ASTUtil.surroundedByLock(parentPair, ASTUtil.READ_WRITE_LOCK, varName);
                    // add sync block
                }
            }
            String folderName = permissionVisitor.getPackageName().replace(".", "/");
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

            PdfUtil.generatePdfFile(Env.TARGET_FOLDER + "/pdf/" + folderName + "/" + FileUtil.removeSuffix(file.getName()) + ".pdf", cu.toString());
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

    static class MapKeyComparator implements Comparator<String>{
        @Override
        public int compare(String s1, String s2){
            return s1.compareTo(s2);
        }
    }
}

