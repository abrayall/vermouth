package vermouth;

public class Version {
	
	private int major = 0;
	private int minor = 0;
	private int patch = 0;
	
	private String metadata = "";
	private String qualifier = "";
	
	public Version() {}
	public Version(int major, int minor, int patch) {
		this(major, minor, patch, "", "");
	}
	
	public Version(int major, int minor, int patch, String qualifier, String metadata) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.qualifier = qualifier;
		this.metadata = metadata;
	}
	
	public int getMajor() {
		return this.major;
	}
	
	public int getMinor() {
		return this.minor;
	}
	
	public int getPatch() {
		return this.patch;
	}
	
	public String getQualifier() {
		return this.qualifier;
	}
	
	public String getPrerelease() {
		return this.getQualifier();
	}
	
	public String getMetadata() {
		return this.metadata;
	}
		
	public boolean equals(Object object) {
		if (Version.class.isInstance(object))
			return this.equals((Version) object);
		
		return false;
	}

	public boolean equals(String version) {
		return this.toString().equals(version);
	}

	public boolean equals(Version version) {
		return this.equals(version.toString());
	}
	
	public int compare(String version) {
		return this.compare(parse(version));
	}
	
	public int compare(Version version) {
		if (this.major != version.major) return this.major > version.major ? 1 : -1;
		if (this.minor != version.minor) return this.minor > version.minor ? 1 : -1;
		if (this.patch != version.patch) return this.patch > version.patch ? 1 : -1;
		if (this.qualifier.equals("") == true && version.qualifier.equals("") == false) return 1;
		if (this.qualifier.equals("") == false && version.qualifier.equals("") == true) return -1;
		return 0;
	}
	
	public boolean isGreater(String version) {
		return this.isGreater(parse(version));
	}
	
	public boolean isGreater(Version version) {
		return this.compare(version) == 1;
	}
	
	public boolean isLesser(String version) {
		return this.isLesser(parse(version));
	}
	
	public boolean isLesser(Version version) {
		return this.compare(version) == -1;
	}
	
	public String toString() {
		return this.major + "." + this.minor + "." + this.patch + 
			(this.qualifier.equals("") == false ? "-" + this.qualifier : "") + 
			(this.metadata.equals("") == false ? "+" + this.metadata : "");
	}
	
	public static Version parse(String text) {
		Version version = new Version();
		String[] tokens = text.split("\\.|\\-|\\+");
		
		if (tokens.length > 0)
			version.major = integer(tokens[0], 0);
		
		if (tokens.length > 1)
			version.minor = integer(tokens[1], 0);
		
		if (tokens.length > 2) 
			version.patch = integer(tokens[2], 0);
		
		if (tokens.length > 3 && text.contains("-"))
			version.qualifier = tokens[3];
		
		if (tokens.length > 4 || (tokens.length > 3 && text.contains("+")))
			version.metadata = tokens[tokens.length - 1];
			
		return version;
	}
	
	private static int integer(String value, int defaultValue) {
		try {
			return Integer.parseInt(value);
		} catch (Exception e) {
			return defaultValue;
		}
	}
	
	/*
	private static String load(String name) {
		File file = new File(name);
		if (file.exists() == true)
			return load(file(file));
		else
			return load(Version.class.getClassLoader().getResourceAsStream(name));
	}

	private static String load(InputStream input) {
		Properties properties = properties(input);
		return properties.getProperty("major", "0") + "." + properties.getProperty("minor", "0") + "." + properties.getProperty("maintenance", "0") + "." + properties.getProperty("revision", "0");
	}
	
	private static InputStream file(File file) {
		try {
			return new FileInputStream(file.toFile());
		} catch (Exception e) {
			return new ByteArrayInputStream(new byte[0]);
		}
	} */
}

