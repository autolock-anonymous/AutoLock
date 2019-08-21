package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import sip4j.graphstructure.E_ClassGraphs;
import sip4j.graphstructure.E_MVertice;
import sip4j.graphstructure.E_MethodGraph;
import top.liebes.ast.VarFindVisitor;
import top.liebes.controller.JFileController;
import top.liebes.entity.JFile;
import top.liebes.entity.Pair;
import top.liebes.env.Env;

import java.util.*;

/**
 * @author liebes
 */
public class GraphUtil {
    private static Logger logger = (Logger) LoggerFactory.getLogger(GraphUtil.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    private TypeDeclaration newLockBlock;

    public static Pair<ASTNode, Pair<ASTNode, ASTNode>> getParentNode(Set<ASTNode> nodeSet){
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
                logger.debug("error happens when find ancestor of two nodes in one method");
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
        VarFindVisitor.clear();
    }

    public static class MapKeyComparator implements Comparator<String>{
        private Map<String, String> varLockMap;

        public MapKeyComparator(Map<String, String> varLockMap){
            this.varLockMap = varLockMap;
        }

        @Override
        public int compare(String s1, String s2){

            String[] tmp1 = s1.split("(\\.\\{|}\\.)");
            String[] tmp2 = s2.split("(\\.\\{|}\\.)");
            // should be like className.methodName.varName
            if(tmp1.length != 3 || tmp2.length != 3){
                logger.error("name error : " + s1);
                logger.error("name error : " + s2);
                return -1;
            }
            String className1 = tmp1[0];
            String methodName1 = "{" + tmp1[1] + "}";
            String varName1 = tmp1[2];

            String className2 = tmp2[0];
            String methodName2 = "{" + tmp2[1] + "}";
            String varName2 = tmp2[2];

            if(className1.equals(className2)){
                if(methodName1.equals(methodName2)){
                    String lockName1 = varLockMap.getOrDefault(className1 + "." + varName1, varName1 + "Lock");
                    String lockName2 = varLockMap.getOrDefault(className2 + "." + varName2, varName2 + "Lock");
                    return System.identityHashCode(lockName1) - System.identityHashCode(lockName2);
                }
                return methodName1.compareTo(methodName2);
            }
            return className1.compareTo(className2);
        }
    }

    /**
     * use union set to join same variable together that uses same lock
     * @param jFile
     * @return
     */
    public static Map<String, String> getLockForVar(JFile jFile, Map<String, Set<String>> includedVars){
        Map<String, String> reMap = new HashMap<>();
        for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
            for(E_MethodGraph methodGraph : classGraph.getMethodgraphs()){
                String classname = classGraph.getClassGraphName();
                String methodName = classname + "." + ASTUtil.getUniquelyIdentifiers(methodGraph);
                List<String> list = new ArrayList<>();
                for(E_MVertice vertex : methodGraph.getVertices()){
                    if("foo".equals(vertex.getVName())
                            || "context".equals(vertex.getVName())
                            || ! vertex.isField()
                    ){
                        continue;
                    }
                    if(includedVars.containsKey(methodName) && includedVars.get(methodName).contains(vertex.getVName())){
                        if(
                                "share".equals(vertex.getPre_permissions())
                                        || "unique".equals(vertex.getPre_permissions())
                                        || "full".equals(vertex.getPre_permissions())
                        ){
                            list.add(classname + "." + vertex.getVName());
                        }
                    }
                }
                if(list.size() > 0){
                    reMap.putIfAbsent(list.get(0), list.get(0));
                    for(int i = 1; i < list.size(); i ++){
                        reMap.put(list.get(i), getParent(reMap, list.get(0)));
                    }
                }
            }
        }
        Map<String, Set<String>> res = new HashMap<>();
        for(Map.Entry<String, String> entry : reMap.entrySet()){
            String v = entry.getKey();
            String pa = getParent(reMap, entry.getValue());
            if(! res.containsKey(pa)){
                Set<String> tmp = new TreeSet<>();
                tmp.add(pa);
                res.put(pa, tmp);
            }
            res.get(pa).add(v);
            res.put(v, res.get(pa));
        }
        Map<String, String> resMap = new TreeMap<>();
        for(Map.Entry<String, Set<String>> entry : res.entrySet()){
            List<String> list = new ArrayList<>();
            for(String s : entry.getValue()){
                list.add(s.substring(s.lastIndexOf(".") + 1));
            }
            resMap.put(entry.getKey(), String.join("_", list) + "Lock");
        }

        return resMap;
    }

    private static String getParent(Map<String, String> map, String s){
        if(s == null){
            logger.error("error, null pointer");
            return null;
        }
        if(s.equals(map.get(s))){
            return s;
        }
        String res = getParent(map, map.get(s));
        map.put(s, res);
        return res;
    }

    public static Map<String, Pair<String, String>> getPermissionForVar(JFile jFile){
        // get information from sip4j
        Map<String, Pair<String, String>> permissionMap = new HashMap<>();
        for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
            for(E_MethodGraph methodGraph : classGraph.getMethodgraphs()){
                for(E_MVertice vertex : methodGraph.getVertices()){
                    if("foo".equals(vertex.getVName()) || "context".equals(vertex.getVName()) || ! vertex.isField()){
                        continue;
                    }
                    String s = classGraph.getClassGraphName() + "."
                            + ASTUtil.getUniquelyIdentifiers(methodGraph) + "."
                            + vertex.getVName();
                    permissionMap.put(s,
                            Pair.make(vertex.getPre_permissions(), vertex.getPost_permissions()));
                }
            }
        }
        return permissionMap;
    }

    public static Map<String, Pair<String, String>> getPermissionForMethod(JFile jFile, Map<String, Set<String>> includedVars){
        Map<String, Pair<String, String>> permissionForMethodMap = new HashMap<>();
        for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
            for(E_MethodGraph methodGraph : classGraph.getMethodgraphs()){
                String classname = classGraph.getClassGraphName();
                String methodName = classname + "." + ASTUtil.getUniquelyIdentifiers(methodGraph);
                List<String> prePermissionList = new ArrayList<>();
                List<String> postPermissionList = new ArrayList<>();
                for(E_MVertice vertex : methodGraph.getVertices()){
                    if("foo".equals(vertex.getVName()) || "context".equals(vertex.getVName()) || !vertex.isField()){
                        continue;
                    }
                    if(includedVars.containsKey(methodName) && includedVars.get(methodName).contains(vertex.getVName())){
                        prePermissionList.add(vertex.getPre_permissions() + "(" + vertex.getVName() + ")");
                        postPermissionList.add(vertex.getPost_permissions() + "(" + vertex.getVName() + ")");
                    }
                }
                permissionForMethodMap.put(
                        methodName,
                        Pair.make(
                                String.join(" * ", prePermissionList),
                                String.join(" * ", postPermissionList)
                        )
                );
            }
        }
        return permissionForMethodMap;
    }
}

