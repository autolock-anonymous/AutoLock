package datautilities;

import datastructure.E_Package;

import java.util.HashMap;
import java.util.Map;

public class Data_Generator {

	private static Map<String, E_Package> pkMap = new HashMap<>();

	private static E_Package packg;
	
	Data_Generator(){	
		
	}
	public static void createNewPackage(){	
		
		packg =  new E_Package();
		
	}

	public static E_Package getPackage(){
		return packg;
	}

	public static E_Package getPackage(String s){
		if(pkMap.containsKey(s)){
			return pkMap.get(s);
		}
		E_Package pack = new E_Package();
		pack.setName(s);
		pkMap.put(s, pack);
		return pack;
	}

	public static void clear(){
		pkMap.clear();
	}
	
}
