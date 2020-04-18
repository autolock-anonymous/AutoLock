package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.env.Env;

import java.util.HashSet;
import java.util.Set;

public class AddGetterVisitor extends ASTVisitor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(AddGetterVisitor.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    private Set<FieldDeclaration> fieldSet = new HashSet<>();
    private Set<MethodDeclaration> methodSet = new HashSet<>();

    public Set<FieldDeclaration> getFieldSet() {
        return fieldSet;
    }

    public Set<MethodDeclaration> getMethodSet() {
        return methodSet;
    }

    @Override
    public boolean visit(FieldDeclaration node) {
        if(!node.getType().isArrayType() && !node.getType().isPrimitiveType()){
            this.fieldSet.add(node);
        }
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if(node.getName().toString().startsWith("get")){
            this.methodSet.add(node);
        }
        return super.visit(node);
    }
}
