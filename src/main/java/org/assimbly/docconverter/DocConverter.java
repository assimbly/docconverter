package org.assimbly.docconverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;

import org.json.JSONObject;
import org.json.XML;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class DocConverter {

	private static String xml;
	private static String yaml;
	private static String json;

	public static int PRETTY_PRINT_INDENT_FACTOR = 4;

	public static String convertStreamToString(InputStream is) {
		@SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static InputStream convertStringToStream(String str) throws UnsupportedEncodingException {
		return new ByteArrayInputStream(str.getBytes("UTF-8"));
	}

	public static Document convertStringToDoc(String str) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(new ByteArrayInputStream(str.getBytes()));
	}

	public static String convertDocToString(Document doc) {
		try {
			StringWriter sw = new StringWriter();
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

			transformer.transform(new DOMSource(doc), new StreamResult(sw));
			return sw.toString();
		} catch (Exception ex) {
			throw new RuntimeException("Error converting to String", ex);
		}
	}

	public static String convertUriToString(URI uri) throws Exception {

		URL url = uri.toURL(); // get URL from your uri object
		InputStream stream = url.openStream();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.parse(stream);
		
		String docAsString = convertDocToString(doc); 
		
		return docAsString;
	}

	
	public static Document convertUriToDoc(URI uri) throws Exception {

		URL url = uri.toURL(); // get URL from your uri object
		InputStream stream = url.openStream();

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		return builder.parse(stream);
	}

	public static String convertXmlToJson(String xml) {

		JSONObject xmlJSONObj = XML.toJSONObject(xml);
		String json = xmlJSONObj.toString(PRETTY_PRINT_INDENT_FACTOR);

		return json;
	}

	public static String convertXMLtoYAML(String xml) throws IOException {

		JSONObject xmlJSONObj = XML.toJSONObject(xml);
		json = xmlJSONObj.toString();

		JsonNode jsonNode = new ObjectMapper().readTree(json);
		yaml = new YAMLMapper().writeValueAsString(jsonNode);

		return yaml;
	}

	
	public static String convertJsonToXml(String json) {

		JSONObject jsonObj = new JSONObject(json);
		String xml = XML.toString(jsonObj);

		return xml;

	}

	public static String convertJSONtoYAML(String json) throws IOException {

		JsonNode jsonNodeTree = new ObjectMapper().readTree(json);
		yaml = new YAMLMapper().writeValueAsString(jsonNodeTree);

		return yaml;
	}
	
	public static String convertYAMLtoXML(String yaml) throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);

		ObjectMapper jsonWriter = new ObjectMapper();
		json = jsonWriter.writeValueAsString(obj);

		JSONObject jsonObj = new JSONObject(json);
		xml = XML.toString(jsonObj);

		return xml;
	}

	public static String convertYAMLtoJSON(String yaml) throws JsonParseException, JsonMappingException, IOException {

		ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
		Object obj = yamlReader.readValue(yaml, Object.class);

		ObjectMapper jsonWriter = new ObjectMapper();
		json = jsonWriter.writeValueAsString(obj);

		return json;
	}	

}
