package airptool.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import airptool.builder.AirpBuilder;
import airptool.core.DataStructure;

//TODO: MUITOS metodos comentados no Util. Verificar se deve mudar isso

public final class AirpUtil {
	public static final String NOME_APLICACAO = ".: Airp Tool :.";
	public static final String DATA_FOLDER = "airpdata";

	// TODO: To be parameterized
	public static final String TEMP_FOLDER = "/Users/arthurfp/Desktop/REPORTS/";

	// TODO: Construtor private e vazio. Ver se deve mudar
	private AirpUtil() {
	}

	public static boolean isAirpEnabled(IProject project) throws CoreException {
		for (ICommand c : project.getDescription().getBuildSpec()) {
			if (c.getBuilderName().equals(AirpBuilder.BUILDER_ID)) {
				return true;
			}
		}
		return false;
	}

	// /**
	// * DCL2 Adjust the name of the class to make the identification easier It
	// is
	// * done by converting all "/" to "."
	// *
	// * Still "converts" the primitive types to your Wrapper.
	// *
	// * @param className
	// * Name of the class
	// * @return Adjusted class name
	// */
	// public static String adjustClassName(String className) {
	// if (className.startsWith("boolean") || className.startsWith("byte") ||
	// className.startsWith("short")
	// || className.startsWith("long") || className.startsWith("double") ||
	// className.startsWith("float")) {
	// return "java.lang." + className.toUpperCase().substring(0, 1) +
	// className.substring(1);
	// } else if (className.startsWith("int")) {
	// return "java.lang.Integer";
	// } else if (className.startsWith("int[]")) {
	// return "java.lang.Integer[]";
	// } else if (className.startsWith("char")) {
	// return "java.lang.Character";
	// } else if (className.startsWith("char[]")) {
	// return "java.lang.Character[]";
	// }
	// return className.replaceAll("/", ".");
	// }
	//
	// /**
	// * DCL2 Checks whether the given resource is a Java source file.
	// *
	// * @param resource
	// * * The resource to check.
	// * @return <code>true</code> if the given resource is a Java source file,
	// * <code>false</code> otherwise.
	// */
	public static boolean isJavaFile(IResource resource) {
		if (resource == null || (resource.getType() != IResource.FILE)) {
			return false;
		}
		String ex = resource.getFileExtension();
		return "java".equalsIgnoreCase(ex); //$NON-NLS-1$
	}

