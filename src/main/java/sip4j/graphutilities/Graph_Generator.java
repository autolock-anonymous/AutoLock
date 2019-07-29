package sip4j.graphutilities;
import sip4j.graphstructure.E_PackGraphs;

import java.util.HashMap;
import java.util.Map;

public class Graph_Generator {
	public static Map<String, E_PackGraphs> pgMap = new HashMap<>();

	static E_PackGraphs packgraph = new E_PackGraphs();
	Graph_Generator(){
	}
	public static void createNewPackage(){
		packgraph =  new E_PackGraphs();
	}
	public static void addPackage(E_PackGraphs _pkg){
	}
	public static E_PackGraphs getPackage(){
		return packgraph;
	}

	public static E_PackGraphs getPackgraph(String s){
		if(pgMap.containsKey(s)){
			return pgMap.get(s);
		}
		E_PackGraphs pg = new E_PackGraphs();
		pg.setName(s);
		pgMap.put(s, pg);
		return pg;
	}

	public static void clear(){
		pgMap.clear();
	}
}
