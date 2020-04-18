package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author liebes
 */
public class FieldFindVisitor extends ASTVisitor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(FieldFindVisitor.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    private static String classname = "";
    private static String methodName = "";
    /**
     * map : {classname -> set(varName)}
     */
    public Map<String, Set<String>> classMembers = new HashMap<>();

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
                    // mapping field to map
                    classMembers.putIfAbsent(classname + "." + methodName, new HashSet<>());
                    classMembers.computeIfPresent(classname + "." + methodName, (k, v) -> {
                        v.add(node.getIdentifier());
                        return v;
                    });
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

    @Override
    public boolean visit(BlockComment node) {
        super.visit(node);
        node.delete();
        return true;
    }

    @Override
    public boolean visit(LineComment node) {
        super.visit(node);
        node.delete();
        return true;
    }


    @Override
    public boolean visit(Javadoc node) {
        super.visit(node);
        node.delete();
        return true;
    }
}
