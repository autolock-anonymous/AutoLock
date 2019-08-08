package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import sip4j.graphstructure.E_ClassGraphs;
import sip4j.graphstructure.E_MVertice;
import sip4j.graphstructure.E_MethodGraph;
import top.liebes.ast.AddLockVisitor;
import top.liebes.ast.AddPermissionVisitor;
import top.liebes.ast.VarFindVisitor;
import top.liebes.controller.JFileController;
import top.liebes.entity.JFile;
import top.liebes.entity.Pair;
import top.liebes.env.Env;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    public static Pair<ASTNode, Pair<ASTNode, ASTNode>> getParent(Set<ASTNode> nodeSet){
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
        @Override
        public int compare(String s1, String s2){
            return s1.compareTo(s2);
        }
    }

    public static Map<String, Pair<String, String>> getPermissionForVar(JFile jFile){
        // get information from sip4j
        Map<String, Pair<String, String>> permissionMap = new HashMap<>();
        for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
            for(E_MethodGraph methodGraph : classGraph.getMethodgraphs()){
                for(E_MVertice vertex : methodGraph.getVertices()){
                    if("foo".equals(vertex.getVName()) || "context".equals(vertex.getVName())){
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
                    if("foo".equals(vertex.getVName()) || "context".equals(vertex.getVName())){
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

