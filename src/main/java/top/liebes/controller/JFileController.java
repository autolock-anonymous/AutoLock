package top.liebes.controller;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import sip4j.graphstructure.E_ClassGraphs;
import sip4j.graphstructure.E_MVertice;
import sip4j.graphstructure.E_MethodGraph;
import sip4j.graphstructure.E_PackGraphs;
import sip4j.graphutilities.Graph_Generator;
import top.liebes.entity.JFile;
import top.liebes.env.Env;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author liebes
 */
public class JFileController {
    private static Logger logger = (Logger) LoggerFactory.getLogger(JFileController.class);
    static {
        logger.setLevel(Env.LOG_LEVEL);
    }

    public static Map<String, JFile> jfMap = new HashMap<>();

    public static JFile get(String filename){
        if(jfMap.containsKey(filename)){
            return jfMap.get(filename);
        }
        return null;
    }

    public static void put(File file){
        JFile jFile = new JFile();
        jFile.setFile(file);
        jFile.setClassGraphs(new ArrayList<>());
        jfMap.put(file.getName(), jFile);
    }

    public static void printInfo(){
        for(String filename : jfMap.keySet()){
            JFile jFile = jfMap.get(filename);
            logger.debug("--------------start of file : " + filename + "----------------");
            for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
                logger.debug("-----------start of class :  " + classGraph.getClassGraphName() + "----------------------");
                for(E_MethodGraph mg : classGraph.getMethodgraphs()){
                    logger.debug("-----------start of method :  " + mg.getMgraphName() + "----------------------");

                    for(E_MVertice mv : mg.getVertices()){
                        if("foo".equals(mv.getVName()) || "context".equals(mv.getVName())){
                            continue;
                        }
                        logger.debug("var name : " + mv.getVName());
                        logger.debug("require permission : " + mv.getPre_permissions());
                        logger.debug("ensure permission : " + mv.getPost_permissions());
                    }
                    logger.debug("-----------end of method :  " + mg.getMgraphName()  + "----------------------");
                }
                logger.debug("-----------end of class :  " + classGraph.getClassGraphName() + "----------------------");
            }
            logger.debug("-----------end of file :  " + filename + "----------------------");
        }

    }

    public static void collectInfo(){
        E_PackGraphs pkg = Graph_Generator.getPackage();
        LinkedList<E_ClassGraphs> classGraphs = pkg.getClasses();
        for(E_ClassGraphs classGraph : classGraphs){
            if(classGraph.getFilename() == null){
                continue;
            }
            JFileController.get(classGraph.getFilename()).getClassGraphs().add(classGraph);
        }
    }

    public static void clear(){
        jfMap.clear();
    }

}
