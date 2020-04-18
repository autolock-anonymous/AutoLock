package top.liebes.main;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.slf4j.LoggerFactory;
import sip4j.datautilities.Data_Generator;
import sip4j.graphutilities.Graph_Controller;
import sip4j.parser.AST_Parser;
import sip4j.parser.AST_Visitor;
import top.liebes.ast.AddGetterVisitor;
import top.liebes.controller.JFileController;
import top.liebes.controller.LockingPolicyController;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;
import top.liebes.util.ExperimentUtil;
import top.liebes.util.FileUtil;

import java.io.*;
import java.util.*;

/**
 * @author liebes
 */
public class Main {
    private static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);

    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            Env.SOURCE_FOLDER = args[0];
            if (args.length == 2) {
                switch (args[1]) {
                    case "debug":
                        Env.LOG_LEVEL = Level.DEBUG;
                        break;
                    case "info":
                        Env.LOG_LEVEL = Level.INFO;
                        break;
                    case "warn":
                        Env.LOG_LEVEL = Level.WARN;
                        break;
                    case "error":
                        Env.LOG_LEVEL = Level.ERROR;
                        break;
                    case "off":
                        Env.LOG_LEVEL = Level.OFF;
                        break;
                    default:
                        Env.LOG_LEVEL = Level.INFO;
                }
            }
        }
        logger.setLevel(Env.LOG_LEVEL);
        logger.info("start to handle folder : " + Env.SOURCE_FOLDER);

        File file = new File(Env.SOURCE_FOLDER + "/" + "lib");
        if (file.exists() && file.isDirectory()) {
            List<String> arr = new ArrayList<>(Arrays.asList(Env.CLASSPATH));
            for (File jar : file.listFiles()) {
                if ("jar".equals(FileUtil.getSuffix(jar.getName()))) {
                    arr.add(jar.getAbsolutePath());
                }
            }
            Env.CLASSPATH = arr.toArray(new String[arr.size()]);
            logger.info("CLASSPATH SET : " + arr);
        }

        List<File> files = FileUtil.getFiles(new File(Env.SOURCE_FOLDER), new String[]{"java"});
        for (File tFile : files) {
            final CompilationUnit cu = ASTUtil.getCompilationUnit(tFile, null);
            AddGetterVisitor addGetterVisitor = new AddGetterVisitor();
            try {
                cu.accept(addGetterVisitor);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            Set<FieldDeclaration> fieldSet = addGetterVisitor.getFieldSet();
            Set<MethodDeclaration> methodSet = addGetterVisitor.getMethodSet();
            for (FieldDeclaration fd : fieldSet) {
                if (fd.fragments().size() > 0 && fd.fragments().get(0) instanceof VariableDeclarationFragment && fd.getParent() instanceof TypeDeclaration) {
                    boolean hasGetter = false;
                    String fdName = ((VariableDeclarationFragment) fd.fragments().get(0)).getName().toString();
                    for (MethodDeclaration md : methodSet) {
                        String methodName = md.getName().toString();
                        // 已经有 getter 了，只需要在 getter 的最开始进行
                        if (methodName.equalsIgnoreCase("get" + fdName)) {
                            hasGetter = true;
                            VariableDeclarationFragment fragment = md.getAST().newVariableDeclarationFragment();
                            fragment.setName(md.getAST().newSimpleName("cloner"));
                            ClassInstanceCreation ci = md.getAST().newClassInstanceCreation();
                            ci.setType(md.getAST().newSimpleType(md.getAST().newSimpleName("Cloner")));
                            fragment.setInitializer(ci);
                            VariableDeclarationStatement st1 = md.getAST().newVariableDeclarationStatement(fragment);
                            st1.setType(md.getAST().newSimpleType(md.getAST().newSimpleName("Cloner")));
                            md.getBody().statements().add(0, ASTNode.copySubtree(md.getAST(), st1));
                            Assignment ep = md.getAST().newAssignment();
                            ep.setLeftHandSide(md.getAST().newSimpleName(fdName));
                            MethodInvocation mi = md.getAST().newMethodInvocation();
                            mi.setExpression(md.getAST().newSimpleName("cloner"));
                            mi.setName(md.getAST().newSimpleName("deepClone"));
                            mi.arguments().add(md.getAST().newSimpleName(fdName));
                            ep.setRightHandSide(mi);
                            ExpressionStatement es = md.getAST().newExpressionStatement(ep);
                            md.getBody().statements().add(1, ASTNode.copySubtree(md.getAST(), es));
                        }
                    }
                    if (!hasGetter) {
                        boolean isStatic = false;
                        String methodName = "get" + Character.toUpperCase(fdName.charAt(0)) + fdName.substring(1);
                        String body = "Cloner cloner = new Cloner();\n" +
                                fdName + " = cloner.deepClone(" + fdName + ");\n" +
                                "return " + fdName + ";";
                        MethodDeclaration getterMethod = ASTUtil.getMethodDeclaration(
                                new String[]{"public", Modifier.isStatic(fd.getModifiers()) ? "static" : ""},
                                fd.getType().toString(),
                                methodName,
                                new String[]{},
                                body
                        );
                        ((TypeDeclaration) fd.getParent()).bodyDeclarations().add(ASTNode.copySubtree(fd.getAST(), getterMethod));
                    }
                }
            }
            FileUtil.writeToFile(tFile.getPath(), cu.toString());
        }
        files = FileUtil.getFiles(new File(Env.SOURCE_FOLDER), new String[]{"java"});
        for (File tFile : files) {
            final CompilationUnit cu = ASTUtil.getCompilationUnit(tFile, null);
            List<Pair<Integer, Integer>> positionList = new ArrayList<>();
            cu.accept(new ASTVisitor() {
                @Override
                public boolean visit(SimpleName node) {
                    IBinding binding = node.resolveBinding();
                    // check if the simple name is a class member, not parameter or reserved word
                    if (binding instanceof IVariableBinding) {
                        IVariableBinding variableBinding = (IVariableBinding) binding;
                        // not in same package
                        if (variableBinding.isField() && !Modifier.isFinal(variableBinding.getModifiers()) && ! variableBinding.getType().isPrimitive()){
                            boolean flag = false;
                            ASTNode tNode = node;
                            while( tNode != null && ! (tNode instanceof TypeDeclaration)){
                                tNode = tNode.getParent();
                            }

                            if(tNode != null){
                                Set<ITypeBinding> superTypes = new HashSet<>();
                                TypeDeclaration td = (TypeDeclaration) tNode;
                                ITypeBinding typeBinding = null;
                                if(td.getSuperclassType() != null){
                                    typeBinding = td.getSuperclassType().resolveBinding();
                                }
                                while(typeBinding != null){
                                    superTypes.add(typeBinding);
                                    typeBinding = typeBinding.getSuperclass();
                                }
                                if(superTypes.contains(variableBinding.getDeclaringClass())){
                                    flag = true;
                                }
                            }
                            if(! ASTUtil.getPackageName(node).equals(variableBinding.getDeclaringClass().getPackage().getName()) && ! flag){
                                // TODO setter or getter
                                positionList.add(Pair.make(node.getStartPosition(), node.getLength()));
                            }
                        }
                    }
                    return super.visit(node);
                }
            });
            if(positionList.size() > 0){
                System.out.println(tFile.getPath());
                Collections.sort(positionList, (o1, o2) -> o2.getV1() - o1.getV1());
                Document document = new Document(new String(FileUtil.getFileContents(tFile)));
                for(Pair<Integer, Integer> pair : positionList){
                    try{
                        String word = document.get(pair.getV1(), pair.getV2());
                        String newWord = "get" + Character.toUpperCase(word.charAt(0)) + word.substring(1) + "()";
                        document.replace(pair.getV1(), pair.getV2(), newWord);
                        System.out.println(word + " -> " + newWord);
                    }
                    catch (BadLocationException e){
                        e.printStackTrace();
                    }
                }
                FileUtil.writeToFile(tFile.getPath(), document.get());
            }
        }


		new Main().doThat(Env.SOURCE_FOLDER);
		ExperimentUtil.print();
    }

    private void doThat(String folder) {
        long startTime = System.currentTimeMillis();
        File root = new File(folder);
        List<Pair<String, CompilationUnit>> cUnitList = ASTUtil.parseFiles(root);

        Data_Generator.createNewPackage();

        for (Pair<String, CompilationUnit> fileUnitPair : cUnitList) {
            File file = new File(fileUnitPair.getV1());
            JFileController.put(file);
            final CompilationUnit cu = fileUnitPair.getV2();
            AST_Visitor visitor = new AST_Visitor(file.getName());
            try {
                cu.accept(visitor);
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }

        AST_Parser.extractContextInformation();
        logger.info("sip4j meta-data extraction is done");
        try {
            Graph_Controller.createGraph();
        } catch (IOException e) {
            e.printStackTrace();
            logger.warn("Graph Construction failed");
        }
        logger.info("Graph Construction and permission inference is done");
        logger.info("sip4j get information cost : " + (System.currentTimeMillis() - startTime));
        ExperimentUtil.setSip4jTime((System.currentTimeMillis() - startTime));
        startTime = System.currentTimeMillis();
        // generate locking policy and write to file
        LockingPolicyController.getInstance().generate();
        logger.info("further add lock information cost : " + (System.currentTimeMillis() - startTime));
    }
}
