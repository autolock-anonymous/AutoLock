package top.liebes.main;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import sip4j.datautilities.Data_Generator;
import sip4j.graphutilities.Graph_Controller;
import org.eclipse.jdt.core.dom.CompilationUnit;
import sip4j.parser.AST_Parser;
import sip4j.parser.AST_Visitor;
import top.liebes.controller.JFileController;
import top.liebes.controller.LockingPolicyController;
import top.liebes.entity.Pair;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;
import top.liebes.util.ExperimentUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author liebes
 */
public class Main  {
	private static Logger logger = (Logger) LoggerFactory.getLogger(Main.class);
	static {
		logger.setLevel(Env.LOG_LEVEL);
	}

	public static void main(String[] args) {
		if(args.length > 0){
			Env.SOURCE_FOLDER = args[0];
			if(args.length == 2){
				switch (args[1]){
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
		new Main().doThat(Env.SOURCE_FOLDER);
		ExperimentUtil.print();
	}

	private void doThat(String folder){
		long startTime = System.currentTimeMillis();
		File root = new File(folder);
		List<Pair<String, CompilationUnit>> cUnitList = ASTUtil.parseFiles(root);

		Data_Generator.createNewPackage();

		for(Pair<String, CompilationUnit> fileUnitPair : cUnitList){
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

		try{
			Graph_Controller.createGraph();
		} catch(IOException e) {
			e.printStackTrace();
			logger.warn("Graph Construction failed");
		}
		logger.info("Graph Construction and permission inference is done");
		logger.info("sip4j get information cost : " + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		// generate locking policy and write to file
		LockingPolicyController.getInstance().generate();
		logger.info("further add lock information cost : " + (System.currentTimeMillis() - startTime));
	}
}
