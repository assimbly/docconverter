package org.assimbly.docconverter;

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

/**
 * Static utility for converting between XML, JSON, YAML and CSV.
 * <p>
 * Format conversions use Jackson's tree model as the intermediate representation.
 * This class is not instantiable.
 * <p>
 * Null, empty, and whitespace-only input are treated as an empty document
 * and convert to the canonical empty representation of the target format:
 * {@code {}} for JSON and YAML, {@code <root/>} for XML, and {@code ""} for CSV.
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

	private static final String EMPTY_JSON = "{}";
	private static final String EMPTY_YAML = "{}";
	private static final String EMPTY_XML = "<root/>";
	private static final String EMPTY_CSV = "";

	private static final int INITIAL_BUFFER_SIZE = 8192;

	private DocConverter() {}

	// ─────────────────────────────────────────────
	// Data-format conversions
	// ─────────────────────────────────────────────

	/**
	 * Converts XML to JSON.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty JSON object {@code {}}.</p>
	 *
	 * @param xml XML document
	 * @return JSON representation, or {@code {}} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String xmlToJson(String xml) {
		if (isNullOrBlank(xml)) {
			return EMPTY_JSON;
		}
		try {
			return JSON_MAPPER.writeValueAsString(XML_MAPPER.readTree(xml));
		} catch (Exception e) {
			throw wrap("Failed to convert XML to JSON", e);
		}
	}

	/**
	 * Converts XML to YAML.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty YAML object {@code {}}.</p>
	 *
	 * @param xml XML document
	 * @return YAML representation, or {@code {}} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String xmlToYaml(String xml) {
		if (isNullOrBlank(xml)) {
			return EMPTY_YAML;
		}
		try {
			return YAML_MAPPER.writeValueAsString(XML_MAPPER.readTree(xml));
		} catch (Exception e) {
			throw wrap("Failed to convert XML to YAML", e);
		}
	}

	/**
	 * Converts XML to CSV.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in an empty string.</p>
	 *
	 * @param xml XML document
	 * @return CSV representation, or {@code ""} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String xmlToCsv(String xml) {
		if (isNullOrBlank(xml)) {
			return EMPTY_CSV;
		}
		try {
			return treeToCsv(XML_MAPPER.readTree(xml));
		} catch (Exception e) {
			throw wrap("Failed to convert XML to CSV", e);
		}
	}

	/**
	 * Converts JSON to XML.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty XML document {@code <root/>}.</p>
	 *
	 * @param json JSON document
	 * @return XML representation, or {@code <root/>} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String jsonToXml(String json) {
		if (isNullOrBlank(json)) {
			return EMPTY_XML;
		}
		try {
			return treeToXml(JSON_MAPPER.readTree(json));
		} catch (Exception e) {
			throw wrap("Failed to convert JSON to XML", e);
		}
	}

	/**
	 * Converts JSON to YAML.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty YAML object {@code {}}.</p>
	 *
	 * @param json JSON document
	 * @return YAML representation, or {@code {}} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String jsonToYaml(String json) {
		if (isNullOrBlank(json)) {
			return EMPTY_YAML;
		}
		try {
			return YAML_MAPPER.writeValueAsString(JSON_MAPPER.readTree(json));
		} catch (Exception e) {
			throw wrap("Failed to convert JSON to YAML", e);
		}
	}

	/**
	 * Converts JSON to CSV.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in an empty string.</p>
	 *
	 * @param json JSON document
	 * @return CSV representation, or {@code ""} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String jsonToCsv(String json) {
		if (isNullOrBlank(json)) {
			return EMPTY_CSV;
		}
		try {
			return treeToCsv(JSON_MAPPER.readTree(json));
		} catch (Exception e) {
			throw wrap("Failed to convert JSON to CSV", e);
		}
	}

	/**
	 * Converts CSV to XML.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty XML document {@code <root/>}.</p>
	 *
	 * @param csv CSV document
	 * @return XML representation, or {@code <root/>} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String csvToXml(String csv) {
		if (isNullOrBlank(csv)) {
			return EMPTY_XML;
		}
		try {
			StringBuilder xml = new StringBuilder(INITIAL_BUFFER_SIZE);
			xml.append("<?xml version='1.0' encoding='UTF-8'?><rows>");
			try (MappingIterator<String[]> rows = CSV_ROW_READER.readValues(nullToEmpty(csv))) {
				while (rows.hasNextValue()) {
					String[] row = rows.nextValue();
					if (isBlankRow(row)) {
						continue;
					}
					xml.append("<row>");
					for (String col : row) {
						xml.append("<item>");
						appendEscaped(xml, col != null ? col : "");
						xml.append("</item>");
					}
					xml.append("</row>");
				}
			}
			xml.append("</rows>");
			return xml.toString();
		} catch (Exception e) {
			throw wrap("Failed to convert CSV to XML", e);
		}
	}

	/**
	 * Converts CSV to JSON.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty JSON object {@code {}}.</p>
	 *
	 * @param csv CSV document
	 * @return JSON representation, or {@code {}} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String csvToJson(String csv) {
		if (isNullOrBlank(csv)) {
			return EMPTY_JSON;
		}
		try {
			return JSON_MAPPER.writeValueAsString(csvToTree(csv));
		} catch (Exception e) {
			throw wrap("Failed to convert CSV to JSON", e);
		}
	}

	/**
	 * Converts CSV to YAML.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty YAML object {@code {}}.</p>
	 *
	 * @param csv CSV document
	 * @return YAML representation, or {@code {}} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String csvToYaml(String csv) {
		if (isNullOrBlank(csv)) {
			return EMPTY_YAML;
		}
		try {
			return YAML_MAPPER.writeValueAsString(csvToTree(csv));
		} catch (Exception e) {
			throw wrap("Failed to convert CSV to YAML", e);
		}
	}

	/**
	 * Converts YAML to XML.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty XML document {@code <root/>}.</p>
	 *
	 * @param yaml YAML document
	 * @return XML representation, or {@code <root/>} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String yamlToXml(String yaml) {
		if (isNullOrBlank(yaml)) {
			return EMPTY_XML;
		}
		try {
			return treeToXml(YAML_MAPPER.readTree(yaml));
		} catch (Exception e) {
			throw wrap("Failed to convert YAML to XML", e);
		}
	}

	/**
	 * Converts YAML to JSON.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in the canonical empty JSON object {@code {}}.</p>
	 *
	 * @param yaml YAML document
	 * @return JSON representation, or {@code {}} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String yamlToJson(String yaml) {
		if (isNullOrBlank(yaml)) {
			return EMPTY_JSON;
		}
		try {
			return JSON_MAPPER.writeValueAsString(YAML_MAPPER.readTree(yaml));
		} catch (Exception e) {
			throw wrap("Failed to convert YAML to JSON", e);
		}
	}

	/**
	 * Converts YAML to CSV.
	 *
	 * <p>Null, empty, and blank input are treated as an empty document
	 * and result in an empty string.</p>
	 *
	 * @param yaml YAML document
	 * @return CSV representation, or {@code ""} for empty input
	 * @throws DocConversionException if conversion fails
	 */
	public static String yamlToCsv(String yaml) {
		if (isNullOrBlank(yaml)) {
			return EMPTY_CSV;
		}
		try {
			return treeToCsv(YAML_MAPPER.readTree(yaml));
		} catch (Exception e) {
			throw wrap("Failed to convert YAML to CSV", e);
		}
	}

	// ─────────────────────────────────────────────
	// Format detection
	// ─────────────────────────────────────────────

	/**
	 * Checks if String is XML
	 * @param xml as String
	 * @return check as boolean
	 */
	public static boolean isXML(String xml) {
		if (isNullOrBlank(xml)) {
			return false;
		}
		String stripped = xml.stripLeading();
		if (!stripped.startsWith("<")) {
			return false;
		}
		try {
			XML_MAPPER.readTree(xml);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Checks if String is JSON
	 * @param json as String
	 * @return check as boolean
	 */
	public static boolean isJson(String json) {
		if (isNullOrBlank(json)) {
			return false;
		}
		String stripped = json.stripLeading();
		if (!stripped.startsWith("{") && !stripped.startsWith("[")) {
			return false;
		}
		try {
			JSON_MAPPER.readTree(json);
			return true;
		} catch (Exception ignored) {
			return false;
		}
	}

	/**
	 * Checks if String is YAML
	 * @param yaml as String
	 * @return check as boolean
	 */
	public static boolean isYaml(String yaml) {
		if (isNullOrBlank(yaml)) {
			return false;
		}
		try {
			YAML_MAPPER.readTree(yaml);
			return true;
		} catch (Exception ignored) {
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

		StringBuilder csv = new StringBuilder(INITIAL_BUFFER_SIZE);
		if (rows.isArray()) {
			for (JsonNode row : rows) {
				appendCsvRow(csv, row);
			}
		} else {
			appendCsvRow(csv, rows);
		}

		int length = csv.length();
		while (length > 0 && (csv.charAt(length - 1) == '\n' || csv.charAt(length - 1) == '\r')) {
			length--;
		}
		csv.setLength(length);
		return csv.toString();
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

	private static void appendCsvRow(StringBuilder csv, JsonNode row) {
		JsonNode items = row.get("item");
		if (items == null || items.isNull()) {
			return;
		}
		if (items.isArray()) {
			for (int i = 0, size = items.size(); i < size; i++) {
				if (i > 0) {
					csv.append(',');
				}
				appendCsvField(csv, items.get(i).asString());
			}
		} else {
			appendCsvField(csv, items.asString());
		}
		csv.append('\n');
	}

	private static void appendCsvField(StringBuilder csv, String value) {
		String fieldValue = value == null ? "" : value;
		boolean needsQuotes = false;
		for (int i = 0, len = fieldValue.length(); i < len; i++) {
			char c = fieldValue.charAt(i);
			if (c == ',' || c == '"' || c == '\n' || c == '\r') {
				needsQuotes = true;
				break;
			}
		}
		if (!needsQuotes) {
			csv.append(fieldValue);
			return;
		}
		csv.append('"');
		for (int i = 0, len = fieldValue.length(); i < len; i++) {
			char c = fieldValue.charAt(i);
			if (c == '"') {
				csv.append('"');
			}
			csv.append(c);
		}
		csv.append('"');
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

	private static boolean isNullOrBlank(String value) {
		return value == null || value.isBlank();
	}

	private static DocConversionException wrap(String message, Exception e) {
		if (e instanceof DocConversionException conversionException) {
			return conversionException;
		}
		return new DocConversionException(message, e);
	}

	private static void appendEscaped(StringBuilder xml, String value) {
		for (int i = 0, len = value.length(); i < len; i++) {
			char c = value.charAt(i);
			switch (c) {
				case '<'  -> xml.append("&lt;");
				case '>'  -> xml.append("&gt;");
				case '&'  -> xml.append("&amp;");
				case '"'  -> xml.append("&quot;");
				case '\'' -> xml.append("&apos;");
				default   -> xml.append(c);
			}
		}
	}

}
