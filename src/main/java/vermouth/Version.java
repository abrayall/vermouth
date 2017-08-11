package vermouth;

import java.io.*;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Class that represents a version of product, component or service
 * This class implements the semantic versioning specification described at {@link}http://semver.org{@link}
 */
public class Version {
	
	private int major = 0;
	private int minor = 0;
	private int patch = 0;
	
	private String metadata = "";
	private String qualifier = "";
	
	/**
	 * Constructs a version of 0.0.0
	 */
	public Version() {}
	
	
	/**
	 * Constructs a vesion with the given major, minor and patch numbers
	 * @param major the major version number 
	 * @param minor the minor version number
	 * @param patch the patch version number
	 */
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, "", "");
	}
	
	
	/**
	 * Constructs a vesion with the given major, minor, patch numbers and a qualifier and metedata labels
	 * @param major the major version number 
	 * @param minor the minor version number
	 * @param patch the patch version number
	 * @param qualifier the qualifier (prerelease) label
	 * @param metadata the metadata label
	 */
	public Version(int major, int minor, int patch, String qualifier, String metadata) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.qualifier = (qualifier != null ? qualifier : "");
		this.metadata = (metadata != null ? metadata : "");
	}
	
	
	/**
	 * Gets the major version number
	 * @return an integer representing the major version number
	 */
	public int getMajor() {
		return this.major;
	}
	
	
	/**
	 * Gets the minor version number
	 * @return an integer representing the minor version number
	 */
	public int getMinor() {
		return this.minor;
	}
	
	
	/**
	 * Gets the patch version number 
	 * @return an integer representing the patch version number
	 */
	public int getPatch() {
		return this.patch;
	}
	
	
	/**
	 * Get the qualifier (prerelease) label of the version
	 * @return a String containing the qualifier of the version (blank if there is not a qualifier label)
	 */
	public String getQualifier() {
		return this.qualifier;
	}
	
	
	/**
	 * Get the prerelease (qualifier) label of the version
	 * @return a String containing the prerelease of the version (blank if there is not a prerelease label)
	 */
	public String getPrerelease() {
		return this.getQualifier();
	}
	
	
	/**
	 * Get the the metadata label of the version
	 * @return a String containing the metadata of the version (blank if there is not a metedata label)
	 */
	public String getMetadata() {
		return this.metadata;
	}
		
	
	/**
	 * Determines if the current version is equal to the given object
	 * @param object an object that should be tested against the current version
	 * @return true if the current version is equal to the given object, false if not
	 */
	public boolean equals(Object object) {
		if (Version.class.isInstance(object))
			return this.equals((Version) object);
		
		return false;
	}

	/**
	 * Determines if the current version is equal to the given string
	 * @param version a string containing the version that should be tested against the current version
	 * @return true if the current version is equal to the given string, false if not
	 */
	public boolean equals(String version) {
		return this.toString().equals(version);
	}

	
	/**
	 * Determines if the current version is equal to the given version
	 * @param version the version that should be tested against the current version
	 * @return true if the current version is equal to the given version, false if not
	 */
	public boolean equals(Version version) {
		return this.equals(version.toString());
	}
	
	
	/**
	 * Compares the current version to a given string representing another version
	 * @param version a string containing the version that should be compared against the current version
	 * @return an integer where 1 means the current version is greater than given version, -1 means the given version is greater, and 0 if they are equal
	 */
	public int compare(String version) {
		return this.compare(parse(version));
	}
	
	
	/**
	 * Compares the current version to a given string representing another version
	 * @param version the version that should be compared against the current version
	 * @return an integer where 1 means the current version is greater than given version, -1 means the given version is greater, and 0 if they are equal
	 */
	public int compare(Version version) {
		if (this.major != version.major) return this.major > version.major ? 1 : -1;
		if (this.minor != version.minor) return this.minor > version.minor ? 1 : -1;
		if (this.patch != version.patch) return this.patch > version.patch ? 1 : -1;
		if (this.qualifier.equals("") == true && version.qualifier.equals("") == false) return 1;
		if (this.qualifier.equals("") == false && version.qualifier.equals("") == true) return -1;
		return this.compare(this.qualifier.split("\\."), version.qualifier.split("\\."));
	}
	
	
	/**
	 * Determines if the current version is greater than the given version
	 * @param version a string containing the version that should be compared against the current version
	 * @return true if the current version if greater than the given version
	 */
	public boolean isGreater(String version) {
		return this.isGreater(parse(version));
	}
	
	
	/**
	 * Determines if the current version is greater than the given version
	 * @param version  the version that should be compared against the current version
	 * @return true if the current version if greater than the given version
	 */
	public boolean isGreater(Version version) {
		return this.compare(version) == 1;
	}
	
	
	/**
	 * Determines if the current version is lesser than the given version
	 * @param version a string containing the version that should be compared against the current version
	 * @return true if the current version if lesser than the given version
	 */
	public boolean isLesser(String version) {
		return this.isLesser(parse(version));
	}
	
	
	/**
	 * Determines if the current version is lesser than the given version
	 * @param version the version that should be compared against the current version
	 * @return true if the current version if lesser than the given version
	 */
	public boolean isLesser(Version version) {
		return this.compare(version) == -1;
	}
	
	
	/**
	 * Returns the string representation of the current version
	 * @return a string containing the representing of the current version
	 */
	public String toString() {
		return this.major + "." + this.minor + "." + this.patch + 
			(this.qualifier.equals("") == false ? "-" + this.qualifier : "") + 
			(this.metadata.equals("") == false ? "+" + this.metadata : "");
	}	
	
	
	/**
	 * Gets a properties object that represents the current version
	 * @return a properties object containing the values that represent the current version
	 */
	public Properties properties() {
		return new vermouth.properties.Serializer().properties(this);
	}
	
	
	/**
	 * Searches for version.properties files in current directory and classpath and returns version object representing the one found
	 * @return a version object representing the version.properties found and null if nothing was found
	 */
	public static Version getVersion() throws Exception {
		return getVersion("0.0.0");
	}
	
	
	/**
	 * Searches for version.properties files in current directory and classpath and returns version object representing the one found
	 * @param defaultValue the version that should be returned if no version can be found
	 * @return a version object representing the version.properties found and null if nothing was found
	 */
	public static Version getVersion(String defaultValue) {
		try {
			return getVersion(getCallingClass());
		} catch (Exception e) {
			return Version.parse(defaultValue);
		}
	}
			
	
	/**
	 * Searches for version.properties files in current directory and classpath in the context of the given class and returns version object representing the one found
	 * @param clazz the class that should be used to searched for the version.properites (only files from the same jar file)
	 * @return a version object representing the version.properties found and null if nothing was found
	 * @throws Exception if an error is encountered while loading version.properties
	 */
	public static Version getVersion(Class<?> clazz) throws Exception {
		return getVersion(clazz, clazz.getClassLoader());
	}
	
	
	/**
	 * Searches for version.properties files in current directory and classpath using the given classloader and returns version object representing the one found
	 * @param classloader the classloader that should be used to search for version.properties
	 * @return a version object representing the version.properties found and null if nothing was found
	 * @throws Exception if an error is encountered while loading version.properties
	 */
	public static Version getVersion(ClassLoader classloader) throws Exception {
		return getVersion(null, classloader);
	}
	
	
	/**
	 * Searches for version.properties files in current directory and classpath using the given classloader and returns version object representing the one found
	 * @param clazz the class that should be used to searched for the version.properites (only files from the same jar file)
	 * @param classloader the classloader that should be used to search for version.properties
	 * @return a version object representing the version.properties found and null if nothing was found
	 * @throws Exception if an error is encountered while loading version.properties
	 */
	public static Version getVersion(Class<?> clazz, ClassLoader classloader) throws Exception {
		return load("version.properties", clazz, classloader);
	}
	
	
	/**
	 * Parses a version from a given string
	 * @param text the string representation of a version
	 * @return the version object that represents the version contained in the given string
	 */
	public static Version parse(String text) {
		Version version = new Version();
		String[] tokens = text.trim().split("\\-|\\+");
		String[] numbers = tokens[0].split("\\.");
		
		if (numbers.length > 0)
			version.major = integer(numbers[0], 0);
		
		if (numbers.length > 1)
			version.minor = integer(numbers[1], 0);
		
		if (numbers.length > 2) 
			version.patch = integer(numbers[2], 0);
		
		if (tokens.length > 1 && text.contains("-"))
			version.qualifier = tokens[1];
		
		if (tokens.length > 2 || (tokens.length > 1 && text.contains("+")))
			version.metadata = tokens[tokens.length - 1];
			
		return version;
	}
	
	
	/**
	 * Parses a version from a given properties object
	 * @param properties a properties object contains the different parts of the version
	 * @return the version object that represents the version contained in the given properties object
	 */
	public static Version parse(Properties properties) {
		return new vermouth.properties.Parser().parse(properties);	
	}

	
	/**
	 * Loads a version from a given properties file name from the filesystem (if file exists) and/or the classpath
	 * @param name the name of the properties file
	 * @param classloader the classloader that should be used to find the file if the file does not exist on the filesystem
	 * @return a version object representing the version contained in the given properties file name
	 * @throws Exception if an error is encountered while trying to load the given file from the filesystem or the classpath
	 */
	public static Version load(String name) throws Exception {
		return load(name, Version.class, Version.class.getClassLoader());
	}
	
	
	/**
	 * Loads a version from a given properties file name from the filesystem (if file exists) and/or the classpath
	 * @param name the name of the properties file
	 * @param classloader the classloader that should be used to find the file if the file does not exist on the filesystem
	 * @return a version object representing the version contained in the given properties file name
	 * @throws Exception if an error is encountered while trying to load the given file from the filesystem or the classpath
	 */
	public static Version load(String name, Class<?> clazz, ClassLoader classloader) throws Exception {
		URL url = search(name, clazz, classloader);
		if (url != null)
			return load(url.openStream());
		
		File file = new File(name);
		if (file.exists() == true)
			return load(file(file));
		
		return null;
	}

	
	/**
	 * Loads a version from the given inputstream containing a properties format 
	 * @param input the inputstream containing the contents of a properties format
	 * @return a version object representing the version contained in the given inputstream
	 * @throws Exception if an error is encountered while reading from the inputstream
	 */
	public static Version load(InputStream input) throws Exception {
		return new vermouth.properties.Parser().parse(input);
	}	
	
	
	/**
	 * Stores a version to a given outputstream in properties format
	 * @param version the version object that should be stored
	 * @param output the outputstream that the version should be stored to
	 * @return the given version object
	 * @throws Exception if an error is encountered while storing
	 */
	public static Version store(Version version, OutputStream output) throws Exception {
		new vermouth.properties.Serializer().serialize(version, output);
		return version;
	}
	
	
	/**
	 * Stores a version to a given file in properties format
	 * @param version the version object that should be stored
	 * @param file the file that the version should be stored to
	 * @return the given version object
	 * @throws Exception if an error is encountered while storing
	 */
	public static Version store(Version version, File file) throws Exception {
		return store(version, new FileOutputStream(file));
	}
	
	
	/**
	 * Stores a version to a given file in properties format
	 * @param version the version object that should be stored
	 * @param file the name of the file that the version should be stored to
	 * @return the given version object
	 * @throws Exception if an error is encountered while storing
	 */
	public static Version store(Version version, String file) throws Exception {
		return store(version, new File(file));
	}
	
	
	/**
	 * Determines which version of the two given versions is greater
	 * @param base the base version to compare
	 * @param other the other version to compare
	 * @return the greater of the two versions or base if they are equal
	 */
	public static Version greater(Version base, Version other) {
		return other.isGreater(base) ? other : base;
	}
	
	
	/**
	 * Determines which version of the two given versions is lesser
	 * @param base the base version to compare
	 * @param other the other version to compare
	 * @return the lesser of the two versions or base if they are equal
	 */
	public static Version lesser(Version base, Version other) {
		return other.isLesser(base) ? other : base;
	}
	
	
	/**
	 * Gets a inputstream for a given file
	 * @param file the file that the inputstream should be created
	 * @return a InputStream reading from the given file
	 */
	protected static InputStream file(File file) {
		try {
			return new FileInputStream(file);
		} catch (Exception e) {
			return new ByteArrayInputStream(new byte[0]);
		}
	}
	
	
	/**
	 * Searches for files with a given name in a given class context (same jar file) using a given classloader
	 * @param name the name of the file that should be searched for
	 * @param context the class that should be used to limit the search (same jar file) (null for all classpath)
	 * @param classloader the classloader that should be used to search for files with the given name
	 * @return the URL representing the first file with the matching name in the context of the given class
	 * @throws Exception if an error is encountered while searching
	 */
	protected static URL search(String name, Class<?> context, ClassLoader classloader) throws Exception {
		if (context == null)
			return classloader.getResource(name);
		
		Enumeration<URL> urls = classloader.getResources(name);
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			if (url.toString().contains(context.getProtectionDomain().getCodeSource().getLocation().toString()))
				return url;
		}
		
		return null;
	}
	
	
	/**
	 * Gets the class of the code that called the current method
	 * @return the Class representing the code that call the current method
	 * @throws Exception if an error is encountered while determining calling class
	 */
	protected static Class<?> getCallingClass() throws Exception {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i=1; i< elements.length; i++) {
            StackTraceElement element = elements[i];
            if (element.getClassName().equals(Version.class.getName()) == false && element.getClassName().indexOf("java.lang.Thread") != 0)
            	return ClassLoader.getSystemClassLoader().loadClass(element.getClassName());
        }
        
        return Version.class;
	}
	
	
	/**
	 * Compares to arrays of strings that represent the qualifier of a two versions
	 * @param base the array of strings representing the qualifier of the base version
	 * @param other the array of string representing the qaulifier of the othe version
	 * @return an integer where 1 means the current version is greater than given version, -1 means the given version is greater, and 0 if they are equal
	 */
	protected Integer compare(String[] base, String[] other) {
		if (base.length == 0 && other.length > 0) return 1;
		if (base.length > 0 && other.length == 0) return -1;
				
		for (int i = 0; i < Math.min(base.length, other.length); i++) {
			int compared = compare(base[i], other[i]);
			if (compared != 0)
				return compared;
		}
		
		if (base.length > 0 && other.length > 0 && base.length != other.length) return (base.length > other.length) ? 1 : -1;
		return 0;
	}
	
	
	/**
	 * Compares the segments of qualifiers from two version
	 * @param base a segment of the qualifier from the base verison
	 * @param other the corresponding segment of the qualifier from the othe version
	 * @return an integer where 1 means the current version is greater than given version, -1 means the given version is greater, and 0 if they are equal
	 */
	protected Integer compare(String base, String other) {
		if (base.matches("[0-9]+") && other.matches("[0-9]+") == false) return -1;
		if (base.matches("[0-9]+") == false && other.matches("[0-9]+")) return 1;
		if (base.matches("[0-9]+") && other.matches("[0-9]+")) return new Integer(integer(base, 0)).compareTo(integer(other, 0));
		
		int compared = base.compareTo(other);
		if (compared == 0)
			return 0;
		
		return compared > 0 ? 1 : -1;
	}
	
		
	/**
	 * Parse an integer from a given string
	 * @param value the string containing the integer value that should be parsed
	 * @param defaultValue the default value that should be returned if the given string does not contain a valid integer value
	 * @return an integer representing the value parsed from the string or the given default value if an error is encountered
	 */
	private static int integer(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}
}