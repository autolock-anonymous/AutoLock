package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;

import java.util.*;

/**
 * @author liebes
 */
public class AddLockVisitor extends ASTVisitor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(AddLockVisitor.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    private static String classname = "";
    private static String methodName = "";

    private Map<String, Set<String>> classMembers;
    private Map<String, Pair<String, String>> permissionMap;

    public AddLockVisitor(Map<String, Set<String>> classMembers, Map<String, Pair<String, String>> permissionMap){
        this.classMembers = classMembers;
        this.permissionMap = permissionMap;
    }

    /**
     * map : {classname.methodName.varName -> set(node of variable)}
     */
    public Map<String, Set<ASTNode>> fieldAccessMap = new HashMap<>();

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
                IVariableBinding variableBinding = (IVariableBinding) binding;
                if(variableBinding.isField() && ! Modifier.isFinal(variableBinding.getModifiers())){
                    String varName = classname + "." + methodName + "." + node.getIdentifier();
                    fieldAccessMap.putIfAbsent(varName, new HashSet<>());
                    fieldAccessMap.computeIfPresent(varName, (k, v) -> {
                        v.add(node);
                        return v;
                    });
                }
            }
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodInvocation node) {
        if(node.getName().toString().equals("ensureOpen")){
            System.out.println("???");
        }
        if(node.resolveMethodBinding() != null){
            IMethodBinding bind = node.resolveMethodBinding();
            if(! bind.isConstructor()){
                String invokeMethodName = ASTUtil.getUniquelyIdentifiers(node.resolveMethodBinding());
                if(classMembers.containsKey(classname + "." + invokeMethodName)){
                    Set<String> varNames = classMembers.get(classname + "." + invokeMethodName);
                    for(String varName : varNames){
                        String s = classname + "." + invokeMethodName + "." + varName;
                        if(permissionMap.containsKey(s)){
                            // note that here is methodName not invokedMethodName
                            s = classname + "." + methodName + "." + varName;
                            fieldAccessMap.putIfAbsent(s, new HashSet<>());
                            fieldAccessMap.computeIfPresent(s, (k, v) -> {
                                v.add(node);
                                return v;
                            });
                        }
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
}
