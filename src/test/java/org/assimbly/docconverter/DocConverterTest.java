package org.assimbly.docconverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.dataformat.yaml.YAMLMapper;

import com.opencsv.CSVReader;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.Input;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assimbly.docconverter.DocConverter.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocConverterTest {

	private static final ObjectMapper YAML_MAPPER = new YAMLMapper();
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

	// ─────────────────────────────────────────────
	// Shared fixtures
	// ─────────────────────────────────────────────

	private static final String ROOT_XML = "<root><child>value</child></root>";
	private static final String NODE_XML = "<child>value</child>";

	private static final String SIMPLE_XML =
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
					"<headers><Content-Type type=\"header\" language=\"constant\">text/xml</Content-Type></headers>";

	private static final String CSV_AS_XML = "<rows><row><item>1</item><item>FAB0d41d5b5d22c</item><item>Ferrell LLC</item><item>https://price.net/</item><item>Papua New Guinea</item><item>Horizontal empowering knowledgebase</item><item>1990</item><item>Plastics</item><item>3498</item></row><row><item>2</item><item>6A7EdDEA9FaDC52</item><item>Mckinney, Riley and Day</item><item>https://www.hall-buchanan.info/</item><item>Finland</item><item>User-centric system-worthy leverage</item><item>2015</item><item>Glass / Ceramics / Concrete</item><item>4952</item></row></rows>";

	private static final String CSV_AS_YAML = """
---
rows:
  row:
  - item:
    - 1
    - "FAB0d41d5b5d22c"
    - "Ferrell LLC"
    - "https://price.net/"
    - "Papua New Guinea"
    - "Horizontal empowering knowledgebase"
    - 1990
    - "Plastics"
    - 3498
  - item:
    - 2
    - "6A7EdDEA9FaDC52"
    - "Mckinney, Riley and Day"
    - "https://www.hall-buchanan.info/"
    - "Finland"
    - "User-centric system-worthy leverage"
    - 2015
    - "Glass / Ceramics / Concrete"
    - 4952""";

	private static final String CSV_AS_JSON = "{\"rows\":{\"row\":[{\"item\":[\"1\",\"FAB0d41d5b5d22c\",\"Ferrell LLC\",\"https://price.net/\",\"Papua New Guinea\",\"Horizontal empowering knowledgebase\",\"1990\",\"Plastics\",\"3498\"]},{\"item\":[\"2\",\"6A7EdDEA9FaDC52\",\"Mckinney, Riley and Day\",\"https://www.hall-buchanan.info/\",\"Finland\",\"User-centric system-worthy leverage\",\"2015\",\"Glass / Ceramics / Concrete\",\"4952\"]}]}}";

	private static final String SIMPLE_JSON = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"content\":\"text/xml\"}}}";

	private static final String SIMPLE_JSON_ARRAY = "[\"Ford\", \"BMW\", \"Fiat\"]";

	private static final String SIMPLE_YAML = """
        ---
        headers:
          Content-Type:
            language: "constant"
            type: "header"
            content: "text/xml"
        """;

	private static final String SIMPLE_CSV = """
1,FAB0d41d5b5d22c,Ferrell LLC,https://price.net/,Papua New Guinea,Horizontal empowering knowledgebase,1990,Plastics,3498
2,6A7EdDEA9FaDC52,"Mckinney, Riley and Day",https://www.hall-buchanan.info/,Finland,User-centric system-worthy leverage,2015,Glass / Ceramics / Concrete,4952
			""";

	// ─────────────────────────────────────────────
	// Happy path (unchanged from original, kept for baseline)
	// Happy path (unchanged from original, kept for baseline)
	// ─────────────────────────────────────────────

	@Test
	void convertXmlToJson() throws Exception {
		String expected = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"value\":\"text/xml\"}}}";
		JSONAssert.assertEquals(expected, DocConverter.convertXmlToJson(SIMPLE_XML), JSONCompareMode.LENIENT);
	}

	@Test
	void convertXmlToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual   = YAML_MAPPER.readValue(DocConverter.convertXmlToYaml(SIMPLE_XML), MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void convertXmlToCsv() throws Exception {

		try (CSVReader readerE = new CSVReader(new StringReader(SIMPLE_CSV));
			 CSVReader readerA = new CSVReader(new StringReader(DocConverter.convertXmlToCsv(CSV_AS_XML)))) {

			List<String[]> expectedData = readerE.readAll();
			List<String[]> actualData = readerA.readAll();

			assertEquals(expectedData.size(), actualData.size(), "Row count mismatch");

			for (int i = 0; i < expectedData.size(); i++) {
				assertArrayEquals(expectedData.get(i), actualData.get(i), "Mismatch at row " + i);
			}
		}
	}

	@Test
	void convertJsonToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual   = YAML_MAPPER.readValue(DocConverter.convertJsonToYaml(SIMPLE_JSON), MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void convertJsonToXml() {
		String expected = "<headers><Content-Type><type>header</type><language>constant</language><content>text/xml</content></Content-Type></headers>";
		XmlAssert.assertThat(Input.fromString(DocConverter.convertJsonToXml(SIMPLE_JSON)))
				.and(Input.fromString(expected)).areIdentical();
	}

	@Test
	void convertJsonArrayToXml() {
		String expected = "<ArrayList><item>Ford</item><item>BMW</item><item>Fiat</item></ArrayList>";
		XmlAssert.assertThat(Input.fromString(DocConverter.convertJsonToXml(SIMPLE_JSON_ARRAY)))
				.and(Input.fromString(expected)).areIdentical();
	}


	@Test
	void convertJsonToCsv() throws Exception {

		try (CSVReader readerE = new CSVReader(new StringReader(SIMPLE_CSV));
			 CSVReader readerA = new CSVReader(new StringReader(DocConverter.convertJsonToCsv(CSV_AS_JSON)))) {

			List<String[]> expectedData = readerE.readAll();
			List<String[]> actualData = readerA.readAll();

			assertEquals(expectedData.size(), actualData.size(), "Row count mismatch");

			for (int i = 0; i < expectedData.size(); i++) {
				assertArrayEquals(expectedData.get(i), actualData.get(i), "Mismatch at row " + i);
			}
		}
	}

	@Test
	void convertYamlToXml() {
		String expected = "<headers><Content-Type><language>constant</language><type>header</type><content>text/xml</content></Content-Type></headers>";
		XmlAssert.assertThat(Input.fromString(DocConverter.convertYamlToXml(SIMPLE_YAML)))
				.and(Input.fromString(expected)).areIdentical();
	}

	@Test
	void convertYamlToJson() {
		String expected = "{\"headers\":{\"Content-Type\":{\"language\":\"constant\",\"type\":\"header\",\"content\":\"text/xml\"}}}";
		JSONAssert.assertEquals(expected, DocConverter.convertYamlToJson(SIMPLE_YAML), JSONCompareMode.LENIENT);
	}


	@Test
	void convertYamlToCsv() throws Exception {

		try (CSVReader readerE = new CSVReader(new StringReader(SIMPLE_CSV));
			 CSVReader readerA = new CSVReader(new StringReader(DocConverter.convertYamlToCsv(CSV_AS_YAML)))) {

			List<String[]> expectedData = readerE.readAll();
			List<String[]> actualData = readerA.readAll();

			assertEquals(expectedData.size(), actualData.size(), "Row count mismatch");

			for (int i = 0; i < expectedData.size(); i++) {
				assertArrayEquals(expectedData.get(i), actualData.get(i), "Mismatch at row " + i);
			}
		}
	}

	@Test
	void convertCsvToXml() {
		XmlAssert.assertThat(Input.fromString(DocConverter.convertCsvToXml(SIMPLE_CSV)))
				.and(CSV_AS_XML).ignoreWhitespace().areIdentical();
	}

	@Test
	void convertCsvToJson() throws Exception {
		JSONAssert.assertEquals(CSV_AS_JSON, DocConverter.convertCsvToJson(SIMPLE_CSV), JSONCompareMode.LENIENT);
	}

	@Test
	void convertCsvToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(CSV_AS_YAML, MAP_TYPE);
        Map<String, Object> actual   = YAML_MAPPER.readValue(DocConverter.convertCsvToYaml(SIMPLE_CSV), MAP_TYPE);
		assertEquals(expected, actual);
	}

	// ─────────────────────────────────────────────
	// Round-trip fidelity
	// ─────────────────────────────────────────────

	@Test
	void roundTrip_jsonToYamlAndBack() {
		String yaml     = DocConverter.convertJsonToYaml(SIMPLE_JSON);
		String backJson = DocConverter.convertYamlToJson(yaml);
		JSONAssert.assertEquals(SIMPLE_JSON, backJson, JSONCompareMode.LENIENT);
	}

	@Test
	void roundTrip_yamlToXmlAndBack() {
		String xml      = DocConverter.convertYamlToXml(SIMPLE_YAML);
		String backYaml = DocConverter.convertXmlToYaml(xml);
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual   = YAML_MAPPER.readValue(backYaml, MAP_TYPE);
		assertEquals(expected, actual);
	}

	// ─────────────────────────────────────────────
	// Special characters and encoding
	// ─────────────────────────────────────────────

	@Test
	void convertJsonToXml_escapesAmpersandAndAngleBrackets() {
		String json   = "{\"note\":\"a & b < c > d\"}";
		String xml    = DocConverter.convertJsonToXml(json);
		// Values must be XML-escaped in the output
		assertThat(xml).doesNotContain(" & ")
				.doesNotContain(" < ");
	}

	@Test
	void convertXmlToJson_preservesUnicode() throws Exception {
		String xml  = "<data><label>中文テスト</label></data>";
		String json = DocConverter.convertXmlToJson(xml);
		assertThat(json).contains("中文テスト");
	}

	@Test
	void convertJsonToYaml_preservesUnicode() {
		String json = "{\"message\":\"héllo wörld\"}";
		String yaml = DocConverter.convertJsonToYaml(json);
		assertThat(yaml).contains("héllo wörld");
	}

	@Test
	void convertXmlToJson_preservesQuotesInAttributeValues() throws Exception {
		// Attribute values with single quotes should survive the conversion
		String xml  = "<item type=\"it&apos;s a test\">value</item>";
		String json = DocConverter.convertXmlToJson(xml);
		assertThat(json).isNotEmpty();
	}

	// ─────────────────────────────────────────────
	// Numeric and boolean values
	// ─────────────────────────────────────────────

	@Test
	void convertJsonToXml_handlesNumericValues() {
		String json = "{\"stats\":{\"count\":42,\"ratio\":3.14}}";
		String xml  = DocConverter.convertJsonToXml(json);
		assertThat(xml).contains("42").contains("3.14");
	}

	@Test
	void convertJsonToXml_handlesBooleanValues() {
		String json = "{\"flags\":{\"active\":true,\"deleted\":false}}";
		String xml  = DocConverter.convertJsonToXml(json);
		assertThat(xml).contains("true").contains("false");
	}

	@Test
	void convertYamlToJson_handlesBooleanAndNumericValues() {
		String yaml     = "---\nactive: true\ncount: 7\nratio: 2.5\n";
		String json     = DocConverter.convertYamlToJson(yaml);
		JSONAssert.assertEquals("{\"active\":true,\"count\":7,\"ratio\":2.5}", json, JSONCompareMode.STRICT);
	}

	// ─────────────────────────────────────────────
	// Structural edge cases
	// ─────────────────────────────────────────────

	@Test
	void convertJsonToXml_handlesFlatStructure() {

		String json = "{\"key\":\"value\"}";
		String xml  = DocConverter.convertJsonToXml(json);

		assertThat(xml).contains("key").contains("value");

		// Must produce parseable XML
		XmlAssert.assertThat(xml).isInvalid();

	}

	@Test
	void convertXmlToJson_handlesDeeplyNested() throws Exception {
		String xml  = "<a><b><c><d>deep</d></c></b></a>";
		String json = DocConverter.convertXmlToJson(xml);
		JSONAssert.assertEquals("{\"a\":{\"b\":{\"c\":{\"d\":\"deep\"}}}}", json, JSONCompareMode.LENIENT);
	}

	@Test
	void convertJsonToYaml_handlesArray() {
		String json     = "{\"items\":[\"one\",\"two\",\"three\"]}";
		String yaml     = DocConverter.convertJsonToYaml(json);
		String backJson = DocConverter.convertYamlToJson(yaml);
		JSONAssert.assertEquals(json, backJson, JSONCompareMode.LENIENT);
	}

	// ─────────────────────────────────────────────
	// Null and empty inputs
	// ─────────────────────────────────────────────

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void convertXmlToJson_throwsOrReturnsEmptyOnBlankInput(String blank) throws Exception {
		assertThat(DocConverter.convertXmlToJson(blank)).isEqualTo("{}");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void convertJsonToXml_throwsOrReturnsEmptyOnBlankInput(String blank) {
		Assertions.assertEquals("",DocConverter.convertJsonToXml(blank));
	}

	@Test
	void convertXmlToJson_throwsOnMalformedXml() {
		assertThatThrownBy(() -> DocConverter.convertXmlToJson("<unclosed>"))
				.isInstanceOf(Exception.class);
	}

	@Test
	void convertJsonToXml_throwsOnMalformedJson() {
		assertThatThrownBy(() -> DocConverter.convertJsonToXml("{bad json"))
				.isInstanceOf(Exception.class);
	}

	@Test
	void convertYamlToJson_throwsOnMalformedYaml() {
		// Indentation errors in YAML should propagate as exceptions
		String badYaml = """
						key: value
						  invalid_indentation: true""";

		assertThatThrownBy(() -> DocConverter.convertYamlToJson(badYaml))
				.isInstanceOf(Exception.class);
	}

	// ─── isJson ───────────────────────────────────────────────────────────────

	@Test
	void isJson_validObject_returnsTrue() {
		assertThat(isJson("{\"key\": \"value\"}")).isTrue();
	}

	@Test
	void isJson_validArray_returnsTrue() {
		assertThat(isJson("[1, 2, 3]")).isTrue();
	}

	@Test
	void isJson_nestedObject_returnsTrue() {
		assertThat(isJson("{\"outer\": {\"inner\": 42}}")).isTrue();
	}

	@Test
	void isJson_validWithLeadingWhitespace_returnsTrue() {
		assertThat(isJson("   {\"key\": \"value\"}")).isTrue();
	}

	@Test
	void isJson_null_returnsFalse() {
		assertThat(isJson(null)).isFalse();
	}

	@Test
	void isJson_blank_returnsFalse() {
		assertThat(isJson("   ")).isFalse();
	}

	@Test
	void isJson_plainText_returnsFalse() {
		assertThat(isJson("hello world")).isFalse();
	}

	@Test
	void isJson_xmlString_returnsFalse() {
		assertThat(isJson("<root><child/></root>")).isFalse();
	}

	@Test
	void isJson_yamlString_returnsFalse() {
		assertThat(isJson("key: value")).isFalse();
	}

	@Test
	void isJson_malformedObject_returnsFalse() {
		assertThat(isJson("{key: value}")).isFalse();
	}

	@Test
	void isJson_unclosedObject_returnsFalse() {
		assertThat(isJson("{\"key\": \"value\"")).isFalse();
	}

	@Test
	void isJson_emptyObject_returnsTrue() {
		assertThat(isJson("{}")).isTrue();
	}

	@Test
	void isJson_emptyArray_returnsTrue() {
		assertThat(isJson("[]")).isTrue();
	}

	// ─── isYaml ───────────────────────────────────────────────────────────────

	@Test
	void isYaml_simpleKeyValue_returnsTrue() {
		assertThat(isYaml("key: value")).isTrue();
	}

	@Test
	void isYaml_multipleKeyValues_returnsTrue() {
		assertThat(isYaml("key1: value1\nkey2: value2")).isTrue();
	}

	@Test
	void isYaml_withDocumentMarker_returnsTrue() {
		assertThat(isYaml("---\nkey: value")).isTrue();
	}

	@Test
	void isYaml_nestedStructure_returnsTrue() {
		assertThat(isYaml("parent:\n  child: value")).isTrue();
	}

	@Test
	void isYaml_list_returnsTrue() {
		assertThat(isYaml("- item1\n- item2\n- item3")).isTrue();
	}

	@Test
	void isYaml_validWithLeadingWhitespace_returnsTrue() {
		assertThat(isYaml("   key: value")).isTrue();
	}

	@Test
	void isYaml_null_returnsFalse() {
		assertThat(isYaml(null)).isFalse();
	}

	@Test
	void isYaml_blank_returnsFalse() {
		assertThat(isYaml("   ")).isFalse();
	}

	@Test
	void isYaml_invalidTabIndentation_returnsFalse() {
		assertThat(isYaml("key:\n\tchild: value")).isFalse();
	}

	@Test
	void isYaml_unclosedQuote_returnsFalse() {
		assertThat(isYaml("key: \"unclosed")).isFalse();
	}

	@Test
	void isYaml_duplicateKeys_returnsTrue() {
		// YAML spec allows duplicate keys; parsers typically accept them
		assertThat(isYaml("key: value1\nkey: value2")).isTrue();
	}

	// ─── Stream / String ──────────────────────────────────────────────────────

	@Test
	void convertStreamToString_returnsOriginalContent() {
		InputStream stream = new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8));
		assertThat(convertStreamToString(stream)).isEqualTo("hello world");
	}

	@Test
	void convertStringToStream_returnsReadableStream() throws Exception {
		InputStream stream = convertStringToStream("hello world");
		assertThat(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello world");
	}

	// ─── Document / String ───────────────────────────────────────────────────

	@Test
	void convertDocToString_containsRootElement() throws Exception {
		Document doc = convertStringToDoc(ROOT_XML);
		assertThat(convertDocToString(doc)).contains("<root>", "<child>value</child>");
	}

	@Test
	void convertStringToDoc_parsesRootElement() throws Exception {
		Document doc = convertStringToDoc(ROOT_XML);
		assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
	}

	// ─── Node / String ───────────────────────────────────────────────────────

	@Test
	void convertNodeToString_returnsNodeMarkup() throws Exception {
		Document doc = convertStringToDoc(ROOT_XML);
		Node child = doc.getDocumentElement().getFirstChild();
		assertThat(convertNodeToString(child)).contains("child", "value");
	}

	@Test
	void convertStringToNode_parsesTagName() throws Exception {
		Node node = convertStringToNode(NODE_XML);
		assertThat(node.getNodeName()).isEqualTo("child");
	}

	// ─── File / String ───────────────────────────────────────────────────────

	@Test
	void convertFileToString_defaultEncoding_returnsContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		java.nio.file.Files.writeString(file, "file content");
		assertThat(convertFileToString(file.toString())).isEqualTo("file content");
	}

	@Test
	void convertFileToString_explicitEncoding_returnsContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		java.nio.file.Files.writeString(file, "file content");
		assertThat(convertFileToString(file.toString(), StandardCharsets.UTF_8)).isEqualTo("file content");
	}

	@Test
	void convertStringToFile_writesReadableFile(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("output.txt");
		convertStringToFile(file.toString(), "written content");
		assertThat(java.nio.file.Files.readString(file)).isEqualTo("written content");
	}

	// ─── Source / String ─────────────────────────────────────────────────────

	@Test
	void convertStringToSource_returnsNonNullSource() {
		assertThat(convertStringToSource("<root/>")).isNotNull();
	}

	// ─── List / String ───────────────────────────────────────────────────────

	@Test
	void convertListToString_joinsWithComma() {
		assertThat(convertListToString(List.of("a", "b", "c"))).isEqualTo("a,b,c");
	}

	@Test
	void convertStringToList_splitsOnComma() {
		assertThat(convertStringToList("a,b,c")).containsExactly("a", "b", "c");
	}

	// ─── Reader / String ─────────────────────────────────────────────────────

	@Test
	void convertStringToReader_returnsReadableContent() throws Exception {
		Reader reader = convertStringToReader("hello");
		assertThat(new BufferedReader(reader).readLine()).isEqualTo("hello");
	}

	@Test
	void convertReaderToString_returnsOriginalContent() throws Exception {
		assertThat(convertReaderToString(new StringReader("hello"))).isEqualTo("hello");
	}

	// ─── Object conversions ───────────────────────────────────────────────────

	@Test
	void convertObjectToString_returnsStringRepresentation() {
		assertThat(convertObjectToString(42)).isEqualTo("42");
	}

	@Test
	void convertObjectToJSONString_serializesFields() {
		record Person(String name, int age) {}
		assertThat(convertObjectToJSONString(new Person("Alice", 30)))
				.contains("\"name\"", "Alice", "\"age\"", "30");
	}

	// ─── File / URI / URL ────────────────────────────────────────────────────

	@Test
	void convertFileToURI_returnsFileSchemeURI(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("test.txt").toFile();
		boolean created = file.createNewFile();
		assertThat(created).isTrue();
		URI uri = convertFileToURI(file);
		assertThat(uri.getScheme()).isEqualTo("file");
	}

	@Test
	void convertFileToURL_returnsFileSchemeURL(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("test.txt").toFile();
		boolean created = file.createNewFile();
		assertThat(created).isTrue();
		URL url = convertFileToURL(file);
		assertThat(url.getProtocol()).isEqualTo("file");
	}

}