package main;

import datautilities.Data_Controller;
import datautilities.Data_Generator;
import graphutilities.Graph_Controller;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import parser.AST_Parser;
import parser.AST_Visitor;
import top.liebes.controller.JFileController;
import top.liebes.entity.JFile;
import top.liebes.env.Env;
import top.liebes.util.ASTUtil;
import top.liebes.util.FileUtil;
import top.liebes.util.GraphUtil;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class Main  {

	static LinkedList<String> inputFiles;

	private static final Logger logger = Logger.getLogger(Main.class.getName());

	public static void main(String[] args) {
		if(args.length == 1){
			Env.SOURCE_FOLDER = args[0];
		}
		System.out.println("start to handle folder : " + Env.SOURCE_FOLDER);
//		String src = "/Users/liebes/project/laboratory/Sip4J/runtime-sip4j-application/benchmarks/src/";
		doThat(Env.SOURCE_FOLDER);
		// getAnnotationsCompilationUnit
//		long end = System.nanoTime();
//		long elapsedTime = end - startTime;
//		double seconds = (double)elapsedTime / 1000000000.0;
//		System.out.println("Seconds Time = "+seconds);
		////////////////////////////////////////////////

		/// permissions have been already generated
//		IJavaElement javaElement = UserSelectedClasses_Analysis.getPulseCompilationUnit();
//		System.out.println("Pulse processing starts here");
//		final int testType = 0;
//		List<ICompilationUnit> temp = Workspace_Utilities.collectCompilationUnits(javaElement);
//		if(temp != null) {
//			final List<ICompilationUnit> compUnits = temp;
//			UserSelectedClassesAnalysis UAnalysis = new UserSelectedClassesAnalysis();
//			UAnalysis.analyzeFromPlugin(compUnits, testType);
//		}
//
//		try {
//			testType=test;
//			EVMDD_SMC_Generator.reset();
//			Boolean JML;
//			for (ICompilationUnit cunit : compilationUnitList) {
//				JML=false;
//				String prog=getInputStream(cunit);
//				CompilationUnit cu = null;
//				if (prog.contains("//@") == true){
//					JML = true;
//					JMLAnnotatedJavaClass JClass = new JMLAnnotatedJavaClass();
//					prog = JClass.translateJMLAnnotationsToPlural(prog);
//					cu = getCompilationUnit(prog);
//				}
//				else{
//					cu = getCompilationUnit(cunit);
//				}
//
//				SMC_Visitor visitor = new SMC_Visitor();
//				cu.accept(visitor);
//				if (JML==true){
//					prog=EVMDD_SMC_Generator.modifyConstructorSpecifications(prog);
//					String className=EVMDD_SMC_Generator.getPkgObject().getClasses().getLast().getName();
//					E_GeneratedPluralSpecification.createFromPlugin(prog,className);
//				}
//			}
//			E_SMC_Model.generateSMCmodel_Plugin(EVMDD_SMC_Generator.getPkgObject(),test);
//			starttime = getTime();
//			//uncomment later
//			//callModelCheckerThroughPlugin();
//			//uncomment later
//			callModelCheckerThroughCommandLine();
//		}
//
//
//		catch (Exception e) {e.printStackTrace();}



//		System.out.println("Third stage is done");


		//MyClassLoader.getAnnotatedCompilationUnits();

		//inputFiles= new LinkedList<String>();
		//inputFiles.add(testRead("D:\\PhD-Folder-August-2012\\PulseWebSite\\target.java"));
		//UserSelectedClassesAnalysis.analyzeFromCommandLine(inputFiles, "0","2");
		//if (num>=2){
		//seprateJavaFile(args[0]);
		//UserSelectedClassesAnalysis.analyzeFromCommandLine(inputFiles, args[1],args[2]);
		//}
		/*Graph_Construction obj = new Graph_Construction();
		 obj.createGraph();*/
		//Sip4JIFileAction.run();
	}






	private static void seprateJavaFile(String str) {
		boolean flag=false;
		do {
			if (str.lastIndexOf("ENDOFCLASS")>0){
				int index=str.indexOf("ENDOFCLASS");
				inputFiles.add(str.substring(0,index));
				str=str.substring(index+10,str.length());
				flag=true;
			}
			else{
				str=str.trim();
				if (str.isEmpty()==false)
					inputFiles.add(str);
				flag=false;
			}
		}
		while(flag);
	}


	private static String testRead(String file)
	{
		String contents="";
		try{
			FileInputStream fstream = new FileInputStream(file);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
				contents=contents+strLine;
			}

			in.close();
		}
		catch (Exception e){System.err.println("Error: " + e.getMessage());}

		return contents;
	}

	private static void anTest(){

		String str= "requires_clause_of_APDU_setIncomingAndReceive_case1_0_0:";
		Boolean bRequires=true;
		int j=str.indexOf("_of_")+4;
		str=str.substring(j);
		j=str.indexOf("_");
		String className=str.substring(0,j);
		str=str.substring(j+1);
		//j=str.indexOf("_");

		int i=str.indexOf(":");
		String methodName=str.substring(0,i-4);

		String reachability=str.substring(i+1);
		reachability=reachability.trim();

	}

	public static String getPath(){


		return "/Users/liebes/project/laboratory/Sip4J/tmp";
	}

	public static void doThat(String folder){
		long startTime = System.currentTimeMillis();
		File root = new File(folder);
		List<File> files = FileUtil.getFiles(root, new String[]{"java"});

		Data_Generator.createNewPackage();

		// Read java files from folder
		for(File file : files){
			JFileController.put(file);
			final CompilationUnit cu = ASTUtil.getCompilationUnit(file);
			AST_Visitor visitor = new AST_Visitor(file.getName());
			try {
				cu.accept(visitor);
			} catch (IllegalArgumentException ex) {
				ex.printStackTrace();
			}
		}

		AST_Parser.extractContextInformation();
		System.out.println("meta-data extraction is done");

		try{
			Graph_Controller.createGraph();
		} catch(IOException e) {
			e.printStackTrace();
			logger.warning("Graph Construction failed");
		}
		System.out.println("Graph Construction and permission inference is done");
		System.out.println("sip4j get information cost : " + (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		GraphUtil.test();
		System.out.println("further add lock information cost : " + (System.currentTimeMillis() - startTime));
	}

	public static List<File> getFolders(File root){
		List<File> res = new ArrayList<>();
		if(!root.exists() || ! root.isDirectory()){
			return res;
		}
		File[] files = root.listFiles();
		boolean flag = true;
		for(int i = 0; i < files.length; i ++){
			if(files[i].isDirectory()){
				res.addAll(getFolders(files[i]));
				flag = false;
			}
		}
		if(flag){
			res.add(root);
		}
		return res;
	}
}
