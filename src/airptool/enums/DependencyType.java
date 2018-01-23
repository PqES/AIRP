package airptool.enums;

public enum DependencyType {
	ACCESS("access"), USEANNOTATION("useannotation"), CREATE("create"), DECLARE("declare"), 
	DECLARE_ATTRIBUTE("declare an attribute"), DECLARE_FORMAL_PARAMETER("declare a formal parameter"), 
	DERIVE("derive"), EXTEND("extend"), HANDLE("handle"), IMPLEMENT("implement"), THROW("throw"), 
	DEPEND("depend");

	private final String value;

	private DependencyType(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}
	
}