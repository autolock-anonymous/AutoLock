package top.liebes.ast;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.slf4j.LoggerFactory;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;

import java.util.Map;

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
        ImportDeclaration permissionImport = ASTUtil.getImportDeclaration(Env.PERM_IMPORT_DECL);
        ImportDeclaration lockImport = ASTUtil.getImportDeclaration("java.util.concurrent.locks.ReentrantReadWriteLock");
        node.imports().add(ASTNode.copySubtree(node.getAST(), permissionImport));
        node.imports().add(ASTNode.copySubtree(node.getAST(), lockImport));
        String pn = node.getPackage().getName().toString();
        pn += ".withlock";
        node.setPackage((PackageDeclaration) ASTNode.copySubtree(node.getAST(), ASTUtil.getPackageDeclaration(pn)));
        return super.visit(node);
    }
}
