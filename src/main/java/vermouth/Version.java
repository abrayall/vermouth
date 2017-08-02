package vermouth;


public class Version {
	
	private int major = 0;
	private int minor = 0;
	private int patch = 0;
	
	public Version() {}
	public Version(int major, int minor, int patch) {
		this.major = major;
		this.minor = minor;
		this.patch = patch;
	}
	
	public int getMajor() {
		return major;
	}
	
	public int getMinor() {
		return minor;
	}
	
	public int getPatch() {
		return patch;
	}
		
	public boolean equals(Object object) {
		if (Version.class.isInstance(object))
			return this.equals((Version) object);
		
		return false;
	}

	public boolean equals(String version) {
		return this.compare(version) == 0;
	}

	public boolean equals(Version version) {
		return this.compare(version) == 0;
	}
	
	public int compare(String version) {
		return this.compare(parse(version));
	}
	
	public int compare(Version version) {
		if (this.major != version.major) return this.major > version.major ? 1 : -1;
		if (this.minor != version.minor) return this.minor > version.minor ? 1 : -1;
		if (this.patch != version.patch) return this.patch > version.patch ? 1 : -1;
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
		return this.major + "." + this.minor + "." + this.patch;
	}
	
//	public static Version getVersion() {
//		return parse(load("version.properties"));
//	}
	
	public static Version parse(String text) {
		Version version = new Version();
		String[] tokens = text.split("\\.");
		
		if (tokens.length > 0)
			version.major = integer(tokens[0], 0);
		
		if (tokens.length > 1)
			version.minor = integer(tokens[1], 0);
		
		if (tokens.length > 2)
			version.patch = integer(tokens[2], 0);

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

