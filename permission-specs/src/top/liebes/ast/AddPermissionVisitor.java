package top.liebes.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import top.liebes.entity.Pair;
import top.liebes.util.ASTUtil;

import java.util.Map;

/**
 * @author liebes
 */
public class AddPermissionVisitor extends ASTVisitor {
    private Map<String, Pair<String, String>> permissionMap;
    private String classname = "";

    private String methodName = "";

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
    public boolean visit(MethodDeclaration node){
        if(permissionMap.containsKey(classname + "." + node.getName().toString())){
            Pair<String, String> pair = permissionMap.get(classname + "." + node.getName().toString());
            ASTUtil.addPermissionAnnotation(node, pair.getV1(), pair.getV2());
        }
        return super.visit(node);
    }

}
