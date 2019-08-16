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

        System.out.println("total analysis : ");
        System.out.println("class : " + (vistor.getNumberOfClass() - 1));
        System.out.println("method: " + (vistor.getNumberOfMethod() - 1));
        System.out.println("lock : " + vistor.getNumberOfLock());
    }

    public static void print(){
        for(Map.Entry<String, Integer[]> entry : map.entrySet()){
            System.out.println("class " + entry.getKey()
                    + "\t" + "Pure " + entry.getValue()[1]
                    + "\t" + "Share : " + entry.getValue()[2]
                    + "\t" + "Full " + entry.getValue()[3]
                    + "\t" + "Unique " + entry.getValue()[4]
            );
        }
        calClassInfo();
    }
}
