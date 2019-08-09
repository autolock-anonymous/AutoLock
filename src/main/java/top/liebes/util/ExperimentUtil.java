package top.liebes.util;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import top.liebes.env.Env;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

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

    public static void print(){
        for(Map.Entry<String, Integer[]> entry : map.entrySet()){
            System.out.println("class " + entry.getKey()
                    + "\t" + "Pure " + entry.getValue()[1]
                    + "\t" + "Share : " + entry.getValue()[2]
                    + "\t" + "Full " + entry.getValue()[3]
                    + "\t" + "Unique " + entry.getValue()[4]
            );
        }
    }
}
