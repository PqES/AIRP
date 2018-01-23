package airptool.core;

import java.sql.SQLData;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.google.common.collect.Collections2;

import airptool.util.AirpUtil;

public class DataStructure {
	/**
	 * String: class name Collection<Dependency>: Collection of established
	 * dependencies
	 */
	public String projectName = "";
	public Map<String, Collection<Object[]>> projectClassesCP = null;
	public Map<String, HashMap<String, Collection<Object[]>>> projectClassesMC = null;
	public Map<String, HashMap<String, HashMap<String, Collection<Object[]>>>> projectClassesBM = null;


	public DataStructure(IProject project) throws CoreException, ParseException {
		this.projectName = project.getName();
		
		this.projectClassesCP = new HashMap<String, Collection<Object[]>>();
		
		this.projectClassesMC = new HashMap<String, HashMap<String, Collection<Object[]>>>();
		this.projectClassesBM = new HashMap<String, HashMap<String, HashMap<String, Collection<Object[]>>>>();

		Collection<String> classes = AirpUtil.getClassNames(project);
		for (String className : classes) {
			this.projectClassesCP.put(className, null);
			this.projectClassesMC.put(className, null);
			this.projectClassesBM.put(className, null);
			
			/*Collection<String> methods = AirpUtil.getMethodNames(project, className);
			for (String method : methods) {
				this.projectClassesMC.get(className).put(method, null);
				this.projectClassesBM.get(className).put(method, null);
			}*/
		}

	}

	public Set<String> getProjectClasses() {
		return projectClassesCP.keySet();
	}
	
	public String getProjectName() {
		return projectName;
	}
	
	/*public Map<String, ArrayList<String>> getProjectMethods() {
		Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		ArrayList<String> listaString = new ArrayList<String>();
		for (Map.Entry<String, Collection<Object[]>> entry : projectClassesMC.entrySet()){
			listaString.clear();
			 for(Object[] dep : entry.getValue()){
				 if(!listaString.contains(dep[2].toString())){
					 listaString.add(dep[2].toString());
				 }
			 }
			 result.put(entry.getKey(),new ArrayList<String>(listaString));
		}
		
		return result;
	}*/
	
	public Map<String, ArrayList<String>> getProjectMethods() {
		Map<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		ArrayList<String> listaString = new ArrayList<String>();
		for (Map.Entry<String, HashMap<String, Collection<Object[]>>> entry : projectClassesMC.entrySet()){
			try{
				listaString.clear();
			 for(Map.Entry<String, Collection<Object[]>> entry2 : entry.getValue().entrySet()){
				 try{
				 if(!listaString.contains(entry2.getKey())){
					 listaString.add(entry2.getKey());
				 }
				 }catch(NullPointerException npe){
						System.out.println("parou_novo");
					}
			 }
			 result.put(entry.getKey(),new ArrayList<String>(listaString));
			}catch(NullPointerException npe){
				System.out.println("parou_novo2");
			}
		}
		
		return result;
	}
	
	/*public Map<String, Map<String, ArrayList<Integer>>> getProjectBlocks() {
		Map<String, Map<String, ArrayList<Integer>>> result = new HashMap<String, Map<String, ArrayList<Integer>>>();
		Map<String, ArrayList<Integer>> result2 = new HashMap<String, ArrayList<Integer>>();
		ArrayList<Integer> listaInteger = new ArrayList<Integer>();
		String methodName= "";
		
		for (Map.Entry<String, Collection<Object[]>> entry : projectClassesBM.entrySet()){
			listaInteger.clear();
			 for(Object[] dep : entry.getValue()){
				 if(methodName.equals(dep[2].toString())){
					 if(!listaInteger.contains(Integer.parseInt(dep[3].toString()))){
						 listaInteger.add(Integer.parseInt(dep[3].toString()));
					 }
				 }else{
					 listaInteger.clear();
					 if(result2.containsKey(dep[2].toString())){
						 listaInteger.addAll(result2.get(dep[2].toString()));
					 }
					 
					 if(!listaInteger.contains(Integer.parseInt(dep[3].toString()))){
						 listaInteger.add(Integer.parseInt(dep[3].toString()));
					 }
					 methodName = dep[2].toString();
				 }
				 result2.put(dep[2].toString(), new ArrayList<Integer>(listaInteger));
			 }
			 result.put(entry.getKey(),new HashMap<String, ArrayList<Integer>>(result2)); 
		}
		
		return result;
	}*/

