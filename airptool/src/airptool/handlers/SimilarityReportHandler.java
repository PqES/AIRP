package airptool.handlers;

import java.util.List;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import airptool.Activator;
import airptool.core.DataStructure;
import airptool.core.SuitableModule;
import airptool.enums.DependencyType;
import airptool.persistence.AirpPersistence;
import airptool.util.AirpUtil;
import airptool.util.CsvFileWriter;
import airptool.util.CsvFileWriterBM;
import airptool.util.CsvFileWriterMC;
import airptool.util.DataStructureUtils;
import airptool.util.DateUtil;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SimilarityReportHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public SimilarityReportHandler() {
		//TODO: verificar se precisa colocar algo no construtor (provavelmente sim),
		//mas na documentacao do site ta vazia
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 */

	public Object execute(ExecutionEvent event) throws ExecutionException {
		try {
			Shell shell = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

			ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new LabelProvider());
			dialog.setMultipleSelection(true);
			dialog.setEmptySelectionMessage("No project select. Airp has not been triggered.");

			//TODO: ver prq essas 2 linhas esto comentadas em baixo) 
			
			// dialog.setElements(new String[] { "[T]", "[dt, T]" });
			dialog.setTitle(AirpUtil.NOME_APLICACAO);
			dialog.setMessage("Which projects would you like to analyze? (it has to be opened and Airp enabled)");
			// dialog.setInitialSelections(new String[] { "[T]" });

			//TODO: acho que aqui ele ta pegando todos os projetos que etao abertos
			//ver se nao eh melhor mudar pro cara escolher
			//tem que ver se o fato disso ser um Handler, o que est fazendo est
			//certo e eu deveria mexer em outro lugar
			
			LinkedList<IProject> airpEnabledProjects = new LinkedList<IProject>();
			for (IProject project : root.getProjects()) {
				if (project.isOpen() && AirpUtil.isAirpEnabled(project)) {
					airpEnabledProjects.add(project);
				}
			}
			dialog.setElements(airpEnabledProjects.toArray());

			// User pressed cancel
			if (dialog.open() != Window.OK) {
				return null;
			}

			Object[] selectedProjects = (Object[]) dialog.getResult();

			for (Object o : selectedProjects) {
				IProject project = (IProject) o;
				IJavaProject javaProject = JavaCore.create(project);
				DataStructure ds = this.init(project);

				final Map<String, HashMap<String, Collection<Object[]>>> packagesDependenciesOriginal = AirpUtil.getPackagesDependencies(ds);
				final Map<String, HashMap<String, Collection<Object[]>>> classDependenciesOriginal = ds.getAllDependenciesMC();
				final Map<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> methodDependenciesOriginal = ds.getAllDependenciesBM();
				
				final Collection<Object[]> universeOfDependenciesOriginal = ds.getUniverseOfDependencies();
				// long inicioGeral = System.currentTimeMillis();
				
				//TODO: ver prq a linha acima est comentada)
				//parece que ele tava usando soh pra calcular o tempo que demora pra processar

				/*this.calculate(project, javaProject, ds, packagesDependenciesOriginal, universeOfDependenciesOriginal, new TypeFunction(),
						false, "List_{T}");
				System.gc();
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, universeOfDependenciesOriginal,
						new DependencyAndTypeFunction(), false, "List_{dp,T}");
				System.gc();
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, classDependenciesOriginal, methodDependenciesOriginal, universeOfDependenciesOriginal, new TypeFunction(),
						true, "Set_{T}");
				System.gc();
				*/
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, classDependenciesOriginal, methodDependenciesOriginal, universeOfDependenciesOriginal,
						new TypeFunction(), true, "Set_{T}");
				System.gc();
				
				//TODO: era soh pra calcular o tempo mesmo

				// System.out.printf("[%s] Total time: %.3f seconds.\n",
				// project.getName(),
				// (System.currentTimeMillis() - inicioGeral) / 1000.0);
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	private void calculate(final IProject project, final IJavaProject javaProject, final DataStructure ds,
			final Map<String, HashMap<String, Collection<Object[]>>> packagesDependenciesOriginal, final Map<String, HashMap<String, Collection<Object[]>>> classDependenciesOriginal, final  Map<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> methodDependenciesOriginal,
			final Collection<Object[]> universeOfDependenciesOriginal, final Function<Object[], String> function, final boolean set,
			final String identifier) throws FileNotFoundException, JavaModelException, CoreException {

			Collection<String> classes = ds.getProjectClasses();
			Map<String, ArrayList<String>> methods = ds.getProjectMethods();
			Map<String, Map<String, ArrayList<Integer>>> blocos = ds.getProjectBlocks();
			
			// Change the packages
			Map<String, HashMap<String, Collection<? extends Object>>> packagesDependencies = new HashMap<String, HashMap<String, Collection<? extends Object>>>();
			Map<String, HashMap<String, Collection<? extends Object>>> classDependencies = new HashMap<String, HashMap<String, Collection<? extends Object>>>();
			Map<String, HashMap<String, HashMap<Integer,Collection<? extends Object>>>> methodDependencies = new HashMap<String, HashMap<String, HashMap<Integer, Collection<? extends Object>>>>();
			
			
			for (Map.Entry<String, HashMap<String, Collection<Object[]>>> entry : packagesDependenciesOriginal.entrySet()) {
				if(!entry.getKey().toLowerCase().contains("test")){
					for(Map.Entry<String, Collection<Object[]>> entry2 : entry.getValue().entrySet()){
						if(!entry2.getKey().toLowerCase().contains("test")){
							if (set) {
								if(!packagesDependencies.containsKey(entry.getKey())){
									packagesDependencies.put(entry.getKey(), new HashMap<String, Collection<? extends Object>>());
								}
								packagesDependencies.get(entry.getKey()).put(entry2.getKey(), 
										new HashSet<Object>(Collections2.transform(packagesDependenciesOriginal.get(entry.getKey()).get(entry2.getKey()), function)));
							} else {
								if(!packagesDependencies.containsKey(entry.getKey())){
									packagesDependencies.put(entry.getKey(), new HashMap<String, Collection<? extends Object>>());
								}
								packagesDependencies.get(entry.getKey()).put(entry2.getKey(), 
										Collections2.transform(packagesDependenciesOriginal.get(entry.getKey()).get(entry2.getKey()), function));
							}
						}
					}
				}
			}
			
			
			for(Map.Entry<String, HashMap<String, HashMap<Integer, Collection<Object[]>>>> entry : methodDependenciesOriginal.entrySet()){
				//HashMap<String, Collection<? extends Object>> tempMC = new HashMap<String, Collection<? extends Object>>();
				if(!entry.getKey().toLowerCase().contains("test")){
					for(Map.Entry<String, HashMap<Integer, Collection<Object[]>>> entry2 : entry.getValue().entrySet()){
						if(!entry.getKey().endsWith(entry2.getKey().toLowerCase())){
							for(Map.Entry<Integer, Collection<Object[]>> entry3 : entry2.getValue().entrySet()){
								if (set) {
									if(!methodDependencies.containsKey(entry.getKey())){
										methodDependencies.put(entry.getKey(), new HashMap<String, HashMap<Integer, Collection<? extends Object>>>());
									}
									if(!methodDependencies.get(entry.getKey()).containsKey(entry2.getKey())){
										methodDependencies.get(entry.getKey()).put(entry2.getKey(), new HashMap<Integer, Collection<? extends Object>>());
									}
									methodDependencies.get(entry.getKey()).get(entry2.getKey()).put(entry3.getKey(),
												new HashSet<Object>(Collections2.transform(entry3.getValue(), function)));
								} else {
									if(!methodDependencies.containsKey(entry.getKey())){
										methodDependencies.put(entry.getKey(), new HashMap<String, HashMap<Integer, Collection<? extends Object>>>());
									}
									if(!methodDependencies.get(entry.getKey()).containsKey(entry2.getKey())){
										methodDependencies.get(entry.getKey()).put(entry2.getKey(), new HashMap<Integer, Collection<? extends Object>>());
									}
									methodDependencies.get(entry.getKey()).get(entry2.getKey()).put(entry3.getKey(),
											Collections2.transform(entry3.getValue(), function));
								}
							}
						}
					}
				}
			}

			// Change the universe
			Collection<? extends Object> universeOfDependencies = Collections2.transform(universeOfDependenciesOriginal, function);
			
			for (Map.Entry<String, Map<String, ArrayList<Integer>>> entry : blocos.entrySet()){
				String expectedClass = entry.getKey();
				for (Map.Entry<String, ArrayList<Integer>> entry2 : entry.getValue().entrySet()){
					String expectedMethod = entry2.getKey();
					Map<Integer, Collection<Object[]>> depBM = ds.getDependenciesBM(expectedClass, expectedMethod);
					for(Map.Entry<Integer, Collection<Object[]>> entryBM : depBM.entrySet()){
					
					String pkg = AirpUtil.getPackageFromClassName(expectedClass);
					String pkgclass = pkg+"."+expectedClass;
					
					if (pkgclass.toLowerCase().contains("test")) {
						//outLog.println("ignored (test file).");
						//salvaBM=false;
						continue;
					}
					
					if (entryBM.getValue().isEmpty()) {
						//outLog.println("ignored (there are no method dependencies).");
						//salvaBM=false;
						continue;
					}

					if (!AirpUtil.moreThanNDependencies(entryBM.getValue(), 3)) {
						//outLog.println("ignored (uses less than 5 types).");
						//salvaBM=false;
						continue;
					}
					
					if (AirpUtil.isAloneInItsMethod(blocos, expectedMethod, expectedClass)) {
						//outLog.println("ignored (lonely).");
						//salvaBM=false;
						continue;
					}
					
				
					Collection<Object[]> dependenciesBlockUnderAnalysisOriginal = entryBM.getValue();
					
					
					//Map<String, Collection<Object[]>> methodDependenciesOriginal2 = new HashMap<String, Collection<Object[]>>();
					//Collection<Object[]> objMethod= new ArrayList<Object[]>();;
					
					// Disregard dependencies of the own class
					/*for(Object[] depMethods : methodDependenciesOriginal.get(expectedClass).get(expectedMethod)){
						if(depMethods[3].equals(expectedClass) && depMethods[2].equals(expectedMethod)){
							objMethod.add(depMethods);
						}
					}
						methodDependenciesOriginal2.put(expectedMethod, objMethod);
					*/	
					
					String methodName = "";
					String className = "";
					boolean stop=false;
					
					for(Object[] depMethod : dependenciesBlockUnderAnalysisOriginal){
						methodName= depMethod[2].toString();
						className= depMethod[4].toString();
					}
					
					if(!methodName.equals("") && !className.equals("")){ 
						try{
							/*Collection<Object[]> dependenciesBlockUnderAnalysisOriginal2 = new ArrayList<Object[]>();
							
							for(Object[] o : dependenciesBlockUnderAnalysisOriginal){
							
								List<Object> list = new ArrayList<Object>(Arrays.asList(o));
								o = list.toArray(o);
								
								Object[] o2 = new Object[4];
								o2[0]=o[0];
								o2[1]=o[1];
								o2[2]=o[2];
								o2[3]=o[4];
								
								dependenciesBlockUnderAnalysisOriginal2.add(o2);
							}
							Collection<Object[]> x = methodDependenciesOriginal.get(pkg).get(className).get(methodName);
							//methodDependenciesOriginal.get(className).get(methodName).removeAll(dependenciesBlockUnderAnalysisOriginal2);
							
							Collection<Object[]> objectsToRemove = new ArrayList<Object[]>();
							
							for(Object[] o: dependenciesBlockUnderAnalysisOriginal2){
								boolean added =false;
								for(Object[] o2: methodDependenciesOriginal.get(pkg).get(className).get(methodName)){
									String s= "";
									String s2= "";
									
									for(Object o3: o){
										s = s+o3.toString();
									}
									for(Object o3: o2){
										s2 = s2+o3.toString();
									}
	
									if(s.equals(s2)){
										if(!added){
											objectsToRemove.add(o2);
											added=true;
										}
									}
								}
							}
							*/
							methodDependencies.get(className).get(methodName).remove(entryBM.getKey());
							
							//methodDependencies.get(pkg).get(className).put(methodName,
							//		new HashSet<Object>(Collections2.transform(methodDependenciesOriginal.get(className).get(methodName), function)));
							
						}
						catch(NullPointerException npe){
							stop=true;
							System.out.println("parou");
						}
					}
						
					if(!stop){
						//methodDependenciesOriginal2.get(expectedClass).removeAll(dependenciesBlockUnderAnalysisOriginal2);
						
						Collection<? extends Object> dependenciesBlockUnderAnalysis = Collections2.transform(
								dependenciesBlockUnderAnalysisOriginal, function);
						
						if (!set) {
							
							StringBuilder s= SuitableModule.calculateAllBM(ds, expectedClass, entryBM.getKey(), expectedMethod, dependenciesBlockUnderAnalysis,
									methodDependencies, universeOfDependencies);
					
							
						} else {
							// It is not linked with the original list (because HashSet)
						/*	HashMap<String, Collection<? extends Object>> tempMC = new HashMap<String, Collection<? extends Object>>();
							tempMC.put(expectedMethod, new HashSet<Object>(Collections2.transform(methodDependenciesOriginal2.get(expectedClass), function)));
	
							methodDependencies.put(expectedClass, new HashMap<String, Collection<? extends Object>>(tempMC));
						
						*/	
							
							StringBuilder s= SuitableModule.calculateAllBM(ds, expectedClass, entryBM.getKey(), expectedMethod, new HashSet<Object>(
									dependenciesBlockUnderAnalysis), methodDependencies, new HashSet<Object>(universeOfDependencies));
						
						}
					

					// Put it back after
					
						methodDependencies.get(className).get(methodName).put(entryBM.getKey(), entryBM.getValue());
				
					}
					
					/*methodDependencies = new HashMap<String, HashMap<String, Collection<? extends Object>>>(methodDependencies2);
					
					String methodName2 = "";
					String className2 = "";
					
					for(Object[] depMethod1 : dependenciesBlockUnderAnalysisOriginal2){
						methodName2= depMethod[2].toString();
						className2= depMethod[4].toString();
					}
					
					for(Object[] depMethod2 : methodDependencies.get(className2).get(methodName2).toArray()){
						methodName2= depMethod[2].toString();
						className2= depMethod[4].toString();
					}
					
					
					if(!methodName2.equals("") && !className2.equals("")){
						//methodDependencies.get(className2).get(methodName2).
						
						methodDependencies.get(className2).get(methodName2).addAll((Collection<? extends Object>) dependenciesBlockUnderAnalysisOriginal2);
					}*/
					
					/*if (set) {
						// Restore what it was
						
						HashMap<String, Collection<? extends Object>> tempMC = new HashMap<String, Collection<? extends Object>>();
								tempMC.put(expectedMethod, new HashSet<Object>(Collections2.transform(methodDependenciesOriginal2.get(expectedClass), function)));

								methodDependencies.put(expectedClass, new HashMap<String, Collection<? extends Object>>(tempMC));
							
					}*/
				}	
			}
		}
			
			for (Map.Entry<String, HashMap<String, Collection<Object[]>>> entry : classDependenciesOriginal.entrySet()) {
				if(!entry.getKey().toLowerCase().contains("test")){
					
					for(Map.Entry<String, Collection<Object[]>> entry2 : entry.getValue().entrySet()){
						if(!entry.getKey().endsWith(entry2.getKey().toLowerCase())){
							if (set) {
								if(!classDependencies.containsKey(entry.getKey())){
									classDependencies.put(entry.getKey(), new HashMap<String, Collection<? extends Object>>());
								}
								
								classDependencies.get(entry.getKey()).put(entry2.getKey(),
									new HashSet<Object>(Collections2.transform(classDependenciesOriginal.get(entry.getKey()).get(entry2.getKey()), function)));
							} else {
								if(!classDependencies.containsKey(entry.getKey())){
									classDependencies.put(entry.getKey(), new HashMap<String, Collection<? extends Object>>());
								}
								
								classDependencies.get(entry.getKey()).put(entry2.getKey(), Collections2.transform(classDependenciesOriginal.get(entry.getKey()).get(entry2.getKey()), function));
							}
						}
					}
				}
			}
			
			
			for (Map.Entry<String, ArrayList<String>> entry : methods.entrySet()){
				String expectedClass = entry.getKey();
				for(Map.Entry<String, Collection<Object[]>> entryMC : ds.getDependenciesMC(expectedClass).entrySet()){
					
					String pkg = AirpUtil.getPackageFromClassName(expectedClass);
					String pkgclass = pkg+"."+expectedClass;
					
					if (pkgclass.toLowerCase().contains("test")) {
						//outLog.println("ignored (test file).");
						//salvaMC=false;
						continue;
					}
					
					if (entryMC.getValue().isEmpty()) {
						//outLog.println("ignored (there are no method dependencies).");
						//salvaMC=false;
						continue;
					}
	
					
					if (!AirpUtil.moreThanNDependencies(entryMC.getValue(), 3)) {
							//outLog.println("ignored (uses less than 3 types).");
							//salvaMC=false;
							continue;
						}
					
					if (AirpUtil.isAloneInItsClass(methods, expectedClass)) {
						//outLog.println("ignored (lonely).");
						//salvaMC=false;
						continue;
					}
					
					Collection<Object[]> dependenciesMethodUnderAnalysisOriginal = entryMC.getValue();
					
					/*Map<String, Collection<Object[]>> classDependenciesOriginal2 = new HashMap<String, Collection<Object[]>>();
					Collection<Object[]> objClass= new ArrayList<Object[]>();;
					
					// Disregard dependencies of the own class
					for(Object[] depClasses : classDependenciesOriginal.get(expectedClass)){
							objClass.add(depClasses);
					}
						classDependenciesOriginal2.put(expectedClass, objClass);
					*/
					
					//classDependenciesOriginal2.get(expectedClass).removeAll(dependenciesMethodUnderAnalysisOriginal2);
					String className = "";
					String methodName = "";
					boolean stop=false;
					
					for(Object[] depMethod : dependenciesMethodUnderAnalysisOriginal){
						className= depMethod[3].toString();
					}
					
					if(!className.equals("")){ 
						try{
							/*Collection<Object[]> dependenciesMethodUnderAnalysisOriginal2 = new ArrayList<Object[]>();
							
							for(Object[] o : dependenciesMethodUnderAnalysisOriginal){
							
								ArrayList<Object> list = new ArrayList<Object>(Arrays.asList(o));
								o = list.toArray(o);
								
								Object[] o2 = new Object[2];
								o2[0]=o[0];
								o2[1]=o[1];
									
								
								dependenciesMethodUnderAnalysisOriginal2.add(o2);
							}
							Collection<Object[]> objectsToRemove = new ArrayList<Object[]>();
							for(Object[] o: dependenciesMethodUnderAnalysisOriginal2){
								boolean added =false;
								for(Object[] o2: classDependenciesOriginal.get(className)){
									String s= "";
									String s2= "";
									
									for(Object o3: o){
										s = s+o3.toString();
									}
									for(Object o3: o2){
										s2 = s2+o3.toString();
									}
	
									if(s.equals(s2)){
										if(!added){
											objectsToRemove.add(o2);
											added=true;
										}
									}
								}
							}*/
							classDependencies.get(className).remove(entryMC.getKey());
						}
						catch(NullPointerException npe){
							stop=true;
						}
					}
					
					if(!stop){
					
						Collection<? extends Object> dependenciesMethodUnderAnalysis = Collections2.transform(
								dependenciesMethodUnderAnalysisOriginal, function);
						
						if (!set) {
							
							StringBuilder b = SuitableModule.calculateAllMC(ds, entryMC.getKey(), expectedClass, dependenciesMethodUnderAnalysis,
									classDependencies, universeOfDependencies);
							
						} else {
							// It is not linked with the original list (because HashSet)
							
							StringBuilder b = SuitableModule.calculateAllMC(ds, entryMC.getKey(), expectedClass, new HashSet<Object>(
									dependenciesMethodUnderAnalysis), classDependencies, new HashSet<Object>(universeOfDependencies));
						}
		
						// Put it back after
						//classDependenciesOriginal2.get(expectedClass).addAll(dependenciesMethodUnderAnalysisOriginal2);
						
						classDependencies.get(className).put(entryMC.getKey(), entryMC.getValue());
					}
				}
			}
			
			for (String classUnderAnalysis : classes) {
				
				String expectedModule = AirpUtil.getPackageFromClassName(classUnderAnalysis);

				String pkgclass = expectedModule+"."+classUnderAnalysis;
				
				if (pkgclass.toLowerCase().contains("test")) {
					//outLog.println("ignored (test file).");
					//salvaCP=false;
					continue;
				}

				
				// It may be redundant because the next if statement
				if (ds.getDependenciesCP(classUnderAnalysis).isEmpty()) {
					//outLog.println("ignored (there are no class dependencies).");
					//salvaCP=false;
					continue;
				}

				if (!AirpUtil.moreThanNDependencies(ds.getDependenciesCP(classUnderAnalysis), 3)) {
					//outLog.println("ignored (uses less than 3 types).");
					//salvaCP=false;
					continue;
				}
				
				if (AirpUtil.isAloneInItsPackage(javaProject, classUnderAnalysis)) {
					//outLog.println("ignored (lonely).");
					//salvaCP=false;
					continue;
				}

				

				Collection<Object[]> dependenciesClassUnderAnalysisOriginal = ds.getDependenciesCP(classUnderAnalysis);	

				// Disregard dependencies of the own class
				packagesDependencies.get(expectedModule).remove(classUnderAnalysis);
				
				
				Collection<? extends Object> dependenciesClassUnderAnalysis = Collections2.transform(
						dependenciesClassUnderAnalysisOriginal, function);
				
				if (!set) {
					
					StringBuilder s=SuitableModule.calculateAll(ds, classUnderAnalysis, expectedModule, dependenciesClassUnderAnalysis,
							packagesDependencies, universeOfDependencies);
					
				} else {
					// It is not linked with the original list (because HashSet)
					StringBuilder s=SuitableModule.calculateAll(ds, classUnderAnalysis, expectedModule, new HashSet<Object>(
							dependenciesClassUnderAnalysis), packagesDependencies, new HashSet<Object>(universeOfDependencies));
				}

				// Put it back after
				packagesDependencies.get(expectedModule).put(classUnderAnalysis, ds.getDependenciesCP(classUnderAnalysis));

				/*if (set) {
					// Restore what it was

						packagesDependencies.put(expectedModule,
								new HashSet<Object>(Collections2.transform(packagesDependenciesOriginal.get(expectedModule), function)));
				}*/

			}
	}

	/**
	 * Method responsible for getting the architecture and the initialization of
	 * the dependencies (if they have not been initialized yet)
	 */
	private DataStructure init(IProject project) throws CoreException, IOException, ClassNotFoundException, ParseException {
		final DataStructure ds = DataStructureUtils.getOrInitializeDataStructure(project);

		for (String s : ds.getProjectClasses()) {
			if (ds.getDependenciesCP(s) == null || ds.getDependenciesMC(s) == null || ds.getDependenciesBM(s) == null) {
				for (String className : ds.getProjectClasses()) {
					Collection<Object[]> dependenciesCP = AirpPersistence.load(project, className+"CP");
					Collection<Object[]> dependenciesMC = AirpPersistence.load(project, className+"MC");
					Collection<Object[]> dependenciesBM = AirpPersistence.load(project, className+"BM");
					if (dependenciesCP == null || dependenciesMC == null || dependenciesBM == null) {
						throw new CoreException(null);
					}
					ds.updateDependenciesCP(className, dependenciesCP);
					ds.updateDependenciesMC(className, dependenciesMC);
					ds.updateDependenciesBM(className, dependenciesBM);
				}
			}
			break;
		}

		return ds;
	}
}

class TypeFunction implements Function<Object[], String> {

	public String apply(Object[] input) {
		return (String) input[1];
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}

class DependencyAndTypeFunction implements Function<Object[], String> {

	public String apply(Object[] input) {
		return input[0] + ";" + input[1];
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

}