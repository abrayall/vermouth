package vermouth.properties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import vermouth.Version;

/**
 * Properties version parser
 */
public class Parser {

	
	/**
	 * Parses a properties string representing a version
	 * @param properties the String containing the properties representing a version
	 * @return the Version object that representing the version contained in the given string
	 * @throws Exception if an error is encountered while parsing
	 */
	public Version parse(String properties) throws Exception {
		return parse(new ByteArrayInputStream(properties.getBytes()));
	}
	
	
	/**
	 * Parses a properties file representing a version
	 * @param file the file that should be parsed
	 * @return the Version object that represents the version contained in the properties file
	 * @throws Exception if an error is encountered while parsing
	 */
	public Version parse(File file) throws Exception {
		return parse(new FileInputStream(file));
	}
	
	
	/**
	 * Parses a properties input stream representing a version
	 * @param inputStream the input stream that should be parsed
	 * @return a Version object representing the version stored in the input stream
	 * @throws Exception if an error is encountered while parsing
	 */
	public Version parse(InputStream inputStream) throws Exception {
		return parse(properties(inputStream));
	}
	
	
	/**
	 * Parses a version from a properties object
	 * @param properties the properties object that should be parsed
	 * @return a Version object representing the version stored in the given properties object
	 */
	public Version parse(Properties properties) {
		return Version.parse(
			get(properties, "major", "0") + "." + 
			get(properties, "minor", "0") + "." + 
			get(properties, "patch", "0") + 
			valid(get(properties, "qualifier", get(properties, "prerelease", "")), "-") + 
			valid(get(properties, "metadata", ""), "+")
		);	
	}
	
	/**
	 * Loads properties from a given inputstream
	 * @param inputStream the inputstream that the properties should be loaded from
	 * @return a Properties object loaded with property values from the inputstream
	 * @throws Exception if an error is encountered while reading from the inputstream
	 */
	protected static Properties properties(InputStream inputStream) throws Exception {
		Properties properties = new Properties();
		try {
			properties.load(inputStream);
		} catch(Exception e) {} 
		finally {
			if (inputStream != null) inputStream.close();
		}
				
		return properties;
	}
	
	
	/**
	 * Gets a property from a given properties object
	 * @param properties the properties object that should be used to look up the property
	 * @param property the name of the property that should be looked up (if the property does not exist, "version." + property will also be tried)
	 * @param defaultValue the value that should be returned if the property or "version" + property does not exist in the given properties object
	 * @return the value of the property (or "version." + property) if it exists in the given properties object or the given default value if not
	 */
	private static String get(Properties properties, String property, String defaultValue) {
		return properties.getProperty(property, properties.getProperty("version." + property, defaultValue));
	}
	
	
	/**
	 * Checks if a given value is valid (not null and not blank)
	 * @param value the value that should be checked
	 * @param prefix the prefix that should prepended to the value if is valid
	 * @return the prefix and value if the value if valid and blank if not
	 */
	private static String valid(String value, String prefix) {
		if (value != null && value.equals("") == false)
			return prefix + value;
		
		return "";
	}
}
