package top.liebes.ast;

import org.eclipse.jdt.core.dom.*;
import top.liebes.util.ASTUtil;

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
        methodName = ASTUtil.getUniquelyIdentifiers(node);
        return super.visit(node);
    }

    @Override
    public boolean visit(SimpleName node){
        // check classname and method name to make sure SimpleName is in method
        if(! "".equals(classname) && ! "".equals(methodName)){
            IBinding binding = node.resolveBinding();
            // check if the simple name is a class member, not parameter or reserved word
            if(binding instanceof IVariableBinding){
                if(((IVariableBinding) binding).isField()){
                    String varName = classname + "." + methodName + "." + node.getIdentifier();
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
