package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;

import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static sip4j.parser.AST_Parser.createMethodSignature;

/**
 * @author liebes
 */
public class AddPermissionVisitor extends ASTVisitor {
    private static Logger logger = (Logger) LoggerFactory.getLogger(AddPermissionVisitor.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    private Map<String, Pair<String, String>> permissionMap;
    private String classname = "";

    private String methodName = "";

    private String packageName = "";

    private int cnt = 0;

    public AddPermissionVisitor(Map<String, Pair<String, String>> permissionMap){
        super();
        this.permissionMap = permissionMap;
    }

    @Override
    public boolean visit(TypeDeclaration node){
        classname = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(PackageDeclaration node){
        this.packageName = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(MethodDeclaration node){
        String s = classname + "." + ASTUtil.getUniquelyIdentifiers(node);
        if(permissionMap.containsKey(s)){
            Pair<String, String> pair = permissionMap.get(s);
            ASTUtil.addPermissionAnnotation(node, pair.getV1(), pair.getV2());
        }
        return super.visit(node);
    }

    public String getPackageName() {
        return packageName;
    }

    // add import
    @Override
    public boolean visit(CompilationUnit node) {
        ImportDeclaration permissionImport = ASTUtil.getImporDeclaration(Env.PERM_IMPORT_DECL);
        ImportDeclaration lockImport = ASTUtil.getImporDeclaration("java.util.concurrent.locks.ReentrantReadWriteLock");
        node.imports().add(ASTNode.copySubtree(node.getAST(), permissionImport));
        node.imports().add(ASTNode.copySubtree(node.getAST(), lockImport));
        return super.visit(node);
    }
}
