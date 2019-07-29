package top.liebes.controller;

import graphstructure.E_ClassGraphs;
import graphstructure.E_MVertice;
import graphstructure.E_MethodGraph;
import graphstructure.E_PackGraphs;
import graphutilities.Graph_Generator;
import top.liebes.entity.JFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author liebes
 */
public class JFileController {

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
            System.err.println("--------------start of file : " + filename + "----------------");
            for(E_ClassGraphs classGraph : jFile.getClassGraphs()){
                System.out.println("-----------start of class :  " + classGraph.getClassGraphName() + "----------------------");
                for(E_MethodGraph mg : classGraph.getMethodgraphs()){
                    System.out.println("-----------start of method :  " + mg.getMgraphName() + "----------------------");

                    for(E_MVertice mv : mg.getVertices()){
                        if("foo".equals(mv.getVName()) || "context".equals(mv.getVName())){
                            continue;
                        }
                        System.out.println("var name : " + mv.getVName());
                        System.out.println("require permission : " + mv.getPre_permissions());
                        System.out.println("ensure permission : " + mv.getPost_permissions());
                    }
                    System.out.println("-----------end of method :  " + mg.getMgraphName()  + "----------------------");
                }
                System.out.println("-----------end of class :  " + classGraph.getClassGraphName() + "----------------------");
            }
            System.err.println("-----------end of file :  " + filename + "----------------------");
        }

    }

    public static void collectInfo(){
        E_PackGraphs pkg = Graph_Generator.getPackage();
        LinkedList<E_ClassGraphs> classGraphs = pkg.getClasses();
        for(E_ClassGraphs classGraph : classGraphs){
            JFileController.get(classGraph.getFilename()).getClassGraphs().add(classGraph);
        }
    }

    public static void clear(){
        jfMap.clear();
    }

}
