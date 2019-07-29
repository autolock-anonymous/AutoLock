package liebes.top.ast;

import org.eclipse.jdt.core.dom.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * find all class member
 *
 * @author liebes
 */
public class VarFindVisitor extends ASTVisitor {

    public static Set<String> vars = new HashSet<>();

    private static String classname = "";

    public static String packageName = "";

    @Override
    public boolean visit(PackageDeclaration node){
        packageName = node.getName().toString();
        return super.visit(node);
    }

    @Override
    public boolean visit(FieldDeclaration node){
        if(! "".equals(classname)){
            List<VariableDeclarationFragment> variables = node.fragments();
            for(VariableDeclarationFragment variable : variables){
                vars.add(classname + "." + variable.getName().toString());
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
        vars.clear();
    }

}