	public Map<String, Map<String, ArrayList<String>>> getProjectBlocks() {
		Map<String, Map<String, ArrayList<String>>> result = new HashMap<String, Map<String, ArrayList<String>>>();
		Map<String, ArrayList<String>> result2 = new HashMap<String, ArrayList<String>>();
		ArrayList<String> listBlocksNum = new ArrayList<String>();
		String methodName= "";
		String className= "";
		
		for (Map.Entry<String, HashMap<String, HashMap<String, Collection<Object[]>>>> entry : projectClassesBM.entrySet()){
			listBlocksNum.clear();
			result2.clear();
			 for(Map.Entry<String, HashMap<String, Collection<Object[]>>> entry2 : entry.getValue().entrySet()){
				 for(Map.Entry<String, Collection<Object[]>> entry3 : entry2.getValue().entrySet()){
					 if(methodName.equals(entry2.getKey())){
						 if(!listBlocksNum.contains(entry3.getKey())){
							 listBlocksNum.add(entry3.getKey());
						 }
					 }else{
						 listBlocksNum.clear();
						 if(result2.containsKey(entry2.getKey())){
							 listBlocksNum.addAll(result2.get(entry2.getKey()));
						 }
						 
						 if(!listBlocksNum.contains(entry3.getKey())){
							 listBlocksNum.add(entry3.getKey());
						 }
						 methodName = entry2.getKey();
						 className = entry.getKey();
					 }
					 result2.put(entry2.getKey(), new ArrayList<String>(listBlocksNum));
				 }
			 }
			 result.put(entry.getKey(),new HashMap<String, ArrayList<String>>(result2)); 
		}
		
		return result;
	}
	
	public Map<String, Collection<Object[]>> getAllDependenciesCP() {
		return projectClassesCP;
	}
	
	public Map<String, HashMap<String, HashMap<String, Collection<Object[]>>>> getAllDependenciesBM() {
		return projectClassesBM;
	}
	
	public Map<String, HashMap<String, Collection<Object[]>>> getAllDependenciesMC() {
		return projectClassesMC;
	}

	public Collection<Object[]> getDependenciesCP(String className) {
		return projectClassesCP.get(className);
	}
	
	public HashMap<String, Collection<Object[]>> getDependenciesMC(String className) {
	//public Collection<Object[]> getDependenciesMC(String className) {
		return projectClassesMC.get(className);
	}
	
	public HashMap<String, Collection<Object[]>> getDependenciesBM(String className, String method) {
	//public Collection<Object[]> getDependenciesBM(String className) {
		for(Map.Entry<String, HashMap<String, HashMap<String, Collection<Object[]>>>> entry : projectClassesBM.entrySet()){
			for(Map.Entry<String, HashMap<String, Collection<Object[]>>> entry2 : entry.getValue().entrySet()){
				if(entry.getKey().equals(className) && entry2.getKey().equals(method)){
					return entry2.getValue();
				}
			}
		}
			return null;
	}
	
	public HashMap<String,HashMap<String, Collection<Object[]>>> getDependenciesBM(String className) {
		//public Collection<Object[]> getDependenciesBM(String className) {
			
			return projectClassesBM.get(className);
		}

	public void updateDependenciesCP(String className, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClassesCP.put(className, dependencies);
	}
	
	/*public void updateDependenciesMC(String className, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClassesMC.put(className, dependencies);
	}*/
	
