package airptool.handlers;

import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import airptool.Activator;
import airptool.core.DataStructure;
import airptool.core.SuitableModule;
import airptool.enums.DependencyType;
import airptool.jung.NewWindow;
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
	
	public static ArrayList<DadosView> recTabMC = new ArrayList<DadosView>();
	public static ArrayList<DadosView> recTabMM = new ArrayList<DadosView>();
	public static ArrayList<DadosView> recTabEM = new ArrayList<DadosView>();
	
	/**
	 * The constructor.
	 */
	public SimilarityReportHandler() {
		// TODO: verificar se precisa colocar algo no construtor (provavelmente
		// sim),
		// mas na documentacao do site ta vazia
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

			// TODO: ver prq essas 2 linhas esto comentadas em baixo)

			// dialog.setElements(new String[] { "[T]", "[dt, T]" });
			dialog.setTitle(AirpUtil.NOME_APLICACAO);
			dialog.setMessage("Which projects would you like to analyze? (it has to be opened and Airp enabled)");
			// dialog.setInitialSelections(new String[] { "[T]" });

			// TODO: acho que aqui ele ta pegando todos os projetos que etao
			// abertos
			// ver se nao eh melhor mudar pro cara escolher
			// tem que ver se o fato disso ser um Handler, o que est fazendo est
			// certo e eu deveria mexer em outro lugar

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
				
				boolean existe = false;
				
				File f = new File(AirpUtil.TEMP_FOLDER+"/"+project.getName()+"BM.csv");
				if(f.exists() && !f.isDirectory()) { 
				    f = new File(AirpUtil.TEMP_FOLDER+"/"+project.getName()+"MC.csv");
				    if(f.exists() && !f.isDirectory()) {
				    	f = new File(AirpUtil.TEMP_FOLDER+"/"+project.getName()+"CP.csv");
					    if(f.exists() && !f.isDirectory()) {
					    	existe=true;
					    }
				    }
				}
				
				if(!existe){
				
				IJavaProject javaProject = JavaCore.create(project);
				DataStructure ds = this.init(project);

				final Map<String, HashMap<String, Collection<Object[]>>> packagesDependenciesOriginal = AirpUtil
						.getPackagesDependencies(ds);
				final Map<String, HashMap<String, Collection<Object[]>>> classDependenciesOriginal = ds
						.getAllDependenciesMC();
				final Map<String, HashMap<String, HashMap<String, Collection<Object[]>>>> methodDependenciesOriginal = ds
						.getAllDependenciesBM();

				Collection<Object[]> universeOfDependenciesOriginal = ds.getUniverseOfDependencies();

				/*
				 * this.calculate(project, javaProject, ds,
				 * packagesDependenciesOriginal, universeOfDependenciesOriginal,
				 * new TypeFunction(), false, "List_{T}"); System.gc();
				 * this.calculate(project, javaProject, ds,
				 * packagesDependenciesOriginal, universeOfDependenciesOriginal,
				 * new DependencyAndTypeFunction(), false, "List_{dp,T}");
				 * System.gc(); this.calculate(project, javaProject, ds,
				 * packagesDependenciesOriginal, classDependenciesOriginal,
				 * methodDependenciesOriginal, universeOfDependenciesOriginal,
				 * new TypeFunction(), true, "Set_{T}"); System.gc();
				 */
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, classDependenciesOriginal,
						methodDependenciesOriginal, universeOfDependenciesOriginal, new TypeFunction(), true,
						"Set_{T}");
				System.gc();
				}
				
				CalcRec cr = new CalcRec();
				HashSet<DadosView> tempTab = cr.getTab();
				
				for(DadosView dv : tempTab){
					if(dv.getTipo().equals("MC")){
						recTabMC.add(dv);
					}
					else if(dv.getTipo().equals("MM")){
						recTabMM.add(dv);
					}
					else{
						recTabEM.add(dv);
					}
				}
				
				Collections.sort(recTabMC, new Comparator() {

			        public int compare(Object o1, Object o2) {

			            String x1 = ((DadosView) o1).getPkg_ori();
			            String x2 = ((DadosView) o2).getPkg_ori();
			            int comp1 = x1.compareTo(x2);

			            if (comp1 != 0) {
			               return comp1;
			            } else {
			            	x1 = ((DadosView) o1).getClss_ori();
				            x2 = ((DadosView) o2).getClss_ori();
				            int comp2 = x1.compareTo(x2);

				            if (comp2 != 0) {
				               return comp2;
				            } else {
				               Double d1 = ((DadosView) o1).getFx();
				               Double d2 = ((DadosView) o2).getFx();
				               return d1.compareTo(d2);
				            }
			            }
			    }});
				
				Collections.sort(recTabMM, new Comparator() {

			        public int compare(Object o1, Object o2) {

			            String x1 = ((DadosView) o1).getPkg_ori();
			            String x2 = ((DadosView) o2).getPkg_ori();
			            int comp1 = x1.compareTo(x2);

			            if (comp1 != 0) {
			               return comp1;
			            } else {
			            	x1 = ((DadosView) o1).getClss_ori();
				            x2 = ((DadosView) o2).getClss_ori();
				            int comp2 = x1.compareTo(x2);

				            if (comp2 != 0) {
				               return comp2;
				            } else {
				            	x1 = ((DadosView) o1).getMet_ori();
					            x2 = ((DadosView) o2).getMet_ori();
					            int comp3 = x1.compareTo(x2);

					            if (comp3 != 0) {
					               return comp3;
					            } else {
					               Double d1 = ((DadosView) o1).getFx();
					               Double d2 = ((DadosView) o2).getFx();
					               return d1.compareTo(d2);
					            }
				            }
			            }
			    }});
				
				Collections.sort(recTabEM, new Comparator() {

			        public int compare(Object o1, Object o2) {

			            String x1 = ((DadosView) o1).getPkg_ori();
			            String x2 = ((DadosView) o2).getPkg_ori();
			            int comp1 = x1.compareTo(x2);

			            if (comp1 != 0) {
			               return comp1;
			            } else {
			            	x1 = ((DadosView) o1).getClss_ori();
				            x2 = ((DadosView) o2).getClss_ori();
				            int comp2 = x1.compareTo(x2);

				            if (comp2 != 0) {
				               return comp2;
				            } else {
				            	x1 = ((DadosView) o1).getMet_ori();
					            x2 = ((DadosView) o2).getMet_ori();
					            int comp3 = x1.compareTo(x2);

					            if (comp3 != 0) {
					               return comp3;
					            } else {
					            	x1 = ((DadosView) o1).getBlo_ori();
						            x2 = ((DadosView) o2).getBlo_ori();
						            int comp4 = x1.compareTo(x2);

						            if (comp4 != 0) {
						               return comp4;
						            } else {
						               Double d1 = ((DadosView) o1).getFx();
						               Double d2 = ((DadosView) o2).getFx();
						               return d1.compareTo(d2);
						            }
					            }
				            }
			            }
			    }});
				
				openView();
				NewWindow nw = new NewWindow();
				
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private void calculate(final IProject project, final IJavaProject javaProject, final DataStructure ds,
			final Map<String, HashMap<String, Collection<Object[]>>> packagesDependenciesOriginal,
			final Map<String, HashMap<String, Collection<Object[]>>> classDependenciesOriginal,
			final Map<String, HashMap<String, HashMap<String, Collection<Object[]>>>> methodDependenciesOriginal,
			final Collection<Object[]> universeOfDependenciesOriginal, final Function<Object[], String> function,
			final boolean set, final String identifier) throws FileNotFoundException, JavaModelException, CoreException,
			MalformedURLException, ClassNotFoundException {

		Collection<String> classes = ds.getProjectClasses();
		Map<String, ArrayList<String>> methods = ds.getProjectMethods();
		Map<String, Map<String, ArrayList<String>>> blocos = ds.getProjectBlocks();

		// Change the packages
		Map<String, HashMap<String, Collection<? extends Object>>> packagesDependencies = new HashMap<String, HashMap<String, Collection<? extends Object>>>();
		Map<String, HashMap<String, Collection<? extends Object>>> classDependencies = new HashMap<String, HashMap<String, Collection<? extends Object>>>();
		Map<String, HashMap<String, HashMap<String, Collection<? extends Object>>>> methodDependencies = new HashMap<String, HashMap<String, HashMap<String, Collection<? extends Object>>>>();

		for (Map.Entry<String, HashMap<String, Collection<Object[]>>> entry : packagesDependenciesOriginal.entrySet()) {
			try {
				if (!entry.getKey().toLowerCase().contains("test")) {
					for (Map.Entry<String, Collection<Object[]>> entry2 : entry.getValue().entrySet()) {
						try {
							if (!entry2.getKey().toLowerCase().contains("test")) {
								if (set) {
									if (!packagesDependenciesOriginal.get(entry.getKey()).get(entry2.getKey())
											.isEmpty()) {
										if (!packagesDependencies.containsKey(entry.getKey())) {
											packagesDependencies.put(entry.getKey(),
													new HashMap<String, Collection<? extends Object>>());
										}
										packagesDependencies.get(entry.getKey()).put(entry2.getKey(),
												new HashSet<Object>(Collections2.transform(packagesDependenciesOriginal
														.get(entry.getKey()).get(entry2.getKey()), function)));
									}
								} else {
									if (!packagesDependenciesOriginal.get(entry.getKey()).get(entry2.getKey())
											.isEmpty()) {
										if (!packagesDependencies.containsKey(entry.getKey())) {
											packagesDependencies.put(entry.getKey(),
													new HashMap<String, Collection<? extends Object>>());
										}
										packagesDependencies.get(entry.getKey()).put(entry2.getKey(),
												Collections2.transform(packagesDependenciesOriginal.get(entry.getKey())
														.get(entry2.getKey()), function));
									}
								}
							}
						} catch (NullPointerException npe) {
							System.out.println("Paoru_geral1");
						}
					}
				}
			} catch (NullPointerException npe) {
				System.out.println("Paoru_geral2");
			}
		}

		for (Map.Entry<String, HashMap<String, HashMap<String, Collection<Object[]>>>> entry : methodDependenciesOriginal
				.entrySet()) {
			try {
				// HashMap<String, Collection<? extends Object>> tempMC = new
				// HashMap<String, Collection<? extends Object>>();
				if (!entry.getKey().toLowerCase().contains("test")) {
					for (Map.Entry<String, HashMap<String, Collection<Object[]>>> entry2 : entry.getValue()
							.entrySet()) {
						try {
							if (!entry.getKey().endsWith(entry2.getKey().toLowerCase())) {
								for (Map.Entry<String, Collection<Object[]>> entry3 : entry2.getValue().entrySet()) {
									if (!entry3.getValue().isEmpty()) {
										if (set) {

											if (!methodDependencies.containsKey(entry.getKey())) {
												methodDependencies.put(entry.getKey(),
														new HashMap<String, HashMap<String, Collection<? extends Object>>>());
											}
											if (!methodDependencies.get(entry.getKey()).containsKey(entry2.getKey())) {
												methodDependencies.get(entry.getKey()).put(entry2.getKey(),
														new HashMap<String, Collection<? extends Object>>());
											}
											methodDependencies.get(entry.getKey()).get(entry2.getKey())
													.put(entry3.getKey(), new HashSet<Object>(
															Collections2.transform(entry3.getValue(), function)));

										} else {

											if (!methodDependencies.containsKey(entry.getKey())) {
												methodDependencies.put(entry.getKey(),
														new HashMap<String, HashMap<String, Collection<? extends Object>>>());
											}
											if (!methodDependencies.get(entry.getKey()).containsKey(entry2.getKey())) {
												methodDependencies.get(entry.getKey()).put(entry2.getKey(),
														new HashMap<String, Collection<? extends Object>>());
											}
											methodDependencies.get(entry.getKey()).get(entry2.getKey()).put(
													entry3.getKey(),
													Collections2.transform(entry3.getValue(), function));

										}
									}
								}
							}
						} catch (NullPointerException npe) {
							System.out.println("Paoru_geral3");
						}
					}
				}
			} catch (NullPointerException npe) {
				System.out.println("Paoru_geral4");
			}
		}

		for (Map.Entry<String, HashMap<String, Collection<Object[]>>> entry : classDependenciesOriginal.entrySet()) {
			try {
				if (!entry.getKey().toLowerCase().contains("test")) {

					for (Map.Entry<String, Collection<Object[]>> entry2 : entry.getValue().entrySet()) {
						try {
							if (!entry.getKey().toLowerCase().endsWith(entry2.getKey().toLowerCase())) {
								if (!classDependenciesOriginal.get(entry.getKey()).get(entry2.getKey()).isEmpty()) {
									if (set) {
										if (!classDependencies.containsKey(entry.getKey())) {
											classDependencies.put(entry.getKey(),
													new HashMap<String, Collection<? extends Object>>());
										}

										classDependencies.get(entry.getKey()).put(entry2.getKey(),
												new HashSet<Object>(Collections2.transform(classDependenciesOriginal
														.get(entry.getKey()).get(entry2.getKey()), function)));

									} else {
										if (!classDependencies.containsKey(entry.getKey())) {
											classDependencies.put(entry.getKey(),
													new HashMap<String, Collection<? extends Object>>());
										}

										classDependencies.get(entry.getKey()).put(entry2.getKey(),
												Collections2.transform(classDependenciesOriginal.get(entry.getKey())
														.get(entry2.getKey()), function));
									}

								}
							}
						} catch (NullPointerException npe) {
							System.out.println("Paoru_geral5");
						}
					}
				}
			} catch (NullPointerException npe) {
				System.out.println("Paoru_geral6");
			}
		}

		// Change the universe
		Collection<? extends Object> universeOfDependencies = Collections2.transform(universeOfDependenciesOriginal,
				function);

		for (Map.Entry<String, Map<String, ArrayList<String>>> entry : blocos.entrySet()) {
			try {
				String expectedClass = entry.getKey();
				for (Map.Entry<String, ArrayList<String>> entry2 : entry.getValue().entrySet()) {
					try {
						String expectedMethod = entry2.getKey();
						Map<String, Collection<Object[]>> depBM = ds.getDependenciesBM(expectedClass, expectedMethod);
						
						//TODO: fazer de um jeito menos gambiarra
						ArrayList<String> keysParaRemover = new ArrayList<String>();
						for (Map.Entry<String, Collection<Object[]>> entryBM : depBM.entrySet()) {
							if(!entryBM.getKey().contains("-")){
								keysParaRemover.add(entryBM.getKey());
							}
						}
						
						for (String key : keysParaRemover) {
							depBM.remove(key);
						}
						keysParaRemover.clear();
						
						for (Map.Entry<String, Collection<Object[]>> entryBM : depBM.entrySet()) {
							try {
								String pkg = AirpUtil.getPackageFromClassName(expectedClass);
								String pkgclass = pkg + "." + expectedClass;
								String blockNum = "";
								int lineUnderAnalysis = 0;
								for (Object[] obj : entryBM.getValue()) {
									blockNum = obj[3].toString();
									lineUnderAnalysis = Integer.parseInt(obj[5].toString());
								}

								if (pkgclass.toLowerCase().contains("test")) {
									// System.out.println("Block " + blockNum +
									// " from
									// method " + expectedMethod + " in class "
									// + expectedClass + " was ignored (test
									// class).");
									// salvaBM=false;
									continue;
								}

								// if
								// (!AirpUtil.checkIfIsNotInterface(javaProject,
								// expectedClass)) {
								// System.out.println("Block " + blockNum + "
								// from
								// method " + expectedMethod + " in class "
								// + expectedClass + " was ignored (method from
								// Interface)");
								// salvaBM=false;
								// continue;
								// }

								if (expectedClass.toLowerCase().endsWith(expectedMethod.toLowerCase())) {
									// System.out.println("Block " + blockNum +
									// " from
									// method " + expectedMethod + " in class "
									// + expectedClass + " was ignored
									// (Constructor
									// Method)");
									continue;
								}

								if (entryBM.getValue().isEmpty()) {
									// System.out.println("Block " + blockNum +
									// " from
									// method " + expectedMethod + " in class "
									// + expectedClass + " was ignored (there
									// are no
									// dependencies).");
									// salvaBM=false;
									continue;
								}

								if (!AirpUtil.moreThanNDependencies(entryBM.getValue(), 3)) {
									// System.out.println("Block " + blockNum +
									// " from
									// method " + expectedMethod + " in class "
									// + expectedClass + " was ignored (uses
									// less than 3
									// types).");
									// salvaBM=false;

									continue;
								}

								if (AirpUtil.isAloneInItsMethod(depBM)) {
									// System.out.println("Block " + blockNum +
									// " from
									// method " + expectedMethod + " in class "
									// + expectedClass + " was ignored (less
									// than 3 blocks
									// in the respective method).");
									// salvaBM=false;
									continue;
								}

								if (AirpUtil.isAloneInItsClass(ds.getDependenciesMC(expectedClass), expectedClass)) {
									// System.out.println("Method " +
									// expectedMethod + " in
									// class " + expectedClass
									// + " was ignored for Extract Method from
									// its blocks
									// (less than 3 methods in the respective
									// class)");
									// salvaMC=false;
									continue;
								}

								/*if (entryBM.getKey().length() < 2) {
									// System.out.println("Block " + blockNum +
									// " from
									// method " + expectedMethod + " in class "
									// + expectedClass + " was ignored (block of
									// the method
									// shouldn't be Extracted).");
									// salvaMC=false;
									continue;
								}*/

								Collection<? extends Object> tempRemoved = null;
								Collection<Object[]> dependenciesBlockUnderAnalysisOriginal = entryBM.getValue();

								// Map<String, Collection<Object[]>>
								// methodDependenciesOriginal2 = new
								// HashMap<String,
								// Collection<Object[]>>();
								// Collection<Object[]> objMethod= new
								// ArrayList<Object[]>();;

								String methodName = "";
								String className = "";
								boolean stop = false;

								for (Object[] depMethod : dependenciesBlockUnderAnalysisOriginal) {
									methodName = depMethod[2].toString();
									className = depMethod[4].toString();
								}

								if (!methodName.equals("") && !className.equals("")) {
									try {

										// tempRemoved =
										// methodDependencies.get(className).get(methodName).get(entryBM.getKey());
										// methodDependencies.get(className).get(methodName).remove(entryBM.getKey());

										tempRemoved = classDependencies.get(className).get(methodName);
										classDependencies.get(className).remove(methodName);

										// methodDependencies.get(pkg).get(className).put(methodName,
										// new
										// HashSet<Object>(Collections2.transform(methodDependenciesOriginal.get(className).get(methodName),
										// function)));

									} catch (NullPointerException npe) {
										stop = true;
										System.out.println("parou");
									}
								}

								if (!stop) {
									// methodDependenciesOriginal2.get(expectedClass).removeAll(dependenciesBlockUnderAnalysisOriginal2);

									Collection<? extends Object> dependenciesMethodWithNoExtractUnderAnalysis = Collections2
											.transform(ds.getDependenciesMC(className).get(methodName), function);

									if (!set) {
										StringBuilder s = SuitableModule.calculateAllBM(ds,
												methodName + "WithNoExtract", expectedClass,
												dependenciesMethodWithNoExtractUnderAnalysis,
												classDependencies.get(className), universeOfDependencies,
												"", "Max", className);

									} else {
										StringBuilder s = SuitableModule.calculateAllBM(ds,
												methodName + "WithNoExtract", expectedClass,
												new HashSet<Object>(dependenciesMethodWithNoExtractUnderAnalysis),
												classDependencies.get(className),
												new HashSet<Object>(universeOfDependencies), "", "Max",
												className);

									}

									Collection<Object[]> methodDependenciesUnderAnalysis = new ArrayList<Object[]>();

									for (Map.Entry<String, Collection<Object[]>> entryDepBM : depBM.entrySet()) {
										if (!entryDepBM.getKey().equals(entryBM.getKey())) {
											for (Object[] obj : entryDepBM.getValue()) {
												boolean diferente = true;
												for (Object[] obj2 : entryBM.getValue()) {
													//TODO: nome do método ta errado
													if (obj[0].toString().equals(obj2[0].toString())
															&& obj[1].toString().equals(obj2[1].toString())
															&& obj[4].toString().equals(obj2[4].toString())
															&& obj[6].toString().equals(obj2[6].toString())
															&& obj[7].toString().equals(obj2[7].toString())
															&& obj[8].toString().equals(obj2[8].toString()))
														diferente = false;
												}
												if (diferente)
													methodDependenciesUnderAnalysis.add(obj);
											}
										}
									}

									Collection<? extends Object> dependenciesMethodWithExtractUnderAnalysis = Collections2
											.transform(methodDependenciesUnderAnalysis, function);

									 classDependencies.get(className).put("ExtractedMethod_AIRP",
									 new HashSet<Object>(Collections2
												.transform(entryBM.getValue(), function)));

									if (!set) {
										StringBuilder s = SuitableModule.calculateAllBM(ds, methodName + "WithExtract",
												expectedClass, dependenciesMethodWithExtractUnderAnalysis,
												classDependencies.get(className), universeOfDependencies,
												blockNum, "Min", className);

									} else {
										// It is not linked with the original
										// list (because

										StringBuilder s = SuitableModule.calculateAllBM(ds, methodName + "WithExtract",
												expectedClass,
												new HashSet<Object>(dependenciesMethodWithExtractUnderAnalysis),
												classDependencies.get(className),
												new HashSet<Object>(universeOfDependencies), blockNum, "Min",
												className);

									}

									// Put it back after

									// methodDependencies.get(className).get(methodName).put(entryBM.getKey(),
									// new HashSet<Object>(tempRemoved));

									classDependencies.get(className).put(methodName, new HashSet<Object>(tempRemoved));
									classDependencies.get(className).remove("ExtractedMethod_AIRP");

								}

							} catch (NullPointerException npe) {
								System.out.println("Paoru_geral6");
							}
						}
					} catch (NullPointerException npe) {
						System.out.println("Paoru_geral7");
					}
				}
			} catch (NullPointerException npe) {
				System.out.println("Paoru_geral8");
			}
		}
		for (Map.Entry<String, ArrayList<String>> entry : methods.entrySet()) {
			try{
			String expectedClass = entry.getKey();
			for (Map.Entry<String, Collection<Object[]>> entryMC : ds.getDependenciesMC(expectedClass).entrySet()) {
				try{
				String pkg = AirpUtil.getPackageFromClassName(expectedClass);
				String pkgclass = pkg + "." + expectedClass;
				int lineUnderAnalysis = 0;
				//TODO: arrumar isso
				/*for (Object[] obj : entryMC.getValue()) {

					lineUnderAnalysis = Integer.parseInt(obj[4].toString());
				}*/

				if (expectedClass.toLowerCase().endsWith(entryMC.getKey().toLowerCase())) {
					// System.out.println("Method " + entryMC.getKey() + " in
					// class " + expectedClass
					// + " was ignored (Constructor Method)");
					continue;
				}

				if (pkgclass.toLowerCase().contains("test")) {
					// System.out.println(
					// "Method " + entryMC.getKey() + " in class " +
					// expectedClass + " was ignored (test class)");
					// salvaMC=false;
					continue;
				}

				if (entryMC.getValue().isEmpty()) {
					// System.out.println("Method " + entryMC.getKey() + " in
					// class " + expectedClass
					// + " was ignored (there are no method dependencies)");
					// salvaMC=false;
					continue;
				}

				if (!AirpUtil.moreThanNDependencies(entryMC.getValue(), 3)) {
					// System.out.println("Method " + entryMC.getKey() + " in
					// class " + expectedClass
					// + " was ignored (uses less than 3 types)");
					// salvaMC=false;
					continue;
				}

				if (AirpUtil.isAloneInItsClass(ds.getDependenciesMC(expectedClass), expectedClass)) {
					// System.out.println("Method " + entryMC.getKey() + " in
					// class " + expectedClass
					// + " was ignored (less than 3 methods in the respective
					// class)");
					// salvaMC=false;
					continue;
				}

				Collection<Object[]> dependenciesMethodUnderAnalysisOriginal = entryMC.getValue();
				Collection<? extends Object> tempRemoved = null;


				// classDependenciesOriginal2.get(expectedClass).removeAll(dependenciesMethodUnderAnalysisOriginal2);
				String className = "";
				String methodName = "";
				boolean stop = false;

				try {
					for (Object[] depMethod : dependenciesMethodUnderAnalysisOriginal) {
						className = depMethod[3].toString();
					}

					try{	
						if (!className.equals("")) {
							tempRemoved = classDependencies.get(className).get(entryMC.getKey());
							classDependencies.get(className).remove(entryMC.getKey());
						}
					} catch (NullPointerException npe) {
						stop = true;
						System.out.println("parou2");
					}
				} catch (ArrayIndexOutOfBoundsException aioobe) {
					stop = true;
					System.out.println("parou3");
				}
				//}

				if (!stop) {

					Collection<? extends Object> dependenciesMethodUnderAnalysis = Collections2
							.transform(dependenciesMethodUnderAnalysisOriginal, function);

					if (!set) {

						StringBuilder b = SuitableModule.calculateAllMC(ds, entryMC.getKey(), expectedClass,
								dependenciesMethodUnderAnalysis, classDependencies, universeOfDependencies,
								lineUnderAnalysis);
						
					} else {
						// It is not linked with the original list (because
						// HashSet)

						StringBuilder b = SuitableModule.calculateAllMC(ds, entryMC.getKey(), expectedClass,
								new HashSet<Object>(dependenciesMethodUnderAnalysis), classDependencies,
								new HashSet<Object>(universeOfDependencies), lineUnderAnalysis);
						
					}

					// Put it back after
					classDependencies.get(className).put(entryMC.getKey(), new HashSet<Object>(tempRemoved));
				}
			}catch(NullPointerException npe){
				System.out.println("Paoru_geral9");
			}
			}
		}catch(NullPointerException npe){
			System.out.println("Paoru_geral10");
		}
		}

		for (String classUnderAnalysis : classes) {
			try{
			String expectedModule = AirpUtil.getPackageFromClassName(classUnderAnalysis);

			String pkgclass = expectedModule + "." + classUnderAnalysis;

			if (pkgclass.toLowerCase().contains("test")) {
				// System.out.println(
				// "Class " + classUnderAnalysis + " from " + expectedModule + "
				// was ignored (test class).");
				// salvaCP=false;
				continue;
			}

			// It may be redundant because the next if statement
			if (ds.getDependenciesCP(classUnderAnalysis).isEmpty()) {
				// System.out.println("Class " + classUnderAnalysis + " from " +
				// expectedModule
				// + " was ignored (there are no class dependencies).");
				// salvaCP=false;
				continue;
			}

			if (!AirpUtil.moreThanNDependencies(ds.getDependenciesCP(classUnderAnalysis), 3)) {
				// System.out.println("Class " + classUnderAnalysis + " from " +
				// expectedModule
				// + " was ignored (uses less than 3 types).");
				// salvaCP=false;
				continue;
			}

			if (AirpUtil.isAloneInItsPackage(ds, classUnderAnalysis, expectedModule, classes)) {
				// System.out.println("Class " + classUnderAnalysis + " from " +
				// expectedModule
				// + " was ignored (less than 3 classes in the respective
				// package).");
				// salvaCP=false;
				continue;
			}

			Collection<? extends Object> tempRemoved = null;

			Collection<Object[]> dependenciesClassUnderAnalysisOriginal = ds.getDependenciesCP(classUnderAnalysis);

			// Disregard dependencies of the own class
			tempRemoved = packagesDependencies.get(expectedModule).get(classUnderAnalysis);
			packagesDependencies.get(expectedModule).remove(classUnderAnalysis);

			Collection<? extends Object> dependenciesClassUnderAnalysis = Collections2
					.transform(dependenciesClassUnderAnalysisOriginal, function);

			if (!set) {

				StringBuilder s = SuitableModule.calculateAll(ds, classUnderAnalysis, expectedModule,
						dependenciesClassUnderAnalysis, packagesDependencies, universeOfDependencies);
				
			} else {
				// It is not linked with the original list (because HashSet)
				StringBuilder s = SuitableModule.calculateAll(ds, classUnderAnalysis, expectedModule,
						new HashSet<Object>(dependenciesClassUnderAnalysis), packagesDependencies,
						new HashSet<Object>(universeOfDependencies));
				
			}

			// Put it back after
			packagesDependencies.get(expectedModule).put(classUnderAnalysis, new HashSet<Object>(tempRemoved));

			/*
			 * if (set) { // Restore what it was
			 * 
			 * packagesDependencies.put(expectedModule, new
			 * HashSet<Object>(Collections2.transform(
			 * packagesDependenciesOriginal.get(expectedModule), function))); }
			 */

		}catch(NullPointerException npe){
			System.out.println("Paoru_geral11");
		}
		}
	}

	/**
	 * Method responsible for getting the architecture and the initialization of
	 * the dependencies (if they have not been initialized yet)
	 */
	private DataStructure init(IProject project)
			throws CoreException, IOException, ClassNotFoundException, ParseException {
		final DataStructure ds = DataStructureUtils.getOrInitializeDataStructure(project);

		for (String s : ds.getProjectClasses()) {
			if (ds.getDependenciesCP(s) == null || ds.getDependenciesMC(s) == null || ds.getDependenciesBM(s) == null) {
				for (String className : ds.getProjectClasses()) {
					Collection<Object[]> dependenciesCP = AirpPersistence.load(project, className + "CP");
					Collection<Object[]> dependenciesMC = AirpPersistence.load(project, className + "MC");
					Collection<Object[]> dependenciesBM = AirpPersistence.load(project, className + "BM");
					if (dependenciesCP == null || dependenciesMC == null || dependenciesBM == null) {
						// throw new CoreException(null);
						System.out.println("parou_null");
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
	
	private void hideView() {
		IWorkbenchPage wp = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IViewPart myView1 = wp.findView("airptool.views.RefactoringViewMC");
		IViewPart myView2 = wp.findView("airptool.views.RefactoringViewMM");
		IViewPart myView3 = wp.findView("airptool.views.RefactoringViewEM");
		wp.hideView(myView1);
		wp.hideView(myView2);
		wp.hideView(myView3);
	}

	private void openView() {
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("airptool.views.RefactoringViewMC");
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("airptool.views.RefactoringViewMM");
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("airptool.views.RefactoringViewEM");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
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