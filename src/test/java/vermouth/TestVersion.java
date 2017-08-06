package vermouth;

import java.util.Properties;

import javax.lang.Assert;

public class TestVersion {
	public static void main(String[] arguments) throws Exception {
		testSimpleVersions();
		testQualifiedVersions();
		testMetadataVersions();
		testComplexVersions();
		testLoad();
		testProperties();
	}
	
	public static void testSimpleVersions() throws Exception {
		Version version = Version.parse("1.11.100");
		Assert.equals(1, version.getMajor());
		Assert.equals(11, version.getMinor());
		Assert.equals(100, version.getPatch());
		Assert.equals("", version.getQualifier());
		Assert.equals("", version.getMetadata());
		
		Assert.equals(true, version.isGreater("1.10.500"));
		Assert.equals(true, version.isGreater(Version.parse("1.10.501")));
		
		Assert.equals(true, version.isLesser("1.12.0"));
		Assert.equals(true, version.isLesser(Version.parse("12.12.1200")));
		
		Assert.equals(version, version);
		Assert.equals(true, version.equals(new Version(1, 11, 100)));
		Assert.equals(true, version.equals("1.11.100"));
		Assert.equals(false, version.equals(10));
	}
	
	public static void testQualifiedVersions() throws Exception {
		Version version = Version.parse("2.1111.994-beta1");
		Assert.equals(2, version.getMajor());
		Assert.equals(1111, version.getMinor());
		Assert.equals(994, version.getPatch());
		Assert.equals("beta1", version.getQualifier());
		Assert.equals("", version.getMetadata());
		
		Assert.equals(true, version.isGreater("1.10.500"));
		Assert.equals(false, version.isGreater(Version.parse("3.10.501")));
		Assert.equals(false, version.isGreater("2.1111.994"));
		Assert.equals(false, version.isGreater("2.1111.994-beta1"));
		
		Assert.equals(true, version.isLesser("3.12.0"));
		Assert.equals(false, version.isLesser(Version.parse("1.112.1200")));
		Assert.equals(true, version.isLesser("2.1111.994"));
		Assert.equals(false, version.isLesser("2.1111.994-beta1"));
		
		Assert.equals(version, version);
		Assert.equals(false, version.equals(new Version(2, 1111, 994)));
		Assert.equals(false, version.equals("2.1111.994"));
		Assert.equals(true, version.equals("2.1111.994-beta1"));
		Assert.equals(false, version.equals(10));
	}
	
	public static void testMetadataVersions() throws Exception {
		Version version = Version.parse("2.1111.994+build1");
		Assert.equals(2, version.getMajor());
		Assert.equals(1111, version.getMinor());
		Assert.equals(994, version.getPatch());
		Assert.equals("", version.getQualifier());
		Assert.equals("build1", version.getMetadata());
		
		Assert.equals(true, version.isGreater("1.10.500"));
		Assert.equals(false, version.isGreater(Version.parse("3.10.501")));
		Assert.equals(false, version.isGreater("2.1111.994"));
		Assert.equals(true, version.isGreater("2.1111.994-beta1"));
		Assert.equals(false, version.isGreater("2.1111.994+build1"));
		
		Assert.equals(true, version.isLesser("3.12.0"));
		Assert.equals(false, version.isLesser(Version.parse("1.112.1200")));
		Assert.equals(false, version.isLesser("2.1111.994"));
		Assert.equals(false, version.isLesser("2.1111.994-beta1"));
		Assert.equals(false, version.isLesser("2.1111.994-build1"));
		
		Assert.equals(version, version);
		Assert.equals(false, version.equals(new Version(2, 1111, 994)));
		Assert.equals(false, version.equals(new Version(2, 1111, 994, "", "build0")));
		Assert.equals(true, version.equals(new Version(2, 1111, 994, "", "build1")));
		Assert.equals(false, version.equals("2.1111.994"));
		Assert.equals(false, version.equals("2.1111.994-beta1"));
		Assert.equals(false, version.equals(10));
	}
	
	public static void testComplexVersions() throws Exception {
		Version version = Version.parse("2.1111.994-beta2+build16");
		Assert.equals(2, version.getMajor());
		Assert.equals(1111, version.getMinor());
		Assert.equals(994, version.getPatch());
		Assert.equals("beta2", version.getQualifier());
		Assert.equals("build16", version.getMetadata());
		
		Assert.equals(true, version.isGreater("1.10.500"));
		Assert.equals(false, version.isGreater(Version.parse("3.10.501")));
		Assert.equals(false, version.isGreater("2.1111.994"));
		Assert.equals(false, version.isGreater("2.1111.994-beta1"));
		Assert.equals(false, version.isGreater("2.1111.994+build1"));
		Assert.equals(false, version.isGreater("2.1111.994-beta4+build1"));
		
		Assert.equals(true, version.isLesser("3.12.0"));
		Assert.equals(false, version.isLesser(Version.parse("1.112.1200")));
		Assert.equals(true, version.isLesser("2.1111.994"));
		Assert.equals(false, version.isLesser("2.1111.994-beta1"));
		Assert.equals(false, version.isLesser("2.1111.994-build1"));
		
		Assert.equals(version, version);
		Assert.equals(false, version.equals(new Version(2, 1111, 994)));
		Assert.equals(false, version.equals("2.1111.994"));
		Assert.equals(false, version.equals("2.1111.994-beta1"));
		Assert.equals(false, version.equals(10));
	}
	
	public static void testLoad() throws Exception {
		Properties properties = new Properties();
		Assert.equals("0.0.0", Version.parse(properties).toString());
		
		properties.setProperty("major", "1");
		Assert.equals("1.0.0", Version.parse(properties).toString());
		
		properties.remove("major");
		properties.setProperty("version.major", "2");
		Assert.equals("2.0.0", Version.parse(properties).toString());
		
		properties.setProperty("minor", "20");
		Assert.equals("2.20.0", Version.parse(properties).toString());
		
		properties.remove("minor");
		properties.setProperty("version.minor", "22");
		Assert.equals("2.22.0", Version.parse(properties).toString());

		properties.setProperty("patch", "33");
		Assert.equals("2.22.33", Version.parse(properties).toString());
		
		properties.remove("patch");
		properties.setProperty("version.patch", "42");
		Assert.equals("2.22.42", Version.parse(properties).toString());

		properties.setProperty("qualifier", "beta1");
		Assert.equals("2.22.42-beta1", Version.parse(properties).toString());
		
		properties.remove("qualifier");
		properties.setProperty("version.qualifier", "beta2");
		Assert.equals("2.22.42-beta2", Version.parse(properties).toString());
	
		properties.setProperty("metadata", "build7");
		Assert.equals("2.22.42-beta2+build7", Version.parse(properties).toString());
		
		properties.remove("metadata");
		properties.setProperty("version.metadata", "build8");
		Assert.equals("2.22.42-beta2+build8", Version.parse(properties).toString());
		
		properties.remove("version.qualifier");
		Assert.equals("2.22.42+build8", Version.parse(properties).toString());
	}
	
	public static void testProperties() throws Exception {
		Properties properties = Version.parse("11.101.1201-beta1+build2").properties();
		Assert.equals("11", properties.getProperty("major"));
		Assert.equals("101", properties.getProperty("minor"));
		Assert.equals("1201", properties.getProperty("patch"));
		Assert.equals("beta1", properties.getProperty("qualifier"));
		Assert.equals("beta1", properties.getProperty("prerelease"));
		Assert.equals("build2", properties.getProperty("metadata"));
	}
}
