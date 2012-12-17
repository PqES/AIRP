package airptool.core;

import java.text.ParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import airptool.util.AirpUtil;

public class DataStructure {
	/**
	 * String: class name Collection<Dependency>: Collection of established
	 * dependencies
	 */
	public Map<String, Collection<Object[]>> projectClasses = null;

	public DataStructure(IProject project) throws CoreException, ParseException {
		this.projectClasses = new TreeMap<String, Collection<Object[]>>();

		for (String className : AirpUtil.getClassNames(project)) {
			this.projectClasses.put(className, null);
		}

	}

	public Set<String> getProjectClasses() {
		return projectClasses.keySet();
	}

	public Collection<Object[]> getDependencies(String className) {
		return projectClasses.get(className);
	}

	public void updateDependencies(String className, Collection<Object[]> dependencies) {
		this.filterCommonDependencies(dependencies);
		projectClasses.put(className, dependencies);
	}

	private void filterCommonDependencies(Collection<Object[]> dependencies) {
		String typesToDisregard[] = new String[] { "boolean", "char", "byte", "short", "int", "long", "float", "double",
				"java.lang.Boolean", "java.lang.Character", "java.lang.Byte", "java.lang.Short", "java.lang.Integer", "java.lang.Long",
				"java.lang.Float", "java.lang.Double", "java.lang.String", "java.lang.Object", "java.lang.Boolean[]",
				"java.lang.Character[]", "java.lang.Byte[]", "java.lang.Short[]", "java.lang.Integer[]", "java.lang.Long[]",
				"java.lang.Float[]", "java.lang.Double[]", "java.lang.String[]", "java.lang.Object[]", "java.lang.Deprecated",
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

		for (Collection<Object[]> col : projectClasses.values()) {
			set.addAll(col);
		}

		return set;
	}

}
