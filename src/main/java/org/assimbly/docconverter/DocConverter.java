package org.assimbly.docconverter;

import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.MappingIterator;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvReadFeature;
import tools.jackson.dataformat.xml.XmlMapper;
import tools.jackson.dataformat.xml.XmlReadFeature;
import tools.jackson.dataformat.yaml.YAMLMapper;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Generic utility class to convert between XML, JSON, YAML, CSV and String.
 * Jackson tree model is the intermediate representation for format conversions.
 */
public final class DocConverter {

	private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

	private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();

	private static final XmlMapper XML_MAPPER = XmlMapper.builder()
			.defaultUseWrapper(false)
			.nameForTextElement("value")
			.enable(XmlReadFeature.WRAP_ROOT_ELEMENT_NAME)
			.build();

	private static final CsvMapper CSV_MAPPER = CsvMapper.builder()
			.enable(CsvReadFeature.WRAP_AS_ARRAY)
			.enable(CsvReadFeature.SKIP_EMPTY_LINES)
			.build();

	private static final ObjectReader CSV_ROW_READER = CSV_MAPPER.readerFor(String[].class);

	private static final TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newDefaultInstance();

	private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = createSecureDocumentBuilderFactory();

	private static final int INITIAL_BUFFER_SIZE = 8192;

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
		try (InputStream in = inputStream) {
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
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
		return newDocumentBuilder().parse(new InputSource(new StringReader(xmlString)));
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
		return newDocumentBuilder().parse(new InputSource(new StringReader(string))).getDocumentElement();
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
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	/**
	 * Converts a URI to a String
	 * @param uri java.net.URI
	 * @return String
	 * @throws Exception generic exception
	 */
	public static String convertUriToString(URI uri) throws Exception {
		return convertURLToString(uri.toURL());
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
			return newDocumentBuilder().parse(stream);
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
		StringWriter writer = new StringWriter(INITIAL_BUFFER_SIZE);
		reader.transferTo(writer);
		return writer.toString();
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
	 */
	public static String convertObjectToJSONString(Object object) {
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
		if (xml == null || xml.isEmpty()) {
			return "{}";
		}
		return JSON_MAPPER.writeValueAsString(XML_MAPPER.readTree(xml));
	}

	/**
	 * Converts XML to YAML (as String)
	 * @param xml as String
	 * @return yaml as String
	 */
	public static String convertXmlToYaml(String xml) {
		return YAML_MAPPER.writeValueAsString(XML_MAPPER.readTree(xml));
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
		return treeToCsv(XML_MAPPER.readTree(xml));
	}

	/**
	 * Converts JSON to XML (as String)
	 * @param json as String
	 * @return xml as String
	 */
	public static String convertJsonToXml(String json) {
		if (json == null || json.isBlank()) {
			return "";
		}
		return treeToXml(JSON_MAPPER.readTree(json));
	}

	/**
	 * Converts JSON to YAML (as String)
	 * @param json as String
	 * @return yaml as String
	 */
	public static String convertJsonToYaml(String json) {
		return YAML_MAPPER.writeValueAsString(JSON_MAPPER.readTree(json));
	}

	/**
	 * Converts JSON to CSV (as String)
	 * @param json as String
	 * @return csv as String
	 * @throws Exception generic exception
	 */
	public static String convertJsonToCsv(String json) throws Exception {
		if (json == null || json.isBlank()) {
			return "";
		}
		return treeToCsv(JSON_MAPPER.readTree(json));
	}

	/**
	 * Converts CSV to XML (as String)
	 * @param csv as String
	 * @return xml as String
	 */
	public static String convertCsvToXml(String csv) {
		StringBuilder sb = new StringBuilder(INITIAL_BUFFER_SIZE);
		sb.append("<?xml version='1.0' encoding='UTF-8'?><rows>");
		try (MappingIterator<String[]> rows = CSV_ROW_READER.readValues(nullToEmpty(csv))) {
			while (rows.hasNextValue()) {
				String[] row = rows.nextValue();
				if (isBlankRow(row)) {
					continue;
				}
				sb.append("<row>");
				for (String col : row) {
					sb.append("<item>");
					appendEscaped(sb, col != null ? col : "");
					sb.append("</item>");
				}
				sb.append("</row>");
			}
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
		return JSON_MAPPER.writeValueAsString(csvToTree(csv));
	}

	/**
	 * Converts CSV to YAML (as String)
	 * @param csv as String
	 * @return yaml as String
	 */
	public static String convertCsvToYaml(String csv) {
		return YAML_MAPPER.writeValueAsString(csvToTree(csv));
	}

	/**
	 * Converts YAML to XML (as String)
	 * @param yaml as String
	 * @return xml as String
	 */
	public static String convertYamlToXml(String yaml) {
		return treeToXml(YAML_MAPPER.readTree(yaml));
	}

	/**
	 * Converts YAML to JSON (as String)
	 * @param yaml as String
	 * @return json as String
	 */
	public static String convertYamlToJson(String yaml) {
		return JSON_MAPPER.writeValueAsString(YAML_MAPPER.readTree(yaml));
	}

	/**
	 * Converts YAML to CSV (as String)
	 * @param yaml as String
	 * @return csv as String
	 * @throws Exception generic exception
	 */
	public static String convertYamlToCsv(String yaml) throws Exception {
		return treeToCsv(YAML_MAPPER.readTree(yaml));
	}

	// ─────────────────────────────────────────────
	// Format detection
	// ─────────────────────────────────────────────

	/**
	 * Checks if String is a XML
	 * @param xml as String
	 * @return check as boolean
	 */
	public static boolean isXML(String xml) {
		if (xml == null || xml.isBlank()) return false;
		String t = xml.stripLeading();
		if (!t.startsWith("<")) return false;
		try {
			XML_MAPPER.readTree(xml);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Checks if String is a JSON
	 * @param json as String
	 * @return check as boolean
	 */
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

	/**
	 * Checks if String is a YAML
	 * @param yaml as String
	 * @return check as boolean
	 */
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

	private static String treeToXml(JsonNode node) {
		if (node == null || node.isNull() || node.isMissingNode()) {
			return "";
		}
		if (node.isArray()) {
			ObjectNode wrapper = JSON_MAPPER.createObjectNode();
			ObjectNode arrayList = JSON_MAPPER.createObjectNode();
			arrayList.set("item", node);
			wrapper.set("ArrayList", arrayList);
			return XML_MAPPER.writeValueAsString(wrapper);
		}
		return XML_MAPPER.writeValueAsString(node);
	}

	private static JsonNode csvToTree(String csv) {
		ArrayNode rowArray = JSON_MAPPER.createArrayNode();
		try (MappingIterator<String[]> rows = CSV_ROW_READER.readValues(nullToEmpty(csv))) {
			while (rows.hasNextValue()) {
				String[] row = rows.nextValue();
				if (isBlankRow(row)) {
					continue;
				}
				ObjectNode rowNode = JSON_MAPPER.createObjectNode();
				ArrayNode items = JSON_MAPPER.createArrayNode();
				for (String col : row) {
					items.add(col != null ? col : "");
				}
				rowNode.set("item", items);
				rowArray.add(rowNode);
			}
		}
		ObjectNode rowsNode = JSON_MAPPER.createObjectNode();
		rowsNode.set("row", rowArray);
		ObjectNode root = JSON_MAPPER.createObjectNode();
		root.set("rows", rowsNode);
		return root;
	}

	private static String treeToCsv(JsonNode tree) {
		JsonNode rows = extractCsvRows(tree);
		if (rows == null || rows.isMissingNode() || rows.isNull()) {
			return "";
		}

		StringBuilder sb = new StringBuilder(INITIAL_BUFFER_SIZE);
		if (rows.isArray()) {
			for (JsonNode row : rows) {
				appendCsvRow(sb, row);
			}
		} else {
			appendCsvRow(sb, rows);
		}

		int length = sb.length();
		while (length > 0 && (sb.charAt(length - 1) == '\n' || sb.charAt(length - 1) == '\r')) {
			length--;
		}
		sb.setLength(length);
		return sb.toString();
	}

	private static JsonNode extractCsvRows(JsonNode tree) {
		if (tree == null) {
			return null;
		}
		JsonNode rowsWrapper = tree.get("rows");
		if (rowsWrapper != null && !rowsWrapper.isNull()) {
			JsonNode row = rowsWrapper.get("row");
			if (row != null) {
				return row;
			}
		}
		return tree.get("row");
	}

	private static void appendCsvRow(StringBuilder sb, JsonNode row) {
		JsonNode items = row.get("item");
		if (items == null || items.isNull()) {
			return;
		}
		if (items.isArray()) {
			for (int i = 0, size = items.size(); i < size; i++) {
				if (i > 0) {
					sb.append(',');
				}
				appendCsvField(sb, items.get(i).asString());
			}
		} else {
			appendCsvField(sb, items.asString());
		}
		sb.append('\n');
	}

	private static void appendCsvField(StringBuilder sb, String value) {
		if (value == null) {
			value = "";
		}
		boolean quote = false;
		for (int i = 0, len = value.length(); i < len; i++) {
			char c = value.charAt(i);
			if (c == ',' || c == '"' || c == '\n' || c == '\r') {
				quote = true;
				break;
			}
		}
		if (!quote) {
			sb.append(value);
			return;
		}
		sb.append('"');
		for (int i = 0, len = value.length(); i < len; i++) {
			char c = value.charAt(i);
			if (c == '"') {
				sb.append('"');
			}
			sb.append(c);
		}
		sb.append('"');
	}

	private static boolean isBlankRow(String[] row) {
		if (row == null || row.length == 0) {
			return true;
		}
		for (String col : row) {
			if (col != null && !col.isBlank()) {
				return false;
			}
		}
		return true;
	}

	private static String nullToEmpty(String value) {
		return value == null ? "" : value;
	}

	private static DocumentBuilder newDocumentBuilder() throws ParserConfigurationException {
		return DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
	}

	private static DocumentBuilderFactory createSecureDocumentBuilderFactory() {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		try {
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
			factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
			factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
		} catch (ParserConfigurationException e) {
			factory.setValidating(false);
			factory.setNamespaceAware(true);
		}
		factory.setXIncludeAware(false);
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