	public void updateDependenciesMC(String className, Collection<Object[]> dependencies) {
		try{
		this.filterCommonDependencies(dependencies);
		Map<String, Collection<Object[]>> tempMC = new HashMap<String, Collection<Object[]>>();
		Collection<Object[]> colec = new ArrayList<Object[]>();
		String methodName = "";
		
		projectClassesMC.put(className, new HashMap<String,Collection<Object[]>>());
		//}
			
			
			for(Object[] o : dependencies){
				
				methodName=o[2].toString();
				
				if(projectClassesMC.get(className)==null){
					colec = new ArrayList<Object[]>();
					colec.add(o);
				}else if(projectClassesMC.get(className).get(methodName)==null){
					colec = new ArrayList<Object[]>();
					colec.add(o);
				}else{
					colec= new ArrayList<Object[]>(projectClassesMC.get(className).get(methodName));
					colec.add(o);
				}
				

				projectClassesMC.get(className).put(o[2].toString(), new ArrayList<Object[]>(colec));
			}
		}catch(NullPointerException npe){
			System.out.println("parou5");
		}
	}
	
	/*public void updateDependenciesBM(String className, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClassesBM.put(className, dependencies);
	}*/
	
	public void updateDependenciesBM(String className, Collection<Object[]> dependencies) {
		try{
		this.filterCommonDependencies(dependencies);
		Collection<Object[]> colec = new ArrayList<Object[]>();
		Map<String, HashMap<String,Collection<Object[]>>> tempMC = new HashMap<String, HashMap<String,Collection<Object[]>>>();
		Map<String, Collection<Object[]>> tempBM = new HashMap<String, Collection<Object[]>>();
		Collection<Object[]> listaBloco = new ArrayList<Object[]>();
		String methodName = "";
		String bloco = "";
		boolean added=false;
		//if(!projectClassesBM.containsKey(className)){
		projectClassesBM.put(className, new HashMap<String, HashMap<String,Collection<Object[]>>>());
	//}
		
		
		for(Object[] o : dependencies){
			
			methodName=o[2].toString();
			bloco= o[3].toString();
			
			if(projectClassesBM.get(className)==null){
				colec = new ArrayList<Object[]>();
				colec.add(o);
			}else if(projectClassesBM.get(className).get(methodName)==null){
				colec = new ArrayList<Object[]>();
				colec.add(o);
			}else if(projectClassesBM.get(className).get(methodName).get(bloco)==null){
				colec = new ArrayList<Object[]>();
				colec.add(o);
			
			}else{
				colec= new ArrayList<Object[]>(projectClassesBM.get(className).get(methodName).get(bloco));
				colec.add(o);
			}
			
			if(!projectClassesBM.get(className).containsKey(o[2].toString())){
				projectClassesBM.get(className).put(o[2].toString(), new HashMap<String, Collection<Object[]>>());
			}
			projectClassesBM.get(className).get(o[2].toString()).put(o[3].toString(),
						new ArrayList<Object[]>(colec));
		}
			/*
			if(methodName.equals(o[2].toString())){
				if(bloco.equals(o[3].toString())){
					listaBloco.add(o);
					methodName=o[2].toString();
					bloco= o[3].toString();
				}
				else{
					if(!bloco.equals("")){
						tempBM.put(bloco, new ArrayList<Object[]>(listaBloco));
					}
					listaBloco.clear();
					bloco= o[3].toString();
					methodName=o[2].toString();
					listaBloco.add(o);
				}
			}
			else{
				if(!methodName.equals("") && !bloco.equals("")){
					tempBM.put(bloco, new ArrayList<Object[]>(listaBloco));
					tempMC.put(methodName, new HashMap<String,Collection<Object[]>>(tempBM));
					tempBM = new HashMap<String, Collection<Object[]>>();
				}
				listaBloco.clear();
				methodName=o[2].toString();
				bloco= o[3].toString();
				listaBloco.add(o);
			}	
		}
		
		if(!methodName.equals("") && !bloco.equals("")){
		
			tempBM.put(bloco, new ArrayList<Object[]>(listaBloco));
			tempMC.put(methodName, new HashMap<String,Collection<Object[]>>(tempBM));
		}
		
		projectClassesBM.put(className, new HashMap<String, HashMap<String,Collection<Object[]>>>(tempMC));
		*/
		}catch(NullPointerException npe){
			System.out.println("parou6");
		}
	}
	
