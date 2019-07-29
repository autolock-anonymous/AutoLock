package top.liebes.ast;

import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * @author liebes
 */
public class AddLockVisitor extends ASTVisitor {
    private static String classname = "";
    private static String methodName = "";
    public static Map<String, Set<ASTNode>> fieldAccessMap = new HashMap<>();

    @Override
    public boolean visit(MethodDeclaration node){
        methodName = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(SimpleName node){
        if(! "".equals(classname) && ! "".equals(methodName)){
            String varName = classname + "." + node.getIdentifier();
            if(VarFindVisitor.vars.contains(varName)){
                varName = classname + "." + methodName + "." + node.getIdentifier();
                if(fieldAccessMap.containsKey(varName)){
                    fieldAccessMap.get(varName).add(node);
                }
                else{
                    Set<ASTNode> set = new HashSet<>();
                    set.add(node);
                    fieldAccessMap.put(varName, set);
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(TypeDeclaration node){
        if(! node.isInterface()){
            classname = node.getName().toString();
        }
        return super.visit(node);
    }

    public static void clear(){
        fieldAccessMap.clear();
    }
}
