package airptool.persistence;

import static airptool.util.AirpUtil.DATA_FOLDER;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public final class AirpPersistence {
	private AirpPersistence() {
	}

	public static void persist(final IProject project, final String className, final Collection<Object[]> dependencies)
			throws CoreException, IOException {
		IFolder folder = project.getFolder(DATA_FOLDER);
		if (!folder.exists()) {
			folder.create(false, true, null);
			folder.setHidden(true);
		} else {
			folder.setHidden(true);
		}
		IFile storeFile = folder.getFile(className);
		if (storeFile.exists()) {
			storeFile.delete(true, false, null);
		}
		ByteArrayOutputStream bStream = new ByteArrayOutputStream();
		ObjectOutputStream oStream = new ObjectOutputStream(bStream);
		oStream.writeObject(dependencies);
		byte[] byteVal = bStream.toByteArray();
		storeFile.create(new ByteArrayInputStream(byteVal), IResource.FORCE, null);
	}

	public static void delete(final IProject project, final String className) throws CoreException {
		IFile storeFile = project.getFile(DATA_FOLDER + File.separator + className);
		if (!storeFile.exists()) {
			storeFile.delete(true, false, null);
		}
	}

	@SuppressWarnings("unchecked")
	public static Collection<Object[]> load(final IProject project, final String className) throws CoreException, IOException,
			ClassNotFoundException {
		IFile storeFile = project.getFile(DATA_FOLDER + File.separator + className);
		if (storeFile.exists()) {
			ObjectInputStream in = null;
			try {
				in = new ObjectInputStream(storeFile.getContents());
				return (Collection<Object[]>) in.readObject();
			} finally {
				if (in != null) {
					in.close();
				}
			}
		}
		return null;
	}

	public static void clean(final IProject project) throws CoreException {
		IFolder folder = project.getFolder(DATA_FOLDER);
		if (folder.exists()) {
			folder.delete(true, false, null);
		}
	}

	public static boolean existsFolder(final IProject project) {
		return project.getFolder(DATA_FOLDER).exists();
	}

}
