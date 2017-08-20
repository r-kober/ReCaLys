package de.upb.recalys.helper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * The Class ResourceHandler is used to import resources, that are stored within
 * the binaries, in different forms.
 */
public class ResourceHandler {

	/**
	 * Returns the URL from the resource that the path leads to in the following
	 * form: "url('url/to/file/in/the/system')".
	 *
	 * @param path
	 *            the path within the class structure. For further information look
	 *            at {@link java.lang.Class#getResource(String name) }
	 * @return the url as a String or null if no resource with this name is found
	 */
	public static String getURL(String path) {
		URL url = ResourceHandler.class.getResource(path);
		if (url == null) {
			return null;
		} else {
			return "url('" + ResourceHandler.class.getResource(path).toString() + "')";
		}
	}

	/**
	 * Gets the file content as a String.
	 *
	 * @param path
	 *            the path to the file
	 * @return the file content as a String
	 */
	public static String getDataAsString(String path) {
		InputStream is = ResourceHandler.class.getResourceAsStream(path);

		String data = new BufferedReader(new InputStreamReader(is)).lines().collect(Collectors.joining("\n"));

		return data;

	}

	/**
	 * Returns the file from the path as a File object.
	 *
	 * @param path
	 *            the path to the file
	 * @return the file as a File object
	 * @throws URISyntaxException
	 *             the URI syntax exception
	 */
	public static File getFile(String path) throws URISyntaxException {
		File file = new File(ResourceHandler.class.getResource(path).toURI());
		return file;
	}
}
