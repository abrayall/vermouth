package vermouth.maven;

import java.io.File;

import javax.lang.Assert;

public class TestParser {
	public static void main(String[] arguments) throws Exception {
		testParser();
	}
	
	public static void testParser() throws Exception {
		Assert.equals("0.5.0", new Parser().parse(new File("pom.xml")).toString());
	}
}
