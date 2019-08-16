package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;
import org.slf4j.LoggerFactory;
import sip4j.graphstructure.E_MethodGraph;
import sip4j.parser.AST_Parser;
import top.liebes.ast.CombineLockVisitor;
import top.liebes.ast.CompilationUnitASTRequestor;
import top.liebes.entity.LockStatementInfo;
import top.liebes.entity.Pair;
import top.liebes.env.Env;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author liebes
 */
public class ASTUtil {
    private static Logger logger = (Logger) LoggerFactory.getLogger(ASTUtil.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    public static Map<String, CompilationUnit> cpMap = new HashMap<>();

    /**
     * Parse a file to compilation unit.
     *
     * @param file
     * @return
     */
    public static CompilationUnit getCompilationUnit(File file, ASTParser tParser){
        if(cpMap.containsKey(file.getAbsolutePath())){
            return cpMap.get(file.getAbsolutePath());
        }
        ASTParser parser;
        if(tParser == null){
            parser = ASTParser.newParser(top.liebes.env.Env.JAVA_VERSION);
            parser.setResolveBindings(true);
            parser.setKind(ASTParser.K_COMPILATION_UNIT);
            parser.setBindingsRecovery(true);

            parser.setUnitName(FileUtil.removeSuffix(file.getName()));

            String[] sources = { Env.SOURCE_FOLDER };
            String[] classpath = Env.CLASSPATH;

            parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);

        }
        else{
            parser = tParser;
        }
        parser.setSource(FileUtil.getFileContents(file));
        CompilationUnit cu = (CompilationUnit) parser.createAST(null);
//        parser.createASTs();
        return cu;
    }

    public static List<Pair<String, CompilationUnit>> parseFiles(File root){
        ASTParser parser;
        parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setResolveBindings(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setBindingsRecovery(true);

        String[] sources = { Env.SOURCE_FOLDER };
        String[] classpath = Env.CLASSPATH;

        parser.setEnvironment(classpath, sources, new String[] { "UTF-8"}, true);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});
        String[] paths = new String[files.size()];
        for(int i = 0; i < files.size(); i ++){
            paths[i] = files.get(i).getAbsolutePath();
        }
        String[] srcEncodings = new String[paths.length];
        Charset charset = Charset.defaultCharset();
        for (int i = 0; i < srcEncodings.length; i++) {
            srcEncodings[i] = charset.name();
        }
        CompilationUnitASTRequestor requestor = new CompilationUnitASTRequestor();
        parser.createASTs(paths, srcEncodings, new String[]{}, requestor, null);
        for(Pair<String, CompilationUnit> pair : requestor.getFileList()){
            cpMap.put(pair.getV1(), pair.getV2());
        }
        return requestor.getFileList();
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
        String lockProperty = isLock ? "lock()" : "unlock()";
        parser.setSource((lockName + "." + lockType + "." + lockProperty + ";").toCharArray());
        parser.setKind(ASTParser.K_STATEMENTS);
        ExpressionStatement statement = (ExpressionStatement) ((Block)parser.createAST(null)).statements().get(0);
        return statement;
    }

    public static ExpressionStatement getExclusiveLockExpression(String lockName, boolean isLock){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        String lockProperty = isLock ? "lock()" : "unlock()";
        parser.setSource((lockName + "." + lockProperty + ";").toCharArray());
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
    public static FieldDeclaration getVarDeclaration(String varType, String varName, boolean isStatic){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
        String modifier = isStatic ? " static " : " ";
        parser.setSource(("public" + modifier + varType + " " + varName + " = new " + varType + "();").toCharArray());
        TypeDeclaration newLockBlock = (TypeDeclaration) parser.createAST(null);
        return newLockBlock.getFields()[0];
    }

    public static boolean addLockDeclaration(ASTNode node, String lockName, boolean isStatic){
        ASTNode parent = node;
        while(parent != null && parent.getNodeType() != ASTNode.TYPE_DECLARATION){
            parent = parent.getParent();
        }
        if(parent == null){
            logger.debug("node has no ancestor typed type declaration");
            return false;
        }

        TypeDeclaration typeDeclaration = (TypeDeclaration) parent;
        FieldDeclaration fieldDeclaration = getVarDeclaration("ReentrantReadWriteLock", lockName, isStatic);
        fieldDeclaration = (FieldDeclaration) (ASTNode.copySubtree(typeDeclaration.getAST(), fieldDeclaration));
        typeDeclaration.bodyDeclarations().add(0, fieldDeclaration);
        return true;
    }

    private static boolean surroundedByLock(Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair, int lockType, String lockName){
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
                logger.debug("node has no ancestor block");
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

        // handle the condition that last statement is a return statement
        ASTNode lastNode = (ASTNode ) block.statements().get(lastIndex);
        if(lastNode instanceof ReturnStatement){
            if(! addTmpVarForReturnStatement(block, lastIndex, "tmpVar")){
                // if return type is void, then just not include the return statement
                lastIndex --;
            }
        }

        switch (lockType){
            case LockStatementInfo.READ_LOCK:
                preStatement = getReadWriteLockExpression(lockName, true, true);
                postStatement = getReadWriteLockExpression(lockName, true, false);
                preStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), preStatement);
                postStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), postStatement);
                // should add postStatement first

                block.statements().add(lastIndex + 1, postStatement);
                block.statements().add(preIndex, preStatement);
                break;
            case LockStatementInfo.WRITE_LOCK:
                preStatement = getReadWriteLockExpression(lockName, false, true);
                postStatement = getReadWriteLockExpression(lockName, false, false);
                preStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), preStatement);
                postStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), postStatement);
                // should add postStatement first
                block.statements().add(lastIndex + 1, postStatement);
                block.statements().add(preIndex, preStatement);
                break;
            case LockStatementInfo.EXCLUSIVE_LOCK:
                preStatement = getExclusiveLockExpression(lockName, true);
                postStatement = getExclusiveLockExpression(lockName, false);
                preStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), preStatement);
                postStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), postStatement);
                // should add postStatement first
                block.statements().add(lastIndex + 1, postStatement);
                block.statements().add(preIndex, preStatement);
                break;
