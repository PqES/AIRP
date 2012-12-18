package airptool.handlers;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

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
import airptool.persistence.AirpPersistence;
import airptool.util.AirpUtil;
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

			// dialog.setElements(new String[] { "[T]", "[dt, T]" });
			dialog.setTitle(AirpUtil.NOME_APLICACAO);
			dialog.setMessage("Which projects would you like to analyze? (it has to be opened and Airp enabled)");
			// dialog.setInitialSelections(new String[] { "[T]" });

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

				final Map<String, Collection<Object[]>> packagesDependenciesOriginal = AirpUtil.getPackagesDependencies(ds);
				final Collection<Object[]> universeOfDependenciesOriginal = ds.getUniverseOfDependencies();
				long inicioGeral = System.currentTimeMillis();

				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, universeOfDependenciesOriginal, new TypeFunction(),
						false, "List_{T}");
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, universeOfDependenciesOriginal, new DependencyAndTypeFunction(),
						false, "List_{dp,T}");
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, universeOfDependenciesOriginal, new TypeFunction(),
						true, "Set_{T}");
				this.calculate(project, javaProject, ds, packagesDependenciesOriginal, universeOfDependenciesOriginal, new DependencyAndTypeFunction(),
						true, "Set_{dp,T}");
				
				System.out.printf("[%s] Total time: %.3f seconds.\n", project.getName(),
						(System.currentTimeMillis() - inicioGeral) / 1000.0);

				System.gc();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		return null;
	}

	private void calculate(final IProject project, final IJavaProject javaProject, final DataStructure ds,
			final Map<String, Collection<Object[]>> packagesDependenciesOriginal,
			final Collection<Object[]> universeOfDependenciesOriginal, final Function<Object[], String> function, final boolean set, final String identifier)
			throws FileNotFoundException, JavaModelException, CoreException {
		StringBuilder result = new StringBuilder();
		Collection<String> classes = ds.getProjectClasses();

		String filename = "result_" + identifier + "_" + project.getName() + "_" + DateUtil.dateToStr(new Date(), "yyyyMMdd'_'HHmmss")
				+ ".txt";

		PrintWriter out = new PrintWriter(new FileOutputStream(AirpUtil.TEMP_FOLDER + filename));

		// Change the universe
		Collection<? extends Object> universeOfDependencies = Collections2.transform(universeOfDependenciesOriginal, function);

		// Change the packages
		Map<String, Collection<? extends Object>> packagesDependencies = new HashMap<String, Collection<? extends Object>>();
		for (String key : packagesDependenciesOriginal.keySet()) {
			if (set){
				packagesDependencies.put(key, new HashSet<Object>(Collections2.transform(packagesDependenciesOriginal.get(key), function)));
			}else{
				packagesDependencies.put(key, Collections2.transform(packagesDependenciesOriginal.get(key), function));
			}
		}

		int i = 0;
		for (String classUnderAnalysis : classes) {
			System.out.printf("[%s] %4d of %4d: (%s): ", project.getName(), ++i, classes.size(), classUnderAnalysis);

			String expectedModule = AirpUtil.getPackageFromClassName(classUnderAnalysis);

			if (AirpUtil.isAloneInItsPackage(javaProject, classUnderAnalysis)) {
				System.out.println("ignored (lonely).");
				continue;
			}

			if (!AirpUtil.moreThanNDependencies(ds.getDependencies(classUnderAnalysis), 5)) {
				System.out.println("ignored (uses less than 5 types).");
				continue;
			}

			long inicio = System.currentTimeMillis();

			Collection<Object[]> dependenciesClassUnderAnalysisOriginal = ds.getDependencies(classUnderAnalysis);

			// Disregard dependencies of the own class
			packagesDependenciesOriginal.get(expectedModule).removeAll(dependenciesClassUnderAnalysisOriginal);

			Collection<? extends Object> dependenciesClassUnderAnalysis = Collections2.transform(dependenciesClassUnderAnalysisOriginal,
					function);

			if (!set) {
				result.append(SuitableModule.calculateAll(ds, classUnderAnalysis, expectedModule, dependenciesClassUnderAnalysis,
						packagesDependencies, universeOfDependencies));
			} else {
				//It is not linked with the original list (because HashSet)
				packagesDependencies.put(expectedModule, new HashSet<Object>(Collections2.transform(packagesDependenciesOriginal.get(expectedModule), function)));
				
				result.append(SuitableModule.calculateAll(ds, classUnderAnalysis, expectedModule, new HashSet<Object>(dependenciesClassUnderAnalysis),
						packagesDependencies, new HashSet<Object>(universeOfDependencies)));
			}

			// Put it back after
			packagesDependenciesOriginal.get(expectedModule).addAll(dependenciesClassUnderAnalysisOriginal);
			
			if (set){
				//Restore what it was
				packagesDependencies.put(expectedModule, new HashSet<Object>(Collections2.transform(packagesDependenciesOriginal.get(expectedModule), function)));
			}

			result.append("\n");

			System.out.printf("it took %.3f seconds.\n", (System.currentTimeMillis() - inicio) / 1000.0);

			out.write(result.toString());
			result.delete(0, result.length());
		}

		out.close();
	}

	/**
	 * Method responsible for getting the architecture and the initialization of
	 * the dependencies (if they have not been initialized yet)
	 */
	private DataStructure init(IProject project) throws CoreException, IOException, ClassNotFoundException, ParseException {
		final DataStructure ds = DataStructureUtils.getOrInitializeDataStructure(project);

		for (String s : ds.getProjectClasses()) {
			if (ds.getDependencies(s) == null) {
				for (String className : ds.getProjectClasses()) {
					Collection<Object[]> dependencies = AirpPersistence.load(project, className);
					if (dependencies == null) {
						throw new CoreException(null);
					}
					ds.updateDependencies(className, dependencies);
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