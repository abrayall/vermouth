package vermouth.properties;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Properties;

import vermouth.Version;

public class Serializer {

	/**
	 * Gets a properties object that represents the current version
	 * @return a properties object containing the values that represent the current version
	 */
	public Properties properties(Version version) {
		Properties properties = new Properties();
		properties.setProperty("major", new Integer(version.getMajor()).toString());
		properties.setProperty("minor", new Integer(version.getMinor()).toString());
		properties.setProperty("patch", new Integer(version.getPatch()).toString());
		
		if (version.getMetadata().equals("") == false)
			properties.setProperty("metadata", version.getMetadata());
		
		if (version.getQualifier().equals("") == false) {
			properties.setProperty("qualifier", version.getQualifier());
			properties.setProperty("prerelease", version.getQualifier());
		}
		
		return properties;
	}
	
	
	/**
	 * Serializes a given version to properties string
	 * @param version the version that should be serialized
	 * @return a String containing the properties representation of the given version
	 * @throws Exception if an error is encountered while serializing
	 */
	public String serialize(Version version) throws Exception {
		return serialize(version, new ByteArrayOutputStream()).toString();
	}
	
	
	/**
	 * Serializes a given version to properties string to a given output stream
	 * @param version the version that should be serialized
	 * @return the output stream that the properties representation of the given version was written
	 * @throws Exception if an error is encountered while serializing
	 */
	public OutputStream serialize(Version version, OutputStream output) throws Exception {
		properties(version).store(output, "");
		return output;
	}
	
	
	/**
	 * Serializes a given version to properties string to a given file
	 * @param version the version that should be serialized
	 * @return the file that the properties representation of the given version was written
	 * @throws Exception if an error is encountered while serializing
	 */
	public File serialize(Version version, File file) throws Exception {
		serialize(version, new FileOutputStream(file)).close();
		return file;
	}
}
