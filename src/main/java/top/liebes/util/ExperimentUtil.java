package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.LoggerFactory;
import top.liebes.ast.ExperimentVisitor;
import top.liebes.controller.JFileController;
import top.liebes.entity.ExpBean;
import top.liebes.entity.JFile;
import top.liebes.env.Env;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExperimentUtil {
    public static Map<String, Integer[]> map = new HashMap<>();
    public static Logger logger = (Logger) LoggerFactory.getLogger(ExperimentUtil.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }
    public static final String SHARE = "share";
    public static final String IMMUTABLE = "immutable";
    public static final String UNIQUE = "unique";
    public static final String PURE = "pure";
    public static final String FULL = "full";

    private static ExpBean expInfo = new ExpBean();

    public static void increaseNewLockDeclaration(){
        expInfo.setNewLockDeclaration(expInfo.getNewLockDeclaration() + 1);
    }
    public static void increaseTotalLockDeclaration(){
        expInfo.setTotalLockDeclaration(expInfo.getTotalLockDeclaration() + 1);
    }
    public static void increaseMethod(){
        expInfo.setNumberOfMethod(expInfo.getNumberOfMethod() + 1);
    }
    public static void increaseClass(){
        expInfo.setNumberOfClass(expInfo.getNumberOfClass() + 1);
    }
    public static void increaseNewLockInsertion(){
        expInfo.setNewLockInsertion(expInfo.getNewLockInsertion() + 1);
    }
    public static void increaseTotalLockInsertion(){
        expInfo.setTotalLockInsertion(expInfo.getTotalLockInsertion() + 1);
    }
    public static void increasePure(){
        expInfo.setNumberOfPure(expInfo.getNumberOfPure() + 1);
    }
    public static void increaseShare(){
        expInfo.setNumberOfShare(expInfo.getNumberOfShare() + 1);
    }
    public static void increaseFull(){
        expInfo.setNumberOfFull(expInfo.getNumberOfFull() + 1);
    }
    public static void increaseUnique(){
        expInfo.setNumberOfUnique(expInfo.getNumberOfUnique() + 1);
    }
    public static void increaseImmutable(){
        expInfo.setNumberOfImmutable(expInfo.getNumberOfImmutable() + 1);
    }
    public static void setSip4jTime(long time){
        expInfo.setSip4jAnalysisTime(time);
    }
    public static void increaseInferLockTime(long time){
        expInfo.setInferLockTime(expInfo.getInferLockTime() + time);
    }
    public static void increaseApplyLockTime(long time){
        expInfo.setApplyLockTime(expInfo.getApplyLockTime() + time);
    }
    public static void increaseField(String fName){
        expInfo.getFieldsCount().putIfAbsent(fName, 0);
        expInfo.getFieldsCount().computeIfPresent(fName, (key, value) -> ++value);
    }

    public static void calClassInfo(){
        String folder = Env.SOURCE_FOLDER;
        File root = new File(folder);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});

        // Read java files from folder
        ExperimentVisitor vistor = new ExperimentVisitor();
        for(File file : files){
            final CompilationUnit cu = ASTUtil.getCompilationUnit(file, null);
            cu.accept(vistor);
        }
    }

    public static void print(){
        calClassInfo();
        String[] s = Env.SOURCE_FOLDER.split("/");
        int total = 0;
        for(Map.Entry<String, Integer> entry : expInfo.getFieldsCount().entrySet()){
            total += entry.getValue();
        }
        System.out.println(s[s.length - 2] + "/" + s[s.length - 1] + " | "
                + expInfo.getNumberOfClass() + " | "
                + expInfo.getNumberOfMethod() + " | "
                + expInfo.getTotalLockDeclaration() + " | "
                + expInfo.getTotalLockInsertion() + " | "
                + expInfo.getNumberOfPure() + " | "
                + expInfo.getNumberOfShare() + " | "
                + expInfo.getNumberOfFull() + " | "
                + expInfo.getNumberOfUnique() + " | "
                + expInfo.getSip4jAnalysisTime() + " | "
                + expInfo.getInferLockTime() + " | "
                + expInfo.getApplyLockTime() + " | "
                + total
        );

    }
}
