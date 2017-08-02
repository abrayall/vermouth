package vermouth;

import javax.lang.Assert;

public class TestVersion {
	public static void main(String[] arguments) throws Exception {
		Version version = Version.parse("1.11.100");
		Assert.equals(1, version.getMajor());
		Assert.equals(11, version.getMinor());
		Assert.equals(100, version.getPatch());
		
		Assert.equals(true, version.isGreater("1.10.500"));
		Assert.equals(true, version.isGreater(Version.parse("1.10.501")));
		
		Assert.equals(true, version.isLesser("1.12.0"));
		Assert.equals(true, version.isLesser(Version.parse("12.12.1200")));
		
		Assert.equals(version, version);
		Assert.equals(true, version.equals(new Version(1, 11, 100)));
		Assert.equals(true, version.equals("1.11.100"));
		Assert.equals(false, version.equals(10));
	}
}
