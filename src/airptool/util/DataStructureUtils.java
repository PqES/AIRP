package airptool.util;

import java.text.ParseException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import airptool.core.DataStructure;
import airptool.persistence.AirpPersistence;

public class DataStructureUtils {
	public static final QualifiedName AIRP_STRUCTURE = new QualifiedName("airptool", "dataStructure");

	private DataStructureUtils() {
	}

	public static boolean hasDataStructureInitialized(IProject project) throws CoreException {
		return project.getSessionProperties().containsKey(AIRP_STRUCTURE);
	}

	public static DataStructure initializeDataStructure(IProject project) throws CoreException, ParseException {
		final DataStructure ds = new DataStructure(project);
		project.setSessionProperty(AIRP_STRUCTURE, ds);
		return ds;
	}
	
	public static DataStructure getOrInitializeDataStructure(IProject project) throws CoreException, ParseException {
		final DataStructure ds = (DataStructure) project.getSessionProperty(AIRP_STRUCTURE);
		if (ds == null) {
			return initializeDataStructure(project);
		}
		return ds;
	}

	/**
	 * Method called by Builder:clean
	 */
	public static void cleanDataStructure(IProject project) throws CoreException {
		project.setSessionProperty(AIRP_STRUCTURE, null);
		AirpPersistence.clean(project);
	}

	
}
