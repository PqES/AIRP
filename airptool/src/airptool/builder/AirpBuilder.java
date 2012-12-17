package airptool.builder;

import java.io.IOException;
import java.text.ParseException;
import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;

import airptool.core.DataStructure;
import airptool.persistence.AirpPersistence;
import airptool.util.AirpUtil;
import airptool.util.DataStructureUtils;
import airptool.util.MarkerUtils;

public class AirpBuilder extends IncrementalProjectBuilder {
	public static final String BUILDER_ID = "airptool.airpBuilder";

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		super.clean(monitor);
		MarkerUtils.deleteMarkers(this.getProject());
		MarkerUtils.deleteErrorMarker(this.getProject());
		DataStructureUtils.cleanDataStructure(getProject());
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		try {
			if (kind == FULL_BUILD || !AirpPersistence.existsFolder(this.getProject())) {
				fullBuild(monitor);
			} else {
				IResourceDelta delta = getDelta(this.getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					/* If the architecture has not been initialized yet */
					if (!DataStructureUtils.hasDataStructureInitialized(getProject())) {
						fullLoad(monitor);
					}
					incrementalBuild(delta, monitor);
				}
			}
			MarkerUtils.deleteErrorMarker(this.getProject());
		} catch (Throwable e) {
			this.clean(monitor);
			final String logFileName = AirpUtil.logError(this.getProject(), e);
			MarkerUtils.addErrorMarker(this.getProject(), "The Airp Tool has crashed. (see " + logFileName + ")");
		}
		return null;
	}

	protected void fullLoad(final IProgressMonitor monitor) throws CoreException, IOException, ClassNotFoundException, ParseException {
		monitor.setTaskName("Airp Tool");
		monitor.subTask("setting up");
		final DataStructure ds = DataStructureUtils.getOrInitializeDataStructure(this.getProject());
		monitor.beginTask("Loading dependencies", ds.getProjectClasses().size());

		for (String className : ds.getProjectClasses()) {
			monitor.subTask(className);
			Collection<Object[]> dependencies = AirpPersistence.load(this.getProject(), className);
			if (dependencies == null) {
				throw new CoreException(null);
			}
			ds.updateDependencies(className, dependencies);
			monitor.worked(1);
		}
	}

	protected void fullBuild(final IProgressMonitor monitor) throws CoreException, IOException, ParseException, ClassNotFoundException {
		monitor.setTaskName("Airp Tool");
		monitor.subTask("setting up");
		final DataStructure ds = DataStructureUtils.initializeDataStructure(getProject());
		monitor.beginTask("Loading dependencies", ds.getProjectClasses().size());
		getProject().accept(new FullBuildVisitor(ds, monitor, true));
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException, IOException, ParseException {
		final DataStructure ds = DataStructureUtils.getOrInitializeDataStructure(getProject());
		monitor.beginTask("Updating dependencies",
				delta.getAffectedChildren(IResourceDelta.ADDED | IResourceDelta.CHANGED | IResourceDelta.REMOVED, IResource.FILE).length);
		delta.accept(new IncrementalDeltaVisitor(ds, monitor));
	}

	class IncrementalDeltaVisitor implements IResourceDeltaVisitor {
		private final DataStructure ds;
		private final IProgressMonitor monitor;

		public IncrementalDeltaVisitor(DataStructure ds, IProgressMonitor monitor) {
			this.ds = ds;
			this.monitor = monitor;
		}

		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			if (!(resource instanceof IFile) || !resource.getName().endsWith(".java") || !resource.exists()) {
				return true;
			}
			monitor.subTask(resource.getName());

			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				check(resource, ds, true);
				monitor.worked(1);
				break;
			case IResourceDelta.REMOVED:
				delete(resource);
				monitor.worked(1);
				break;
			case IResourceDelta.CHANGED:
				check(resource, ds, true);
				monitor.worked(1);
				break;
			}

			/* return true to continue visiting children */
			return true;
		}
	}

	class FullBuildVisitor implements IResourceVisitor {
		private final DataStructure ds;
		private final IProgressMonitor monitor;
		private final boolean reextractDependencies;

		public FullBuildVisitor(DataStructure ds, IProgressMonitor monitor, boolean reextractDependencies) {
			this.ds = ds;
			this.monitor = monitor;
			this.reextractDependencies = reextractDependencies;
		}

		public boolean visit(IResource resource) throws CoreException {
			if (resource instanceof IFile && resource.getName().endsWith(".java")) {
				monitor.subTask(resource.getName());
				check(resource, ds, reextractDependencies);
				monitor.worked(1);
			}
			return true;
		}
	}

	private void check(IResource resource, DataStructure ds, boolean reextractDependencies) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			final IFile file = (IFile) resource;
			MarkerUtils.deleteMarkers(file);

			final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
			final String className = AirpUtil.getClassName(unit);

			try {
				final Collection<Object[]> dependencies;
				if (reextractDependencies) {
					dependencies = AirpUtil.getDependenciesUsingAST(unit);
					ds.updateDependencies(className, dependencies);
					AirpPersistence.persist(this.getProject(), className, dependencies);
				} else {
					dependencies = ds.getDependencies(className);
				}
			} catch (IOException e) {
				MarkerUtils.addErrorMarker(this.getProject(), "There was a problem in extracting dependencies from " + className);
				throw new CoreException(Status.CANCEL_STATUS);
			}
		}
	}

	private void delete(IResource resource) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			final IFile file = (IFile) resource;
			MarkerUtils.deleteMarkers(file);

			final ICompilationUnit unit = ((ICompilationUnit) JavaCore.create((IFile) resource));
			final String className = AirpUtil.getClassName(unit);

			AirpPersistence.delete(this.getProject(), className);
		}
	}
}