	//
	// /**
	// * DCL2 Checks whether the given resource is a Java class file.
	// *
	// * @param resource
	// * The resource to check.
	// * @return <code>true</code> if the given resource is a class file,
	// * <code>false</code> otherwise.
	// */
	// public static boolean isClassFile(IResource resource) {
	// if (resource == null || (resource.getType() != IResource.FILE)) {
	// return false;
	// }
	// String ex = resource.getFileExtension();
	//		return "class".equalsIgnoreCase(ex); //$NON-NLS-1$
	//
	// }
	//
	// /**
	// * DCL2 Returns all class files inside a specific folder
	// *
	// * @param folder
	// * Startup folder
	// * @return List of class files
	// * @throws CoreException
	// */
	// public static Collection<IFile> getAllClassFiles(IFolder folder) throws
	// CoreException {
	// Collection<IFile> projectClassResources = new HashSet<IFile>();
	//
	// for (IResource resource : folder.members()) {
	// if (resource.getType() == IResource.FOLDER) {
	// projectClassResources.addAll(getAllClassFiles((IFolder) resource));
	// } else if (isClassFile(resource)) {
	// projectClassResources.add((IFile) resource);
	// }
	// }
	//
	// return projectClassResources;
	// }
	//
	// /**
	// * DCL2 Returns all class files inside the project
	// *
	// * @param project
	// * Java Project
	// * @return List of class files
	// * @throws CoreException
	// */
	// public static Collection<IFile> getAllClassFiles(IProject project) throws
	// CoreException {
	// IJavaProject javaProject = JavaCore.create(project);
	// IPath binDir = javaProject.getOutputLocation();
	// return
	// AirpUtil.getAllClassFiles(project.getFolder(binDir.removeFirstSegments(1)));
	// }
	//
	public static Collection<String> getClassNames(final IProject project) throws CoreException {
		final Collection<String> result = new LinkedList<String>();
		project.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) {
				if (resource instanceof IFile && resource.getName().endsWith(".java")) {
					ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
					final String className = AirpUtil.getClassName(unit);
					if (unit.isOpen() && className != null){
						result.add(className);
					}
				}
				return true;
			}
		});

		//Collections.sort((List<String>) result);
		//Collections.reverse((List<String>) result);

		return result;
	}
	
	public static Collection<String> getMethodNames(final IProject project, final String className) throws CoreException {
		final Collection<String> result = new LinkedList<String>();
		project.accept(new IResourceVisitor() {

			@Override
			public boolean visit(IResource resource) {
				if (resource instanceof IMethod) {
					final String methodName = resource.getName();
					if (className.equals(((IMethod)resource).getClassFile().getElementName()) && methodName != null){
						result.add(methodName);
					}
				}
				return true;
			}
		});

		//Collections.sort((List<String>) result);
		//Collections.reverse((List<String>) result);

		return result;
	}

	//
	// @Deprecated
	// public static Collection<IFile> getJavaClasses(final IProject project)
	// throws CoreException {
	// final Collection<IFile> result = new LinkedList<IFile>();
	// project.accept(new IResourceVisitor() {
	//
	// @Override
	// public boolean visit(IResource resource) {
	// if (resource instanceof IFile && resource.getName().endsWith(".java")) {
	// result.add((IFile)resource);
	// }
	// return true;
	// }
	// });
	// return result;
	// }
	//
	// /**
	// * DCL2 The method returns the respective IFile of a java source file
	// *
	// * If the className is an internal class, the parent class will be
	// returned
	// *
	// * @param javaProject
	// * Java Project
	// * @param className
	// * Name of the class in the following format: #org.Foo#
	// * @return Class IFile resource
	// * @throws JavaModelException
	// */
	public static IFile getFileFromClassName(IJavaProject javaProject, final String className) throws JavaModelException {
		for (IPackageFragmentRoot folder : javaProject.getAllPackageFragmentRoots()) {
			if (folder.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IPath path = folder.getPath();
				path = path.removeFirstSegments(1);

				/* If was internal class, consider the parent class */
				if (className.contains("$")) {
					path = path.append(className.substring(0, className.indexOf('$')).replaceAll("[.]", "" + IPath.SEPARATOR) + ".java");
				} else {
					path = path.append(className.replaceAll("[.]", "" + IPath.SEPARATOR) + ".java");
				}

				IFile file = javaProject.getProject().getFile(path);
				if (file.exists()) {
					return file;
				}
			}
		}
		return null;
	}

	//
	// /**
	// * DCL2 Method responsible to log error
	// *
	// * @param project
	// * Project where the error occurs
	// * @param e
	// * Thrown exception
	// * @throws CoreException
	// */

	public static String logError(IProject project, Throwable e) {
		final IFile dcFile = project.getFile("airptool_" + DateUtil.dateToStr(new Date(), "yyyyMMdd-HHmmss") + "_error.log");

		/* Return file name created */
		StringBuilder str = new StringBuilder();
		str.append(e.toString() + "\n");
		if (e.getStackTrace() != null) {
			for (StackTraceElement ste : e.getStackTrace()) {
				str.append("\t" + ste.toString() + "\n");
			}
		}

		InputStream source = new ByteArrayInputStream(str.toString().getBytes());
		try {
			dcFile.create(source, false, null);

		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		return dcFile.getName();

	}

	//
	// /**
	// * DCL2 Returns the module definition from the Java API
	// *
	// * @return $java DCL constraint
	// */
	// public static String getJavaModuleDefinition() {
	// return
	// "java.**,javax.**,org.ietf.jgss.**,org.omg.**,org.w3c.dom.**,org.xml.sax.**,boolean,char,short,byte,int,float,double,void";
	// }
	//
	// /**
	// * DCL2 Checks if a className is contained in the Java API
	// *
	// * @param className
	// * Name of the class
	// * @return true if it is, no otherwise
	// */
	// public static boolean isFromJavaAPI(final String className) {
	// for (String javaModulePkg : getJavaModuleDefinition().split(",")) {
	// String prefix = javaModulePkg.substring(0, javaModulePkg.indexOf(".**"));
	// if (className.startsWith(prefix)) {
	// return true;
	// }
	// }
	// return false;
	// }
	//
	// public static String getNumberWithExactDigits(int originalNumber, int
	// numDigits) {
	// String s = "" + originalNumber;
	// while (s.length() < numDigits) {
	// s = "0" + s;
	// }
	// return s;
	// }
	//
	// // public static void createReport(IProject project, Architecture av,
	// // List<ArchitecturalDrift> architecturalDrifts, long inicio, long
	// termino)
	// // throws IOException {
	// //
	// // final IFile dcFile = project.getFile("dclcheck_"
	// // + DateUtils.dateToStr(new Date(), "yyyyMMdd-HHmmss") + "_report.log");
	// //
	// // StringBuilder str = new StringBuilder();
	// //
	// // str.append("dclcheck v1.0.2 (20081029):\n");
	// // str.append("General Informations:\n");
	// // str.append("Start Time:\t" + DateUtils.dateToStr(new Date(inicio),
	// // DateUtils.fullPattern)
	// // + "\n");
	// // str.append("End Time:\t" + DateUtils.dateToStr(new Date(termino),
	// // DateUtils.fullPattern)
	// // + "\n");
	// // str.append("Spent Time:\t" + ((termino - inicio) / 1000.0) +
	// // " seconds\n");
	// // str.append("\n\n\n");
	// // // double numDepEst = av.getNumberOfEstabilishedDependencies();
	// // // writer.println("Estabilished Dependencies: " + numDepEst);
	// // // writer.println("Violated Dependencies: " +
	// // // architecturalDrifts.size());
	// // // writer.println("Architectural Conforma��o: "
	// // // + (numDepEst - architecturalDrifts.size()) / numDepEst);
	// //
	// // if (architecturalDrifts != null && !architecturalDrifts.isEmpty()) {
	// //
	// // Set<DependencyConstraint> dcList = new
	// TreeSet<DependencyConstraint>();
	// // for (ArchitecturalDrift ad : architecturalDrifts) {
	// // dcList.add(ad.getDependencyConstraint());
	// // }
	// // str.append("\n\n\nSummarized results:\n");
	// // str.append("DC\tNUMBER OF ARCHITECTURAL DRIFTS\n");
	// // for (DependencyConstraint dc : dcList) {
	// // int count = 0;
	// // for (ArchitecturalDrift ad : architecturalDrifts) {
	// // if (ad.getDependencyConstraint().equals(dc)) {
	// // count++;
	// // }
	// // }
	// // str.append(dc + "\t" + count + "\n");
	// // }
	// //
	// // str.append("\n\n\n\n");
	// // str.append("DC\tNUMBER OF CLASSES WITH ARCHITECTURAL DRIFTS\n");
	// // for (DependencyConstraint dc : dcList) {
	// // Set<String> cnList = new HashSet<String>();
	// // for (ArchitecturalDrift ad : architecturalDrifts) {
	// // if (ad.getDependencyConstraint().equals(dc)) {
	// // cnList.add(ad.getClassName());
	// // }
	// // }
	// // str.append(dc + "\t" + cnList.size() + "\n");
	// // }
	// //
	// // str.append("\n\n\n\n");
	// //
	// str.append("DC\tCLASS NAME\tNUMBER OF ARCHITECTURAL DRIFT IN CLASS\n");
	// // for (DependencyConstraint dc : dcList) {
	// // Set<String> cnList = new HashSet<String>();
	// // for (ArchitecturalDrift ad : architecturalDrifts) {
	// // if (ad.getDependencyConstraint().equals(dc)) {
	// // cnList.add(ad.getClassName());
	// // }
	// // }
	// //
	// // for (String className : cnList) {
	// // int count = 0;
	// // for (ArchitecturalDrift ad : architecturalDrifts) {
	// // if (ad.getDependencyConstraint().equals(dc)
	// // && ad.getClassName().equals(className)) {
	// // count++;
	// // }
	// // }
	// // str.append(dc + "\t" + className + "\t" + count + "\n");
	// // }
	// // }
	// //
	// // str.append("\n\n\n");
	// // str.append("Found architectural drifts (result in Eclipse):\n");
	// // str.append("DC\tCLASS NAME\tLINE NUMBER\tMESSAGE\n");
	// // for (ArchitecturalDrift ad : architecturalDrifts) {
	// // str.append(ad.getDependencyConstraint() + "\t" + ad.getClassName() +
	// "\t"
	// // + ad.getLineNumber() + "\t" + ad.getMessage() + "\n");
	// // }
	// //
	// // }
	// //
	// // InputStream source = new
	// ByteArrayInputStream(str.toString().getBytes());
	// // try {
	// // dcFile.create(source, false, null);
	// //
	// IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
	// // dcFile);
	// // } catch (CoreException e1) {
	// // e1.printStackTrace();
	// // }
	// //
	// // }
	//
	// /**
	// * DCL2 Show a message
	// *
	// * @param shell
	// * @param message
	// */
	// public static void showMessage(Shell shell, String message) {
	// MessageDialog.openInformation(shell, NOME_APLICACAO, message);
	// }
	//
	// /**
	// * DCL2 Show an error
	// *
	// * @param shell
	// * @param message
	// */
	// public static void showError(Shell shell, String message) {
	// MessageDialog.openError(shell, NOME_APLICACAO, message);
	// }
	//
	// /**
	// * DCL2 Returns all dependencies from the class class
	// *
	// * @param classes
	// * List of classes
	// * @return List of dependencies
	// */
	// @Deprecated
	// public static Collection<Dependency> getDependenciesUsingASM(IFile file)
	// throws CoreException, IOException {
	// /*
	// * final DCLDeepDependencyVisitor cv = new DCLDeepDependencyVisitor();
	// * final Collection<Dependency> dependencies = new
	// * LinkedList<Dependency>();
	// *
	// * IJavaProject javaProject = JavaCore.create(file.getProject()); IPath
	// * binDir = javaProject.getOutputLocation(); IPath path =
	// * binDir.removeFirstSegments
	// * (1).append(file.getProjectRelativePath().removeFirstSegments(1));
	// * IFile binFile =
	// * javaProject.getProject().getFile(path.removeFileExtension
	// * ().addFileExtension("class"));
	// *
	// * ClassReader cr = new DCLClassReader(binFile.getContents());
	// * cr.accept(cv, 0); dependencies.addAll(cv.getDependencies());
	// *
	// * return dependencies;
	// */
	// return null;
	// }
	//

	public static Collection<Object[]> getDependenciesCPUsingAST(ICompilationUnit unit) throws CoreException, IOException {
		final Collection<Object[]> dependencies = new LinkedList<Object[]>();

		airptool.ast.DependencyVisitor cv = new airptool.ast.DependencyVisitor(unit);

		dependencies.addAll(cv.getDependenciesCP());
		return dependencies;
	}
	
	public static Collection<Object[]> getDependenciesMCUsingAST(ICompilationUnit unit) throws CoreException, IOException {
		final Collection<Object[]> dependencies = new LinkedList<Object[]>();

		airptool.ast.DependencyVisitor cv = new airptool.ast.DependencyVisitor(unit);

		dependencies.addAll(cv.getDependenciesMC());
		return dependencies;
	}
	
	public static Collection<Object[]> getDependenciesBMUsingAST(ICompilationUnit unit) throws CoreException, IOException {
		final Collection<Object[]> dependencies = new LinkedList<Object[]>();

		airptool.ast.DependencyVisitor cv = new airptool.ast.DependencyVisitor(unit);

		dependencies.addAll(cv.getDependenciesBM());
		return dependencies;
	}
	
	public static LinkedList<List<Object[]>> getDependenciesMC2UsingAST(ICompilationUnit unit) throws CoreException, IOException {
		final LinkedList<List<Object[]>> dependencies = new LinkedList<List<Object[]>>();

		airptool.ast.DependencyVisitor cv = new airptool.ast.DependencyVisitor(unit);

		dependencies.add(cv.getDependenciesMC());
		
		//dependencies.addAll(cv.getDependenciesMC());
		return dependencies;
	}
	
	public static LinkedList<List<Object[]>> getDependenciesBM2UsingAST(ICompilationUnit unit) throws CoreException, IOException {
		final LinkedList<List<Object[]>> dependencies = new LinkedList<List<Object[]>>();

		airptool.ast.DependencyVisitor cv = new airptool.ast.DependencyVisitor(unit);

		dependencies.add(cv.getDependenciesBM());
		
		//dependencies.addAll(cv.getDependenciesMC());
		return dependencies;
	}

	//
	// /**
	// * Checks if a specific class is contained in a list of classes, RE or
	// * packages
	// */
	// public static boolean hasClassNameByDescription(final String className,
	// final String moduleDescription,
	// final Map<String, String> modules, final Collection<String>
	// projectClassNames, final IProject project) {
	// for (String desc : moduleDescription.split(",")) {
	// desc = desc.trim();
	//
	// if ("$system".equals(desc)) {
	// /*
	// * If it's $system, any class
	// */
	// return projectClassNames.contains(className);
	// } else if (modules.containsKey(desc)) {
	// /*
	// * If it's a module, call again the same method to return with
	// * its description
	// */
	// if (hasClassNameByDescription(className, modules.get(desc), modules,
	// projectClassNames, project)) {
	// return true;
	// }
	// } else if (desc.endsWith("**")) {
	// /* If it refers to any class in any package below one specific */
	// desc = desc.substring(0, desc.length() - 2);
	// if (className.startsWith(desc)) {
	// return true;
	// }
	// } else if (desc.endsWith("*")) {
	// /* If it refers to classes inside one specific package */
	// desc = desc.substring(0, desc.length() - 1);
	// if (className.startsWith(desc) &&
	// !className.substring(desc.length()).contains(".")) {
	// return true;
	// }
	// } else if (desc.startsWith("\"") && desc.endsWith("\"")) {
	// /* If it refers to regular expression */
	// desc = desc.substring(1, desc.length() - 1);
	// if (className.matches(desc)) {
	// return true;
	// }
	// } else if (desc.endsWith("+")) {
	// /* If it refers to subtypes */
	// desc = desc.substring(0, desc.length() - 1);
	//
	// try {
	// IJavaProject javaProject = JavaCore.create(project);
	// IType type = javaProject.findType(desc);
	//
	// ITypeHierarchy typeHierarchy = type.newTypeHierarchy(null);
	// IType[] typeSubclasses = typeHierarchy.getAllSubtypes(type);
	//
	// StringBuilder strBuilder = new StringBuilder();
	// for (IType t : typeSubclasses) {
	// strBuilder.append(t.getFullyQualifiedName() + ",");
	// }
	// if (strBuilder.length() > 0) {
	// strBuilder.deleteCharAt(strBuilder.length() - 1);
	// }
	// modules.put(desc + "+", strBuilder.toString());
	// if (hasClassNameByDescription(className, modules.get(desc + "+"),
	// modules, projectClassNames, project)) {
	// return true;
	// }
	// } catch (JavaModelException e) {
	// e.printStackTrace();
	// }
	// } else {
	// /* If it refers to a specific class */
	// if (desc.equals(className)) {
	// return true;
	// }
	// }
	// }
	//
	// return false;
	// }
	//
	public static String getClassName(ICompilationUnit unit) {
		try {
			IPackageDeclaration packages[] = unit.getPackageDeclarations();
			String pack;
			if (packages.length > 0)
				pack = packages[0].getElementName() + ".";
			else
				pack = "";

			String clazz = unit.getElementName();
			clazz = clazz.substring(0, clazz.indexOf(".java"));

			return pack + clazz;
		} catch (JavaModelException e) {
			return null;
		}
	}

	public static String getPackageFromClassName(final String className) {
		if (className.contains(".")) {
			return className.substring(0, className.lastIndexOf('.'));
		}
		return "<default>";
	}

	public static String getSimpleClassName(final String qualifiedClassName) {
		return qualifiedClassName.substring(qualifiedClassName.lastIndexOf(".") + 1);
	}

	public static boolean isAloneInItsPackage(IJavaProject javaProject, String className) throws JavaModelException, CoreException {
		IFile file = getFileFromClassName(javaProject, className);
		if (file.getParent().getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) file.getParent();
			IResource members[] = folder.members();
			for (IResource resource : members) {
				if (resource.equals(file)) {
					continue;
				}
				if (isJavaFile(resource)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean isAloneInItsClass(Map<String, ArrayList<String>> methods, String className) throws JavaModelException, CoreException {

		for (Map.Entry<String, ArrayList<String>> entry : methods.entrySet()){
				if(entry.getKey().equals(className)){
					if(entry.getValue().size()>1) return false;
				} 
		}
		return true;
	}
	
	public static boolean isAloneInItsMethod(Map<String, Map<String, ArrayList<String>>> blocos, String methodName, String className) throws JavaModelException, CoreException {
		for (Map.Entry<String, Map<String, ArrayList<String>>> entry : blocos.entrySet()){
			String className2=entry.getKey();
			for (Map.Entry<String, ArrayList<String>> entry2 : entry.getValue().entrySet()){
				if(entry2.getKey().equals(methodName) && className2.equals(className)){
					if(entry2.getValue().size()>1) return false;
				}
			}
		}
		return true;
	}

	public static boolean moreThanNDependencies(Collection<Object[]> dependencies, int referenceSize) {
		Set<String> s = new HashSet<String>();
		for (Object[] o : dependencies) {
			s.add((String) o[1]); // Add the type
			if (s.size() >= referenceSize) {
				s.clear();
				return true;
			}
		}
		return false;
	}
	
	public static boolean moreThanNDependenciesBM(Collection<Object[]> dependencies, int referenceSize) {
		if (dependencies.size() >= referenceSize) {
			return true;
		}
		return false;
	}

	public static Set<String> getPackages(DataStructure ds) {
		Set<String> packages = new HashSet<String>();

		for (String className : ds.getProjectClasses()) {
			packages.add(getPackageFromClassName(className));
		}

		return packages;
	}

	public static Map<String, HashMap<String, Collection<Object[]>>> getPackagesDependencies(DataStructure ds) {
		final Map<String, HashMap<String, Collection<Object[]>>> packagesDependencies = new HashMap<String, HashMap<String, Collection<Object[]>>>();

		for (String className : ds.getProjectClasses()) {
			final String packageName = getPackageFromClassName(className);
			if(!packageName.contains("test")){
				if (!packagesDependencies.containsKey(packageName)) {
					packagesDependencies.put(packageName, new HashMap<String, Collection<Object[]>>());
				}
				packagesDependencies.get(packageName).put(className, ds.getDependenciesCP(className));
				//addAll(ds.getDependenciesCP(className));
		
			}
		}

		return packagesDependencies;
	}
	
/*	public static ArrayList<String> getClassImplements(String className, DataStructure ds) {
		ArrayList<String> classesImplemented = new ArrayList<String>();
		
		Collection<Object[]> dependencies = ds.getDependenciesCP(className);
		
		for(Object[] obj : dependencies){
			if(obj[0].equals("IMPLEMENT")){
				classesImplemented.add(obj[1].toString());
			}
		}
		return classesImplemented;
	}
*/
	
	public static boolean checkIfIsNotInterface(IJavaProject javaProject, final String className) throws JavaModelException, MalformedURLException, ClassNotFoundException {
		//return true;
		for (IPackageFragmentRoot folder : javaProject.getAllPackageFragmentRoots()) {
			if (folder.getKind() == IPackageFragmentRoot.K_SOURCE) {
				IPath path = folder.getPath();
				path = path.removeFirstSegments(1);
				IPath path2 = path;

				if (className.contains("$")) {
					path2 = path2.append(className.substring(0, className.indexOf('$')).replaceAll("[.]", "" + IPath.SEPARATOR) + ".java");
				} else {
					path2 = path2.append(className.replaceAll("[.]", "" + IPath.SEPARATOR) + ".java");
				}

				IFile file = javaProject.getProject().getFile(path2);
				if (file.exists()) {
					String caminho = folder.getResource().getRawLocation().toFile().toURI().toURL().toString();
					
					URLClassLoader loader = new URLClassLoader(new URL[] {
				            new URL(caminho.substring(0, caminho.length()-4)+"bin/")
				    });
					
					Class c = loader.loadClass(className);
					
					if(!Modifier.isInterface(c.getModifiers())){
						return true;
					}
					
				}
			}
		}
		return false;
	}
}

