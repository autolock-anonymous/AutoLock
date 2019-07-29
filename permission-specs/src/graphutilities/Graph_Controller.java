package graphutilities;

import datastructure.*;
import datautilities.Data_Controller;
import datautilities.Data_Generator;
import graphconstruction.Graph_Construction;
import graphconstruction.LabeledEdge;
import graphstructure.E_ClassGraphs;
import graphstructure.E_MVertice;
import graphstructure.E_MethodGraph;
import graphstructure.E_PackGraphs;
import org.jgrapht.DirectedGraph;
import parser.Parser_Utilities;
import top.liebes.controller.JFileController;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
public class Graph_Controller {
	public static LinkedList<E_ClassGraphs> fetchAllGraphClasses(){
		E_PackGraphs pkg = Graph_Generator.getPackage();
		LinkedList<E_ClassGraphs> _class = null;
		if(pkg != null){
			if(pkg.getClasses() !=null){
				_class = pkg.getClasses();
			}
		}
		return _class;
	}
	public static LinkedList<E_MVertice> fetchAllVertices(){
		LinkedList<E_MethodGraph> _methds = fetchAllGraphMethods();
		LinkedList<E_MVertice> _fields = new LinkedList<E_MVertice>();
		for(E_MethodGraph m:_methds){
			LinkedList<E_MVertice> f = m.getVertices();
			for(E_MVertice  ff : f){
				if(ff.isMethod() || ff.isContext()){
					continue;
				}
				else{
					_fields.add(ff);
				}
			}
		}
		return _fields;
	}
	public static LinkedList<E_MethodGraph> fetchAllGraphMethods(){
		LinkedList<E_ClassGraphs> eclass = Graph_Controller.fetchAllGraphClasses();
		LinkedList<E_MethodGraph> _methds = new LinkedList<>();
		for(E_ClassGraphs _class: eclass){
			if(_class.getMethodgraphs() != null){
				LinkedList<E_MethodGraph> m = _class.getMethodgraphs();
				for(E_MethodGraph ms: m){
					_methds.add(ms);
				}
			}
		}
		return _methds;
	}
	public static Set<E_Method> createGraph() throws IOException{
		E_Package pkg = Data_Generator.getPackage();
		LinkedList<E_Class> eclass = pkg.getClasses();
		LinkedList<E_Method> _methds = null;
		Graph_Generator.createNewPackage();
		DirectedGraph<String, LabeledEdge> jgraph = null;
		Set<E_Method> _methodSet = null;
		File file = Parser_Utilities.createPulseFile();
		int classCounter = 0;
		int methodcount = 0;
		for(E_Class _class: eclass){
			_methds = _class.getMethods();
			Graph_Construction.constructGraph(_class, _methds);
			///	LinkedList<String> sipOutput = Graph_Utilities.generatePulsePermissions(classCounter);
			LinkedList<String> sipOutput = Graph_Utilities.generateObjectPermissions(classCounter,methodcount);

			// collect information for next work , by liebes
			JFileController.collectInfo();

			Parser_Utilities.writePulseSpecifications(file, sipOutput);
			/// File anno_file = Parser_Utilities.createAnnotationFile();
			/// LinkedList<String> annotOutput = Graph_Utilities.generateObjectAnnotations(classCounter,methodcount);
			/// Parser_Utilities.writePulseSpecifications(anno_file, annotOutput);
			classCounter++;
		}
        /*IProject[] projects = Workspace_Utilities.getWorkspaceProjects();
		IProject pulseProj = Parser_Utilities.getPulseProject(projects);
		IFolder destination = pulseProj.getFolder("src/outputs");
		String folder = destination.getLocation().toString();
		FileOutputStream fos = new FileOutputStream(folder+"/pulseTest.java");
		FileDescriptor fd = fos.getFD();
		fd.sync();*/
		//Permission_Generation.generatePermissions();
		return _methodSet;

	}
	public static LinkedList<E_Method> fetchAllMethods(){
		return Data_Controller.fetchAllMethods();
	}
	public static LinkedList<E_MInvokedMethod> fetchSubMethods(E_Method m){
		LinkedList<E_MInvokedMethod> tempList = new LinkedList<E_MInvokedMethod>();
		return Data_Controller.fetchSubMethodCalls(m, tempList);
	}
	public static LinkedList<E_MRefField> fetchsubMethodFields(E_MInvokedMethod im){
		LinkedList<E_MRefField> _invmref = null;
		LinkedList<E_Method> _methds = Data_Controller.fetchAllMethods();
		/// System.out.println("Invoked Method Name= "+im.getName().toString());
		for(E_Method m : _methds){
			if(m.getName().equals(im.getName())){
				/// System.out.println("You invoked method "+im.getName()+" matches with "+m.getName());
				_invmref = Data_Controller.fetchSubMethodCallFields(im);
			}
		}
		return _invmref;
	}
	public static String getGraphBody(E_MethodGraph graph){
		List<String> list = graph.getMethodBody();
		Object[] array = list.toArray();
		System.out.println(Arrays.toString(array));
		return Arrays.toString(array).replace("[", "").replace("]", "").replace(',', ' ').trim();
	}
}