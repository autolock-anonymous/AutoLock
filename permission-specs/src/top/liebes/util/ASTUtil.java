package top.liebes.util;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import top.liebes.entity.Pair;
import top.liebes.env.Env;

import java.io.File;
import java.util.*;

/**
 * @author liebes
 */
public class ASTUtil {

    public static Map<String, CompilationUnit> cpMap = new HashMap<>();

    public final static int READ_LOCK = 1;
    public final static int WRITE_LOCK = 2;
    public final static int READ_WRITE_LOCK = 3;

    /**
     * Parse a file to compilation unit.
     *
     * @param file
     * @return
     */
    public static CompilationUnit getCompilationUnit(File file){
        if(cpMap.containsKey(file.getAbsolutePath())){
            return cpMap.get(file.getAbsolutePath());
        }

        ASTParser parser = ASTParser.newParser(AST.JLS3);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

        parser.setUnitName(FileUtil.removeSuffix(file.getName()));

        String[] sources = { Env.SOURCE_FOLDER };
        String[] classpath = Env.CLASSPATH;

        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        parser.setSource(FileUtil.getFileContents(file));

        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        return cu;
    }

    public static void clearMap(){
        cpMap.clear();
    }

    /**
     * get AST node, expression statement, of read write lock
     * example :
     *      lockName.readLock().lock();
     *      lockName.writeLock().unlock;
     * @param lockName name of lock variable
     * @param isRead true for readLock, false for writeLock
     * @param isLock true for lock operation, false for unLock operation
     * @return write lock expression statement, AST node
     */
    public static ExpressionStatement getReadWriteLockExpression(String lockName, boolean isRead, boolean isLock){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        String lockType = isRead ? "readLock()" : "writeLock()";
        String lockProperty = isLock ? "lock()" : "unLock()";
        parser.setSource((lockName + "." + lockType + "." + lockProperty + ";").toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        ExpressionStatement statement = (ExpressionStatement) ((Block)parser.createAST(null)).statements().get(0);
        return statement;
    }

    /**
     * get field declaration, ast node
     * example :
     *          public Object a = new Object();
     * @param varType object type
     * @param varName variable name
     * @return ast node
     */
    public static FieldDeclaration getVarDeclaration(String varType, String varName){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        parser.setSource(("public " + varType + " " + varName + " = new " + varType + "();").toCharArray());
        TypeDeclaration newLockBlock = (TypeDeclaration) parser.createAST(null);
        return newLockBlock.getFields()[0];
    }

    public static boolean addLockDeclaration(ASTNode node, String lockName){
        ASTNode parent = node;
        while(parent != null && parent.getNodeType() != ASTNode.TYPE_DECLARATION){
            parent = parent.getParent();
        }
        if(parent == null){
            System.err.println("node has no ancestor typed type declaration");
            return false;
        }

        TypeDeclaration typeDeclaration = (TypeDeclaration) parent;

        FieldDeclaration fieldDeclaration = getVarDeclaration("ReentrantReadWriteLock", lockName);
        fieldDeclaration = (FieldDeclaration) (ASTNode.copySubtree(typeDeclaration.getAST(), fieldDeclaration));
        typeDeclaration.bodyDeclarations().add(0, fieldDeclaration);
        return true;
    }

    public static boolean surroundedByLock( Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair, int lockType, String varName, ASTNode firstStatement, ASTNode lastStatement){
        String lockName = varName + "Lock";
        ASTNode parent = parentPair.getV1();

        int preIndex = -1;
        int lastIndex = -1;
        Block block;
        if(parent instanceof Block){
            block = (Block) parent;
            Pair<ASTNode, ASTNode> statementPair = parentPair.getV2();
            if(statementPair.getV1() != null  && statementPair.getV2() != null){
                for(int i = 0; i < block.statements().size(); i ++){
                    if(block.statements().get(i) == statementPair.getV1()){
                        preIndex = i;
                    }
                    if(block.statements().get(i) == statementPair.getV2()){
                        lastIndex = i;
                    }
                }
            }
        }
        else{
            Set<ASTNode> statementSet = new HashSet<>();
            while(parent != null && parent.getNodeType() != ASTNode.BLOCK){
                if(parent instanceof Statement && !(parent instanceof Block)){
                    statementSet.add(parent);
                }
                parent = parent.getParent();
            }
            if(parent == null){
                System.err.println("node has no ancestor block");
                return false;
            }
            block = (Block) parent;
            for(int i = 0; i < block.statements().size(); i ++){
                if(statementSet.contains(block.statements().get(i))){
                    preIndex = lastIndex = i;
                }
            }
        }

        preIndex = preIndex == -1 ? 0 : preIndex;
        lastIndex = lastIndex == -1 ? block.statements().size() : lastIndex;

        ExpressionStatement preStatement = null;
        ExpressionStatement postStatement = null;

        switch (lockType){
            case READ_LOCK:
                preStatement = getReadWriteLockExpression(lockName, true, true);
                postStatement = getReadWriteLockExpression(lockName, true, false);
                preStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), preStatement);
                postStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), postStatement);
                // should add postStatement first
                block.statements().add(lastIndex + 1, postStatement);
                block.statements().add(preIndex, preStatement);
                break;
            case WRITE_LOCK:
                preStatement = getReadWriteLockExpression(lockName, false, true);
                postStatement = getReadWriteLockExpression(lockName, false, false);
                preStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), preStatement);
                postStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), postStatement);
                // should add postStatement first
                block.statements().add(lastIndex + 1, postStatement);
                block.statements().add(preIndex, preStatement);
                break;
            case READ_WRITE_LOCK:
                ASTNode node = parentPair.getV1();
                SynchronizedStatement statement = node.getAST().newSynchronizedStatement();
                statement.setExpression(node.getAST().newSimpleName(varName));
                Block tb = node.getAST().newBlock();
                // replace statements with sync statement
                for(int i = preIndex; i < lastIndex + 1; i ++){
                    ASTNode t = (ASTNode) block.statements().get(i);
                    tb.statements().add(ASTNode.copySubtree(statement.getAST(), t));
                    block.statements().remove(i);
                    i --;
                    lastIndex --;
                }
                statement.setBody(tb);
                block.statements().add(preIndex, statement);
                break;
            default:
                System.err.println("wrong type of lock");
                return false;
        }
        return true;
    }

    public static String format(String code) {
        Map<String, String> hashMap = new HashMap<>();
        hashMap.put( JavaCore.COMPILER_SOURCE, "1.5");
        hashMap.put( JavaCore.COMPILER_COMPLIANCE, "1.5");
        hashMap.put( JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "1.5");
        CodeFormatter formatter = ToolFactory.createCodeFormatter(hashMap);

        TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT, code, 0, code.length(), 0, null);
        if (edit == null) {
            return code;
        }
        IDocument doc = new Document();
        doc.set(code);
        try {
            edit.apply(doc);
        } catch (Exception e) {
            e.printStackTrace();
            return code;
        }
        return doc.get();
    }

    public static boolean addPermissionAnnotation(MethodDeclaration node, String prePermission, String postPermission){
        if("".equals(prePermission.trim())){
            prePermission = "no permission";
        }

        if ("".equals(postPermission.trim())) {
            postPermission = "no permission";
        }

        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);

        parser.setSource(("@Perm(requires=\"" + prePermission  +" in alive\", \n" +
                "ensures=\"" + postPermission +" in alive\")\n" +
                "void func() {}\n").toCharArray());

        TypeDeclaration typeDeclaration = (TypeDeclaration) parser.createAST(null);

        NormalAnnotation annotation = (NormalAnnotation) typeDeclaration.getMethods()[0].modifiers().get(0);
        annotation = (NormalAnnotation) ASTNode.copySubtree(node.getAST(), annotation);
        node.modifiers().add(0, annotation);
        return true;
    }
}