//                ASTNode node = parentPair.getV1();
//                SynchronizedStatement statement = node.getAST().newSynchronizedStatement();
//                statement.setExpression(node.getAST().newSimpleName(varName));
//                Block tb = node.getAST().newBlock();
//                statement.setBody(tb);
//                block.statements().add(preIndex, statement);
//                // replace statements with sync statement
//                for(int i = preIndex + 1; i < lastIndex + 2; i ++){
//                    ASTNode t = (ASTNode) block.statements().get(i);
//                    block.statements().remove(i);
//                    tb.statements().add(t);
//                    i --;
//                    lastIndex --;
//                }
//                break;
            default:
                logger.debug("wrong type of lock");
                return false;
        }

        // correct the order
        lastIndex += 2;
        for(int i = preIndex + 1; i < lastIndex; i ++){
            LockStatementInfo lockInfo = ASTUtil.getLockInfo((Statement) block.statements().get(i));
            if(lockInfo != null && lockInfo.isLock() && ! lockInfo.getName().equals(lockName)){
                Object tmp = block.statements().get(i);
                block.statements().remove(i);
                block.statements().add(preIndex, tmp);
                preIndex ++;
                i --;
            }
        }
        for(int i = lastIndex - 1; i > preIndex; i --){
            LockStatementInfo lockInfo = ASTUtil.getLockInfo((Statement) block.statements().get(i));
            if(lockInfo != null && ! lockInfo.isLock() && ! lockInfo.getName().equals(lockName)){
                Object tmp = block.statements().get(i);
                block.statements().remove(i);
                block.statements().add(lastIndex, tmp);
                lastIndex --;
                i ++;
            }
        }

        // remove lock if already some statements have already been locked, this may happens when adding read lock.
        int lockIndex = -1;
        int unLockIndex = -1;

        for(int i = 0 ; i < block.statements().size(); i ++){
            if(i == preIndex || i == lastIndex){
                continue;
            }
            if(isLockStatementByName((Statement) block.statements().get(i), true, lockName)){
                lockIndex = i;
            }
            if(isLockStatementByName((Statement) block.statements().get(i), false, lockName)) {
                unLockIndex = i;
            }
            if(lockIndex != -1 && unLockIndex != -1){
                int preLockType = getLockType((Statement) block.statements().get(lockIndex));
                if(lockIndex - 1 <= preIndex && unLockIndex + 1 >= lastIndex){
                    // var has already been locked, remove duplicated lock node
                    block.statements().remove(lastIndex);
                    block.statements().remove(preIndex);
                    preIndex = lastIndex = -1;
                    break;
                }
                else if (unLockIndex <= preIndex || lockIndex >= lastIndex){
                    // no intersection
                    if(preLockType != lockType){
                        break;
                    }
                    if(unLockIndex + 1 == preIndex){
                        block.statements().remove(preIndex);
                        block.statements().remove(unLockIndex);
                        i --;
                        preIndex = lockIndex;
                        lastIndex -= 2;
                    }
                    else if (lockIndex - 1 == lastIndex){
                        block.statements().remove(lockIndex);
                        block.statements().remove(lastIndex);
                        i -= 2;
                        lastIndex = unLockIndex - 2;
                    }
                }
                else if (lockIndex - 1 > preIndex && unLockIndex + 1 >= lastIndex){
                    if(lockType == preLockType){
                        block.statements().remove(lastIndex);
                        block.statements().remove(lockIndex);
                        i = lastIndex = unLockIndex - 2;
                    }
                    else if(lockType > preLockType){
                        ASTNode tmp = ASTNode.copySubtree(block.getAST(), (ASTNode) block.statements().get(lockIndex)) ;
                        block.statements().remove(lockIndex);
                        if(lastIndex + 1 == unLockIndex){
                            block.statements().remove(unLockIndex - 1);
                            lastIndex --;
                        }
                        else{
                            block.statements().add(lastIndex + 1 - 1, tmp);
                            lastIndex --;
                        }
                    }
                    else{
                        ASTNode tmp = ASTNode.copySubtree(block.getAST(), (ASTNode) block.statements().get(lastIndex)) ;
                        block.statements().remove(lastIndex);
                        block.statements().add(lockIndex, tmp);
                        lastIndex = lockIndex;
                    }
                }
                else if(lockIndex - 1 <= preIndex && unLockIndex + 1 < lastIndex){
                    if(lockType == preLockType){
                        block.statements().remove(unLockIndex);
                        block.statements().remove(preIndex);
                        lastIndex = lastIndex - 2;
                        i -= 2;
                    }
                    else if (lockType > preLockType){
                        ASTNode tmp = ASTNode.copySubtree(block.getAST(), (ASTNode) block.statements().get(lastIndex)) ;
                        block.statements().remove(unLockIndex);
                        block.statements().add(preIndex, tmp);
                    }
                    else{
                        ASTNode tmp = ASTNode.copySubtree(block.getAST(), (ASTNode) block.statements().get(lastIndex)) ;
                        block.statements().remove(preIndex);
                        block.statements().add(unLockIndex + 1 - 1, tmp);
                        i --;
                    }
                }
                else if (lockIndex - 1 > preIndex && unLockIndex + 1 < lastIndex){
                    if(lockType >= preLockType){
                        block.statements().remove(unLockIndex);
                        block.statements().remove(lockIndex);
                        i -= 2;
                    }
                    else{
                        // should be read lock
                        preStatement = getReadWriteLockExpression(lockName, true, true);
                        postStatement = getReadWriteLockExpression(lockName, true, false);
                        preStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), preStatement);
                        postStatement = (ExpressionStatement) ASTNode.copySubtree(parent.getAST(), postStatement);
                        block.statements().add(unLockIndex + 1, preStatement);
                        block.statements().add(lockIndex, postStatement);
                    }
                }
                lockIndex = unLockIndex = -1;
            }
        }

        ASTNode methodNode = block;
        while(methodNode != null && methodNode.getNodeType() != ASTNode.METHOD_DECLARATION){
            methodNode = methodNode.getParent();
        }
        CombineLockVisitor clv = new CombineLockVisitor(block);
        methodNode.accept(clv);
        clv.optimize();

        return true;
    }

    public static boolean isLockStatementByName(Statement statement, boolean isLock, String lockName){
        LockStatementInfo lockStatementInfo = getLockInfo(statement);
        if(lockStatementInfo == null){
            return false;
        }
        return lockStatementInfo.getName().equals(lockName) && lockStatementInfo.isLock() == isLock;
    }


    public static int getLockType(Statement statement){
        LockStatementInfo lockStatementInfo = getLockInfo(statement);
        if(lockStatementInfo == null){
            return -1;
        }
        return lockStatementInfo.getType();
    }

    public static LockStatementInfo getLockInfo(Statement statement){
        String lockName;
        int lockType;
        boolean isLock;
        if(statement.getNodeType() == ASTNode.EXPRESSION_STATEMENT){
            ExpressionStatement es = (ExpressionStatement) statement;
            if(es.getExpression().getNodeType() == ASTNode.METHOD_INVOCATION){
                MethodInvocation mi = (MethodInvocation) es.getExpression();
                if("lock".equals(mi.getName().toString())){
                    isLock = true;
                }
                else if ("unlock".equals(mi.getName().toString())){
                    isLock = false;
                }
                else{
                    return null;
                }
                if(mi.getExpression().getNodeType() == ASTNode.METHOD_INVOCATION){
                    MethodInvocation mi1 = (MethodInvocation) mi.getExpression();
                    lockName = mi1.getExpression().toString();
                    if("readLock".equals(mi1.getName().toString())){
                        lockType = LockStatementInfo.READ_LOCK;
                    }
                    else if ("writeLock".equals(mi1.getName().toString())){
                        lockType = LockStatementInfo.WRITE_LOCK;
                    }
                    else{
                        return null;
                    }
                    return new LockStatementInfo(lockType, lockName, isLock);
                }
            }
        }
        return null;
    }

    public static boolean isLockStatement(Statement statement, boolean isLock){
        LockStatementInfo lockStatementInfo = getLockInfo(statement);
        if(lockStatementInfo == null){
            return false;
        }
        return lockStatementInfo.isLock() == isLock;
    }

    public static boolean isLockStatement(Statement statement){
        LockStatementInfo lockStatementInfo = getLockInfo(statement);
        return lockStatementInfo != null;
    }

    public static boolean isLockPair(Statement statement1, Statement statement2){
        LockStatementInfo info1 = getLockInfo(statement1);
        LockStatementInfo info2 = getLockInfo(statement2);
        if(info1 != null && info2 != null){
            if(info1.getName().equals(info2.getName())
                    && info1.getType() == info2.getType()
                    && ((info1.isLock() && !info2.isLock()) || (!info1.isLock() && info2.isLock()))
            ){
                return true;
            }
        }
        return false;
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

    public static String getUniquelyIdentifiers(MethodDeclaration node){
        String methodName = node.getName().toString().trim();
        List<String> list = new ArrayList<>();
        for(Object obj : node.parameters()){
            SingleVariableDeclaration parameter = (SingleVariableDeclaration) obj;
            list.add(parameter.toString().trim());
        }
        String s = String.join(", ", list);
        if(node.resolveBinding() == null){
            return "wrongClassBinding";
        }
        String modifier = AST_Parser.setMethodModifier(node.resolveBinding()).trim();
        String returnType = "";
        if (! node.isConstructor()) {
            if (node.getReturnType2() != null && node.resolveBinding() != null){
                returnType = node.resolveBinding().getReturnType().getName();
            }
        }
        return "{" + modifier + (modifier.equals("") ? "" : " ")  + returnType + (returnType.equals("") ? "" : " ") + methodName + "(" + s + ")}";
    }

    public static String getUniquelyIdentifiers(E_MethodGraph method){
        return "{" + method.getMethodSignatures().trim() + "}";
    }

    /**
     *
     * @param block block to add tmp var
     * @param index index of return statement
     * @param varName tmp var name
     * @return true if replace, false if return type is void
     */
    public static boolean addTmpVarForReturnStatement(Block block, int index, String varName){
        ReturnStatement returnStatement = (ReturnStatement) block.statements().get(index);
        // 1 get return type
        ASTNode clazz = returnStatement.getParent();
        while (clazz != null && clazz.getNodeType() != ASTNode.METHOD_DECLARATION){
            clazz = clazz.getParent();
        }
        MethodDeclaration tmp = (MethodDeclaration) clazz;
        Type returnType = (Type) tmp.getReturnType2();
        if(returnType instanceof PrimitiveType && ((PrimitiveType)returnType).getPrimitiveTypeCode() == PrimitiveType.VOID){
            return false;
        }
        else{
            ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
            parser.setSource(("boolean " + varName + " = a;").toCharArray());
            parser.setKind(ASTParser.K_STATEMENTS);
            VariableDeclarationStatement statement = (VariableDeclarationStatement) ((Block)parser.createAST(null)).statements().get(0);
            statement.setType((Type)ASTNode.copySubtree(statement.getAST(), returnType));
            VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
            fragment.setInitializer((Expression) ASTNode.copySubtree(fragment.getAST(), returnStatement.getExpression()) );
            returnStatement.setExpression(returnStatement.getAST().newSimpleName("tmpVar"));
            block.statements().add(index, ASTNode.copySubtree(block.getAST(), statement));
        }
        return true;
    }

    public static void addLock(Pair<String, String> permissionPair,
                               Pair<ASTNode, Pair<ASTNode, ASTNode>> parentPair,
                               String lockName){
        if(permissionPair.getV1() == null){
            permissionPair.setV1("immutable");
        }
        if("immutable".equals(permissionPair.getV1() )){
            // do nothing
            return;
        }
        else if ("pure".equals(permissionPair.getV1() )){
            // add read lock
            ASTUtil.surroundedByLock(parentPair, LockStatementInfo.READ_LOCK, lockName);
        }
        else if (
                "share".equals(permissionPair.getV1())
                        || "full".equals(permissionPair.getV1())
                        || "unique".equals(permissionPair.getV1())
        ){
            ASTUtil.surroundedByLock(parentPair, LockStatementInfo.WRITE_LOCK, lockName);
            // add write lock
        }
    }

    public static ImportDeclaration getImporDeclaration(String importName){
        ASTParser parser = ASTParser.newParser(Env.JAVA_VERSION);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setSource(("import " + importName + ";").toCharArray());
        CompilationUnit node = (CompilationUnit)parser.createAST(null);
        return (ImportDeclaration) node.imports().get(0);
    }
}
