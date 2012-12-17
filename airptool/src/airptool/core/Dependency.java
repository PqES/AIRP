package airptool.core;

import airptool.enums.DependencyType;

public class Dependency {
	final DependencyType dependencyType;
	final String targetClassName;
	
	public Dependency(DependencyType dependencyType, String targetClassName) {
		super();
		this.dependencyType = dependencyType;
		this.targetClassName = targetClassName;
	}
	
	public DependencyType getDependencyType() {
		return this.dependencyType;
	}
	
	public String getTargetClassName() {
		return this.targetClassName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dependencyType == null) ? 0 : dependencyType.hashCode());
		result = prime * result + ((targetClassName == null) ? 0 : targetClassName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Dependency other = (Dependency) obj;
		if (dependencyType != other.dependencyType)
			return false;
		if (targetClassName == null) {
			if (other.targetClassName != null)
				return false;
		} else if (!targetClassName.equals(other.targetClassName))
			return false;
		return true;
	}
	
}
