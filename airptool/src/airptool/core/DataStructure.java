package airptool.core;

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

import airptool.util.AirpUtil;

public class DataStructure {
	/**
	 * String: class name Collection<Dependency>: Collection of established
	 * dependencies
	 */
	public Map<String, Collection<Object[]>> projectClassesCP = null;
	//public Map<String, Collection<Object[]>> projectClassesMC = null;
	//public Map<String, Collection<Object[]>> projectClassesBM = null;
	
	
	public Map<String, HashMap<String, Collection<Object[]>>> projectClassesMC = null;
	public Map<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> projectClassesBM = null;


	public DataStructure(IProject project) throws CoreException, ParseException {
		this.projectClassesCP = new HashMap<String, Collection<Object[]>>();
		
		this.projectClassesMC = new HashMap<String, HashMap<String, Collection<Object[]>>>();
		this.projectClassesBM = new HashMap<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>>();

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
			listaString.clear();
			 for(Map.Entry<String, Collection<Object[]>> entry2 : entry.getValue().entrySet()){
				 if(!listaString.contains(entry2.getKey())){
					 listaString.add(entry2.getKey());
				 }
			 }
			 result.put(entry.getKey(),new ArrayList<String>(listaString));
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

	public Map<String, Map<String, ArrayList<Integer>>> getProjectBlocks() {
		Map<String, Map<String, ArrayList<Integer>>> result = new HashMap<String, Map<String, ArrayList<Integer>>>();
		Map<String, ArrayList<Integer>> result2 = new HashMap<String, ArrayList<Integer>>();
		ArrayList<Integer> listaInteger = new ArrayList<Integer>();
		String methodName= "";
		String className= "";
		
		for (Map.Entry<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> entry : projectClassesBM.entrySet()){
			listaInteger.clear();
			result2.clear();
			 for(Map.Entry<String, HashMap<Integer, Collection<Object[]>>> entry2 : entry.getValue().entrySet()){
				 for(Map.Entry<Integer, Collection<Object[]>> entry3 : entry2.getValue().entrySet()){
					 if(methodName.equals(entry2.getKey())){
						 if(!listaInteger.contains(entry3.getKey())){
							 listaInteger.add(entry3.getKey());
						 }
					 }else{
						 listaInteger.clear();
						 if(result2.containsKey(entry2.getKey())){
							 listaInteger.addAll(result2.get(entry2.getKey()));
						 }
						 
						 if(!listaInteger.contains(entry3.getKey())){
							 listaInteger.add(entry3.getKey());
						 }
						 methodName = entry2.getKey();
						 className = entry.getKey();
					 }
					 result2.put(entry2.getKey(), new ArrayList<Integer>(listaInteger));
				 }
			 }
			 result.put(entry.getKey(),new HashMap<String, ArrayList<Integer>>(result2)); 
		}
		
		return result;
	}
	
	public Map<String, Collection<Object[]>> getAllDependenciesCP() {
		return projectClassesCP;
	}
	
	public Map<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> getAllDependenciesBM() {
		return projectClassesBM;
	}
	
	//public Map<String, Collection<Object[]>> getAllDependenciesMC() {
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
	
	public HashMap<Integer, Collection<Object[]>> getDependenciesBM(String className, String method) {
	//public Collection<Object[]> getDependenciesBM(String className) {
		for(Map.Entry<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> entry : projectClassesBM.entrySet()){
			for(Map.Entry<String, HashMap<Integer, Collection<Object[]>>> entry2 : entry.getValue().entrySet()){
				if(entry.getKey().equals(className) && entry2.getKey().equals(method)){
					return entry2.getValue();
				}
			}
		}
			return null;
	}
	
	public HashMap<String,HashMap<Integer, Collection<Object[]>>> getDependenciesBM(String className) {
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
		this.filterCommonDependencies(dependencies);
		Map<String, Collection<Object[]>> tempMC = new HashMap<String, Collection<Object[]>>();
		Collection<Object[]> listaMetodo = new ArrayList<Object[]>();
		String methodName = "";
		
		for(Object[] o : dependencies){
			if(methodName.equals(o[2].toString())){
				listaMetodo.add(o);
				methodName=o[2].toString();
			}
			else{
				if(!methodName.equals("")){
					tempMC.put(methodName, new ArrayList<Object[]>(listaMetodo));
				}
				listaMetodo.clear();
				methodName=o[2].toString();
				listaMetodo.add(o);
			}	
		}
		
		tempMC.put(methodName, new ArrayList<Object[]>(listaMetodo));
		
		projectClassesMC.put(className, new HashMap<String, Collection<Object[]>>(tempMC));
	}
	
	/*public void updateDependenciesBM(String className, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClassesBM.put(className, dependencies);
	}*/
	
	public void updateDependenciesBM(String className, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		Map<String, HashMap<Integer,Collection<Object[]>>> tempMC = new HashMap<String, HashMap<Integer,Collection<Object[]>>>();
		Map<Integer, Collection<Object[]>> tempBM = new HashMap<Integer, Collection<Object[]>>();
		Collection<Object[]> listaBloco = new ArrayList<Object[]>();
		String methodName = "";
		int bloco = -1;
		
		for(Object[] o : dependencies){
			if(methodName.equals(o[2].toString())){
				if(bloco == Integer.parseInt(o[3].toString())){
					listaBloco.add(o);
					methodName=o[2].toString();
					bloco= Integer.parseInt(o[3].toString());
				}
				else{
					if(bloco != -1){
						tempBM.put(bloco, new ArrayList<Object[]>(listaBloco));
					}
					listaBloco.clear();
					bloco= Integer.parseInt(o[3].toString());
					methodName=o[2].toString();
					listaBloco.add(o);
				}
			}
			else{
				if(!methodName.equals("") && bloco !=-1){
					tempBM.put(bloco, new ArrayList<Object[]>(listaBloco));
					tempMC.put(methodName, new HashMap<Integer,Collection<Object[]>>(tempBM));
					tempBM = new HashMap<Integer, Collection<Object[]>>();
				}
				listaBloco.clear();
				methodName=o[2].toString();
				bloco= Integer.parseInt(o[3].toString());
				listaBloco.add(o);
			}	
		}
		
		if(!methodName.equals("") && bloco !=-1){
		
			tempBM.put(bloco, new ArrayList<Object[]>(listaBloco));
			tempMC.put(methodName, new HashMap<Integer,Collection<Object[]>>(tempBM));
		}
		
		projectClassesBM.put(className, new HashMap<String, HashMap<Integer,Collection<Object[]>>>(tempMC));
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
		String typesToDisregard[] = new String[] { "boolean", "char", "byte", "short", "int", "long", "float", "double",
				"java.lang.Boolean", "java.lang.Character", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
				"java.lang.Float", "java.lang.Double", "java.lang.String", "java.lang.Object", "java.lang.Boolean[]", "java.lang.Boolean[][]",
				"java.lang.Character[]", "java.lang.Character[][]", "java.lang.Byte[]", "java.lang.Byte[][]", "java.lang.Short[]", 
				"java.lang.Short[][]", "java.lang.Integer[]", "java.lang.Integer[][]", "java.lang.Long[]", "java.lang.Long[][]",  
				"java.lang.Float[]", "java.lang.Float[][]", "java.lang.Double[]", "java.lang.Double[][]", "java.lang.String[]", "java.lang.String[][]",
				"java.lang.Object[]", "java.lang.Object[][]", "java.lang.Deprecated", "java.util.ArrayList", "java.util.ArrayList[]", "java.util.ArrayList[][]",
				"java.lang.SuppressWarnings", "java.lang.Override", "java.lang.SafeVarargs" };

		for (Iterator<Object[]> it = dependencies.iterator(); it.hasNext();) {
			Object[] o = it.next();
			if (contains(o[1], typesToDisregard)) {
				it.remove();
			}
		}

	}

	private boolean contains(Object value, Object[] array) {
		for (Object o : array) {
			if (o.equals(value)) {
				return true;
			}
		}
		return false;
	}

	public Collection<Object[]> getUniverseOfDependencies() {
		Collection<Object[]> set = new HashSet<Object[]>();

		for (Collection<Object[]> col : projectClassesCP.values()) {
			set.addAll(col);
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
