package vermouth.maven;

import java.io.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import vermouth.Version;

public class Parser {
	public Version parse(InputStream input) throws Exception {
		return Version.parse(node(document(input), "/project/version/text()").getNodeValue());
	}
	
	public Version parse(File file) throws Exception {
		return parse(new FileInputStream(file));
	}
	
	public Version parse(String xml) throws Exception {
		return parse(new ByteArrayInputStream(xml.getBytes()));
	}
	
	protected Document document(InputStream input) throws Exception {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
	}
	
	protected Node node(Document document, String xpath) throws Exception {
		return (Node) XPathFactory.newInstance().newXPath().compile(xpath).evaluate(document, XPathConstants.NODE);
	}
	
	public static void main(String[] arguments) throws Exception {
		System.out.println(new Parser().parse(new File("pom.xml")));
	}
}
