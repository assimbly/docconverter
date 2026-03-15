package org.assimbly.docconverter;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;
import org.apache.commons.io.IOUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

/**
 * Generic utility class to convert between XML, JSON, YAML, CSV and String.
 */
public final class DocConverter {

	// ─────────────────────────────────────────────
	// Cached, thread-safe singletons
	// ─────────────────────────────────────────────

	private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

	private static final XmlMapper XML_MAPPER = new XmlMapper(createXmlModule());

	private static final YAMLMapper YAML_MAPPER = new YAMLMapper();

	private static final XmlMapper XML_MAPPER_PLAIN = new XmlMapper();

	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newDefaultInstance();

	private static final ObjectMapper YAML_READER = new ObjectMapper(new YAMLFactory());

	private static final int INITIAL_BUFFER_SIZE = 8192;

	private static JacksonXmlModule createXmlModule() {
		JacksonXmlModule m = new JacksonXmlModule();
		m.setDefaultUseWrapper(false);
		m.setXMLTextElementName("value");
		return m;
	}

	// Private constructor – utility class
	private DocConverter() {}

	// ─────────────────────────────────────────────
	// Stream / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a Stream to a String
	 * @param inputStream InputStream
	 * @return String
	 */
	public static String convertStreamToString(InputStream inputStream) {
		try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8).useDelimiter("\\A")) {
			return scanner.hasNext() ? scanner.next() : "";
		}
	}

	/**
	 * Converts a String to a Stream
	 * @param string String
	 * @return InputStream
	 */
	public static InputStream convertStringToStream(String string) {
		return new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8));
	}

	// ─────────────────────────────────────────────
	// Document / String
	// ─────────────────────────────────────────────

	/**
	 * Converts an org.w3c.dom.Document (XML) to a String
	 * @param document org.w3c.dom.Document XML document
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertDocToString(Document document) throws Exception {
		Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
		StringWriter stringWriter = new StringWriter();
		transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
		return stringWriter.toString();
	}

	/**
	 * Converts a String to an org.w3c.dom.Document (XML)
	 * @param xmlString String
	 * @return Document org.w3c.dom.Document XML document
	 * @throws Exception generic exception
	 */
	public static Document convertStringToDoc(String xmlString) throws Exception {
		DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
		DocumentBuilder builder = factory.newDocumentBuilder();
		return builder.parse(new InputSource(new StringReader(xmlString)));
	}

	// ─────────────────────────────────────────────
	// Node / String
	// ─────────────────────────────────────────────

	/**
	 * Converts an org.w3c.dom.Node to a String
	 * @param node Node org.w3c.dom.Node
	 * @return String
	 */
	public static String convertNodeToString(Node node) {
		DOMImplementationLS lsImpl = (DOMImplementationLS)
				node.getOwnerDocument().getImplementation().getFeature("LS", "3.0");
		LSSerializer lsSerializer = lsImpl.createLSSerializer();
		DOMConfiguration config = lsSerializer.getDomConfig();
		config.setParameter("xml-declaration", Boolean.FALSE);
		return lsSerializer.writeToString(node);
	}

	/**
	 * Converts a String to an org.w3c.dom.Node
	 * @param string XML node as string, e.g. {@code <node>value</node>}
	 * @return Node org.w3c.dom.Node
	 * @throws Exception generic exception
	 */
	public static Node convertStringToNode(String string) throws Exception {
		return DocumentBuilderFactory
				.newDefaultInstance()
				.newDocumentBuilder()
				.parse(new ByteArrayInputStream(string.getBytes(StandardCharsets.UTF_8)))
				.getDocumentElement();
	}

	// ─────────────────────────────────────────────
	// URL / URI / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a URL to a String
	 * @param url java.net.URL
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertURLToString(URL url) throws Exception {
		try (InputStream stream = url.openStream()) {
			DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
			Document doc = factory.newDocumentBuilder().parse(stream);
			return convertDocToString(doc);
		}
	}

	/**
	 * Converts a URI to a String
	 * @param uri java.net.URI
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertUriToString(URI uri) throws Exception {
		URL url = uri.toURL();
		try (InputStream stream = url.openStream()) {
			DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
			Document doc = factory.newDocumentBuilder().parse(stream);
			return convertDocToString(doc);
		}
	}

	/**
	 * Converts a URI to an org.w3c.dom.Document (XML)
	 * @param uri java.net.URI
	 * @return Document org.w3c.dom.Document XML document
	 * @throws Exception generic exception
	 */
	public static Document convertUriToDoc(URI uri) throws Exception {
		URL url = uri.toURL();
		try (InputStream stream = url.openStream()) {
			DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
			return factory.newDocumentBuilder().parse(stream);
		}
	}

	// ─────────────────────────────────────────────
	// File / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a file to String (UTF-8)
	 * @param path path as String
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertFileToString(String path) throws Exception {
		return Files.readString(Paths.get(path));
	}

	/**
	 * Converts a File to String with explicit encoding
	 * @param path path as String
	 * @param encoding Charset (defaults to UTF-8 when null)
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertFileToString(String path, Charset encoding) throws Exception {
		if (encoding == null) {
			encoding = StandardCharsets.UTF_8;
		}
		return Files.readString(Paths.get(path), encoding);
	}

	/**
	 * Converts a String to a File
	 * @param path path as String
	 * @param content content as String
	 * @throws Exception generic exception
	 */
	public static void convertStringToFile(String path, String content) throws Exception {
		Files.writeString(Paths.get(path), content, StandardOpenOption.CREATE);
	}

	/**
	 * Converts a File to a URI
	 * @param file File object
	 * @return URI
	 */
	public static URI convertFileToURI(File file) {
		return file.toURI();
	}

	/**
	 * Converts a File to a URL
	 * @param file File object
	 * @return URL
	 * @throws Exception generic exception
	 */
	public static URL convertFileToURL(File file) throws Exception {
		return file.toURI().toURL();
	}

	// ─────────────────────────────────────────────
	// Source / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a String to a Source
	 * @param string String object
	 * @return Source
	 */
	public static Source convertStringToSource(String string) {
		return new StreamSource(new StringReader(string));
	}

	// ─────────────────────────────────────────────
	// List / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a List to a comma-separated String
	 * @param list List of strings
	 * @return String
	 */
	public static String convertListToString(List<String> list) {
		return String.join(",", list);
	}

	/**
	 * Converts a comma-separated String to a List
	 * @param commaSeparatedString comma-separated String
	 * @return List
	 */
	public static List<String> convertStringToList(String commaSeparatedString) {
		return new ArrayList<>(Arrays.asList(commaSeparatedString.split(",")));
	}

	// ─────────────────────────────────────────────
	// Reader / String
	// ─────────────────────────────────────────────

	/**
	 * Converts a String to a Reader
	 * @param string String object
	 * @return Reader
	 */
	public static Reader convertStringToReader(String string) {
		return new StringReader(string);
	}

	/**
	 * Converts a Reader to a String
	 * @param reader java.io.Reader
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertReaderToString(Reader reader) throws Exception {
		return IOUtils.toString(reader);
	}

	// ─────────────────────────────────────────────
	// Object conversions
	// ─────────────────────────────────────────────

	/**
	 * Converts an Object to a String
	 * @param object Generic Object
	 * @return String
	 */
	public static String convertObjectToString(Object object) {
		return String.valueOf(object);
	}

	/**
	 * Converts an Object to a JSON String
	 * @param object Generic Object
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertObjectToJSONString(Object object) throws Exception {
		return JSON_MAPPER.writeValueAsString(object);
	}

	// ─────────────────────────────────────────────
	// Data-format conversions
	// ─────────────────────────────────────────────

	/**
	 * Converts XML to JSON (as String)
	 * @param xml as String
	 * @return json as String
	 * @throws Exception generic exception
	 */
	public static String convertXmlToJson(String xml) throws Exception {
		String xmlWithoutDeclaration = removeXmlDeclaration(xml);

		if (!xmlWithoutDeclaration.startsWith("<ObjectNode>")) {
			xmlWithoutDeclaration = "<ObjectNode>" + xmlWithoutDeclaration + "</ObjectNode>";
		}

		JsonNode node = XML_MAPPER.readTree(xmlWithoutDeclaration.getBytes(StandardCharsets.UTF_8));
		return JSON_MAPPER.writeValueAsString(node);
	}

	/**
	 * Converts XML to YAML (as String)
	 * @param xml as String
	 * @return yaml as String
	 * @throws Exception generic exception
	 */
	public static String convertXmlToYaml(String xml) throws Exception {
		JSONObject xmlJSONObj = XML.toJSONObject(xml);
		JsonNode jsonNode = JSON_MAPPER.readTree(xmlJSONObj.toString());
		return YAML_MAPPER.writeValueAsString(jsonNode);
	}

	/**
	 * Converts XML to CSV (as String)
	 * @param xml as String
	 * @return csv as String
	 * @throws Exception generic exception
	 */
	public static String convertXmlToCsv(String xml) throws Exception {
		if (xml == null || xml.isEmpty()) {
			return "";
		}

		StringWriter stringWriter = new StringWriter();

		try (CSVWriter csvWriter = new CSVWriter(stringWriter,
				ICSVWriter.DEFAULT_SEPARATOR,
				ICSVWriter.DEFAULT_QUOTE_CHARACTER,
				ICSVWriter.DEFAULT_ESCAPE_CHARACTER,
				ICSVWriter.DEFAULT_LINE_END)) {

			JsonNode rootNode = XML_MAPPER_PLAIN.readTree(xml.getBytes());
			JsonNode rows = rootNode.get("row");

			if (rows != null) {
				if (rows.isArray()) {
					for (JsonNode row : rows) {
						writeRow(csvWriter, row);
					}
				} else {
					writeRow(csvWriter, rows);
				}
			}
		}

		return stringWriter.toString().trim();
	}

	private static void writeRow(CSVWriter csvWriter, JsonNode row) {
		JsonNode items = row.get("item");
		if (items != null && items.isArray()) {
			List<String> data = new ArrayList<>();
			for (JsonNode item : items) {
				data.add(item.asText());
			}
			csvWriter.writeNext(data.toArray(new String[0]), false);
		}
	}

	/**
	 * Converts JSON to XML (as String)
	 * @param json as String
	 * @return xml as String
	 * @throws Exception generic exception
	 */
	public static String convertJsonToXml(String json) throws Exception {
		// Pre-check for empty or null input
		if (json == null || json.trim().isEmpty()) {
			return "";
		}

		// Use String.charAt(0) for faster prefix check
		boolean isArray = json.charAt(0) == '[';

		Object jsonObject;
		if (isArray) {
			jsonObject = new JSONArray(json).toList();
		} else {
			jsonObject = JSON_MAPPER.readTree(json);
		}

		// Write XML in one go, avoiding intermediate String manipulation
		String xml = XML_MAPPER_PLAIN.writeValueAsString(jsonObject);

		// Only process if not an array and contains ObjectNode
		if (!isArray && xml.contains("<ObjectNode>")) {
			int start = xml.indexOf("<ObjectNode>") + "<ObjectNode>".length();
			int end = xml.indexOf("</ObjectNode>");
			if (start > 0 && end > start) {
				xml = xml.substring(start, end);
			}
		}

		return xml;
	}

	/**
	 * Converts JSON to YAML (as String)
	 * @param json as String
	 * @return yaml as String
s	 * @throws Exception generic exception
	 */
	public static String convertJsonToYaml(String json) throws Exception {
		JsonNode jsonNodeTree = JSON_MAPPER.readTree(json);
		return YAML_MAPPER.writeValueAsString(jsonNodeTree);
	}

	/**
	 * Converts JSON to CSV (as String)
	 * @param json as String
	 * @return csv as String
	 * @throws Exception generic exception
	 */
	public static String convertJsonToCsv(String json) throws Exception {
		String xml = convertJsonToXml(json);
		return convertXmlToCsv(xml);
	}

	/**
	 * Converts CSV to XML (as String)
	 * @param csv as String
	 * @return xml as String
	 */
	public static String convertCsvToXml(String csv) {
		CsvParserSettings settings = new CsvParserSettings();
		settings.detectFormatAutomatically();
		CsvParser parser = new CsvParser(settings);

		StringBuilder sb = new StringBuilder(INITIAL_BUFFER_SIZE);
		sb.append("<?xml version='1.0' encoding='UTF-8'?><rows>");

		try(StringReader stringReader = new StringReader(csv)) {
			parser.beginParsing(stringReader);
			String[] row;
			while ((row = parser.parseNext()) != null) {
				sb.append("<row>");
				for (String col : row) {
					sb.append("<item>");
					appendEscaped(sb, col != null ? col : "");
					sb.append("</item>");
				}
				sb.append("</row>");
			}
		} finally {
			parser.stopParsing();
		}

		sb.append("</rows>");
		return sb.toString();
	}

	/**
	 * Converts CSV to JSON (as String)
	 * @param csv as String
	 * @return json as String
	 * @throws Exception generic exception
	 */
	public static String convertCsvToJson(String csv) throws Exception {
		String xml = convertCsvToXml(csv);
		return convertXmlToJson(xml);
	}

	/**
	 * Converts CSV to YAML (as String)
	 * @param csv as String
	 * @return yaml as String
	 * @throws Exception generic exception
	 */
	public static String convertCsvToYaml(String csv) throws Exception {
		String xml = convertCsvToXml(csv);
		return convertXmlToYaml(xml);
	}

	/**
	 * Converts YAML to XML (as String)
	 * @param yaml as String
	 * @return xml as String
	 * @throws Exception generic exception
	 */
	public static String convertYamlToXml(String yaml) throws Exception {
		String json = convertYamlToJson(yaml);
		return convertJsonToXml(json);
	}

	/**
	 * Converts YAML to JSON (as String)
	 * @param yaml as String
	 * @return json as String
	 * @throws Exception generic exception
	 */
	public static String convertYamlToJson(String yaml) throws Exception {
		Object obj = YAML_READER.readValue(yaml, Object.class);
		return JSON_MAPPER.writeValueAsString(obj);
	}

	/**
	 * Converts YAML to CSV (as String)
	 * @param yaml as String
	 * @return csv as String
	 * @throws Exception generic exception
	 */
	public static String convertYamlToCsv(String yaml) throws Exception {
		String xml = convertYamlToXml(yaml);
		return convertXmlToCsv(xml);
	}

	// ─────────────────────────────────────────────
	// Format detection
	// ─────────────────────────────────────────────

	public static boolean isXML(String xml) {
		if (xml == null || xml.isBlank()) return false;
		String t = xml.stripLeading();
		if (!t.startsWith("<")) return false;
		try {
			SAXParserFactory.newDefaultInstance()
					.newSAXParser()
					.getXMLReader()
					.parse(new InputSource(new StringReader(xml)));
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isJson(String json) {
		if (json == null || json.isBlank()) return false;
		String t = json.stripLeading();
		if (!t.startsWith("{") && !t.startsWith("[")) return false;
		try {
			JSON_MAPPER.readTree(json);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isYaml(String yaml) {
		if (yaml == null || yaml.isBlank()) return false;
		try {
			YAML_MAPPER.readTree(yaml);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	// ─────────────────────────────────────────────
	// Internal helpers
	// ─────────────────────────────────────────────

	private static String removeXmlDeclaration(String xml) {
		return xml.replaceFirst("^<\\?xml.*?\\?>", "").trim();
	}

	private static DocumentBuilderFactory createSecureDocumentBuilderFactory() {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			// Primary defense: block all DOCTYPE declarations entirely
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			// Belt-and-suspenders: disable external general and parameter entities individually
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			// Disable external DTD loading
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e) {
			// Fallback for non-Xerces parsers that don't support the above features
			factory.setValidating(false);
			factory.setNamespaceAware(true);
		}
		// Disable XInclude processing
		factory.setXIncludeAware(false);
		// Disable entity expansion
		factory.setExpandEntityReferences(false);
		return factory;
	}

	private static void appendEscaped(StringBuilder sb, String value) {
		for (int i = 0, len = value.length(); i < len; i++) {
			char c = value.charAt(i);
			switch (c) {
				case '<'  -> sb.append("&lt;");
				case '>'  -> sb.append("&gt;");
				case '&'  -> sb.append("&amp;");
				case '"'  -> sb.append("&quot;");
				case '\'' -> sb.append("&apos;");
				default   -> sb.append(c);
			}
		}
	}

}