	/*public void updateDependenciesMC(String className, String methodName, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClassesMC.get(className).put(methodName, dependencies);
		projectClassesBM.get(className).put(methodName, null);
	}
	
	public void updateDependenciesBM(String className, String methodName, int bloco, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClassesBM.get(className).get(methodName).put(bloco, dependencies);
	}*/

	private void filterCommonDependencies(Collection<Object[]> dependencies) {
		try{
			String typesToDisregard[] = new String[] { "boolean", "char", "byte", "short", "int", "long", "float", "double",
		
				"java.lang.Boolean", /*"java.util.Vector", "java.util.Iterator", "java.lang.Class",*/ "java.lang.Character", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
				"java.lang.Float", "java.lang.Double", "java.lang.String", "java.lang.Object", "java.lang.Boolean[]", "java.lang.Boolean[][]",
				"java.lang.Character[]", "java.lang.Character[][]", "java.lang.Byte[]", "java.lang.Byte[][]", "java.lang.Short[]", 
				"java.lang.Short[][]", "java.lang.Integer[]", "java.lang.Integer[][]", "java.lang.Long[]", "java.lang.Long[][]",  
				"java.lang.Float[]", "java.lang.Float[][]", "java.lang.Double[]", "java.lang.Double[][]", "java.lang.String[]", "java.lang.String[][]",
				"java.lang.Object[]", "java.lang.Object[][]", "java.lang.Deprecated", "java.util.ArrayList", "java.util.ArrayList[]", "java.util.ArrayList[][]",
				"java.util.ArrayList<^[a-zA-Z]>", "java.util.ArrayList<^[a-zA-Z]>[]", "java.util.ArrayList<^.*>[][]",
				"java.util.ArrayList[]<.*>", "java.util.ArrayList[][]<.*>",
				"java.lang.SuppressWarnings", "java.lang.Override", "java.lang.SafeVarargs", "java.util.ArrayList<SQLData>", "java.util.ArrayList<String>", "java.util.ArrayList<JPanel>", "java.util.ArrayList<Long>", "java.util.ArrayList<Double>", "java.util.ArrayList<Integer>" };

			for (Iterator<Object[]> it = dependencies.iterator(); it.hasNext();) {
				Object[] o = it.next();
				if (contains(o[0], typesToDisregard)) {
					it.remove();
				}
			}
		}catch(NullPointerException npe){
			System.out.println("parou4");
		}

	}

	private boolean contains(Object value, Object[] array) {
		for (Object o : array) {
			if (o.equals(value) || value.toString().startsWith("java.util.ArrayList")) {
				return true;
			}
		}
		return false;
	}

	public Collection<Object[]> getUniverseOfDependencies() {
		
		Collection<Object[]> set = new HashSet<Object[]>();
		try{

		for (Collection<Object[]> col : projectClassesCP.values()) {
			set.addAll(col);
		}

		}catch(NullPointerException npe){
			System.out.println("parou7");
			return set;
		}
		return set;
		
	}
	
	/*public Collection<Object[]> getUniverseOfDependenciesMC() {
		Collection<Object[]> set = new HashSet<Object[]>();

		for (HashMap<String, Collection<Object[]>> tmap : projectClassesMC.values()) {
			
			for (Collection<Object[]> col : tmap.values()) {
				set.addAll(col);
			}
		}
		return set;
	}
	public Collection<Object[]> getUniverseOfDependenciesBM() {
		Collection<Object[]> set = new HashSet<Object[]>();

		for (Collection<Object[]> col : projectClassesBM.values()) {
			set.addAll(col);
		}

		return set;
	}*/

}
