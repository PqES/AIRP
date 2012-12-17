package airptool.util;

import javax.print.attribute.standard.Severity;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class MarkerUtils {
	private static final String MARKER_ERROR_TYPE = "airptool.error";
	private static final String MARKER_TYPE = "airptool.violation";
	private static final Severity SEVERITY_TYPE = Severity.ERROR;

	private MarkerUtils() {
	}

	public static IMarker addErrorMarker(IProject project, String message) throws CoreException {
		IMarker marker = project.createMarker(MARKER_ERROR_TYPE);
		marker.setAttribute(IMarker.SEVERITY, SEVERITY_TYPE.getValue());
		marker.setAttribute(IMarker.MESSAGE, message);
		return marker;
	}

//	public static IMarker addErrorMarker(IFile file, String message, int lineNumber) throws CoreException {
//		IMarker marker = file.createMarker(MARKER_ERROR_TYPE);
//		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
//		marker.setAttribute(IMarker.SEVERITY, Severity.ERROR.getValue());
//		marker.setAttribute(IMarker.MESSAGE, message);
//		return marker;
//	}

	public static void deleteErrorMarker(IProject project) throws CoreException {
		project.deleteMarkers(MarkerUtils.MARKER_ERROR_TYPE, false, IResource.DEPTH_ZERO);
	}

	public static void deleteMarkers(IFile file) throws CoreException {
		file.deleteMarkers(MarkerUtils.MARKER_TYPE, false, IResource.DEPTH_ZERO);
	}

	public static void deleteMarkers(IProject project) throws CoreException {
		project.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_INFINITE);
	}

}
