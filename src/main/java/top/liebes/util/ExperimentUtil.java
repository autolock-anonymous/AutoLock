package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.slf4j.LoggerFactory;
import top.liebes.ast.ExperimentVisitor;
import top.liebes.controller.JFileController;
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

    public static void increase(String classname, String type){
        map.putIfAbsent(classname, new Integer[]{0, 0, 0, 0, 0});
        map.computeIfPresent(classname, (k, countArr) -> {
            switch (type){
                case IMMUTABLE:
                    countArr[0] ++;
                    break;
                case PURE:
                    countArr[1] ++;
                    break;
                case SHARE:
                    countArr[2] ++;
                    break;
                case FULL:
                    countArr[3] ++;
                    break;
                case UNIQUE:
                    countArr[4] ++;
            }
            return countArr;
        });
    }

    public static void calClassInfo(){
        String folder = Env.SOURCE_FOLDER;
        File root = new File(folder);
        List<File> files = FileUtil.getFiles(root, new String[]{"java"});

        // Read java files from folder
        ExperimentVisitor vistor = new ExperimentVisitor();
        for(File file : files){
            JFile jFile = JFileController.get(file.getName());
            final CompilationUnit cu = ASTUtil.getCompilationUnit(file, null);
            cu.accept(vistor);
        }

        System.out.println((vistor.getNumberOfClass() - 1)  + "\t" + (vistor.getNumberOfMethod() - 1) + "\t" + vistor.getNumberOfLock());
    }

    public static void print(){
        calClassInfo();
        int a[] = new int[5];
        for(Map.Entry<String, Integer[]> entry : map.entrySet()){
            a[1] += entry.getValue()[1];
            a[2] += entry.getValue()[2];
            a[3] += entry.getValue()[3];
            a[4] += entry.getValue()[4];
        }
        System.out.println(a[1] + "\t" + a[2] + "\t" +a[3] + "\t" +a[4]);

    }
}
