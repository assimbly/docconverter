package org.assimbly.docconverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.yaml.YAMLMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.Input;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assimbly.docconverter.DocConverter.convertDocToString;
import static org.assimbly.docconverter.DocConverter.convertFileToString;
import static org.assimbly.docconverter.DocConverter.convertFileToURI;
import static org.assimbly.docconverter.DocConverter.convertFileToURL;
import static org.assimbly.docconverter.DocConverter.convertListToString;
import static org.assimbly.docconverter.DocConverter.convertNodeToString;
import static org.assimbly.docconverter.DocConverter.convertObjectToJSONString;
import static org.assimbly.docconverter.DocConverter.convertObjectToString;
import static org.assimbly.docconverter.DocConverter.convertReaderToString;
import static org.assimbly.docconverter.DocConverter.convertStreamToString;
import static org.assimbly.docconverter.DocConverter.convertStringToDoc;
import static org.assimbly.docconverter.DocConverter.convertStringToFile;
import static org.assimbly.docconverter.DocConverter.convertStringToList;
import static org.assimbly.docconverter.DocConverter.convertStringToNode;
import static org.assimbly.docconverter.DocConverter.convertStringToReader;
import static org.assimbly.docconverter.DocConverter.convertStringToSource;
import static org.assimbly.docconverter.DocConverter.convertStringToStream;
import static org.assimbly.docconverter.DocConverter.isJson;
import static org.assimbly.docconverter.DocConverter.isXML;
import static org.assimbly.docconverter.DocConverter.isYaml;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocConverterTest {

	private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

	private static final String ROOT_XML = "<root><child>value</child></root>";
	private static final String NODE_XML = "<child>value</child>";

	private static final String SIMPLE_XML =
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>" +
					"<headers><Content-Type type=\"header\" language=\"constant\">text/xml</Content-Type></headers>";

	private static final String CSV_AS_XML = "<rows><row><item>1</item><item>FAB0d41d5b5d22c</item><item>Ferrell LLC</item><item>https://price.net/</item><item>Papua New Guinea</item><item>Horizontal empowering knowledgebase</item><item>1990</item><item>Plastics</item><item>3498</item></row><row><item>2</item><item>6A7EdDEA9FaDC52</item><item>Mckinney, Riley and Day</item><item>https://www.hall-buchanan.info/</item><item>Finland</item><item>User-centric system-worthy leverage</item><item>2015</item><item>Glass / Ceramics / Concrete</item><item>4952</item></row></rows>";

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

	private static final String XML_AS_YAML = """
        ---
        headers:
          Content-Type:
            language: "constant"
            type: "header"
            value: "text/xml"
        """;

	private static final String SIMPLE_CSV = """
1,FAB0d41d5b5d22c,Ferrell LLC,https://price.net/,Papua New Guinea,Horizontal empowering knowledgebase,1990,Plastics,3498
2,6A7EdDEA9FaDC52,"Mckinney, Riley and Day",https://www.hall-buchanan.info/,Finland,User-centric system-worthy leverage,2015,Glass / Ceramics / Concrete,4952
			""";

	@Test
	void convertXmlToJson() throws Exception {
		String expected = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"value\":\"text/xml\"}}}";
		String actual = DocConverter.convertXmlToJson(SIMPLE_XML);
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void convertXmlToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(XML_AS_YAML, MAP_TYPE);
		Map<String, Object> actual = YAML_MAPPER.readValue(DocConverter.convertXmlToYaml(SIMPLE_XML), MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void convertXmlToCsv() throws Exception {
		assertCsvEquals(SIMPLE_CSV, DocConverter.convertXmlToCsv(CSV_AS_XML));
	}

	@Test
	void convertJsonToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual = YAML_MAPPER.readValue(DocConverter.convertJsonToYaml(SIMPLE_JSON), MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void convertJsonToXml() {
		String expected = "<headers><Content-Type><type>header</type><language>constant</language><content>text/xml</content></Content-Type></headers>";
		XmlAssert.assertThat(Input.fromString(DocConverter.convertJsonToXml(SIMPLE_JSON)))
				.and(Input.fromString(expected)).ignoreWhitespace().areSimilar();
	}

	@Test
	void convertJsonArrayToXml() throws Exception {
		String xml = DocConverter.convertJsonToXml(SIMPLE_JSON_ARRAY);
		assertThat(xml).contains("Ford").contains("BMW").contains("Fiat");
		assertThat(convertStringToDoc(xml).getDocumentElement().getTagName()).isEqualTo("ArrayList");
	}

	@Test
	void convertJsonToCsv() throws Exception {
		assertCsvEquals(SIMPLE_CSV, DocConverter.convertJsonToCsv(CSV_AS_JSON));
	}

	@Test
	void convertYamlToXml() {
		String expected = "<headers><Content-Type><language>constant</language><type>header</type><content>text/xml</content></Content-Type></headers>";
		XmlAssert.assertThat(Input.fromString(DocConverter.convertYamlToXml(SIMPLE_YAML)))
				.and(Input.fromString(expected)).ignoreWhitespace().areSimilar();
	}

	@Test
	void convertYamlToJson() throws Exception {
		String expected = "{\"headers\":{\"Content-Type\":{\"language\":\"constant\",\"type\":\"header\",\"content\":\"text/xml\"}}}";
		String actual = DocConverter.convertYamlToJson(SIMPLE_YAML);
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void convertYamlToCsv() throws Exception {
		assertCsvEquals(SIMPLE_CSV, DocConverter.convertYamlToCsv(DocConverter.convertCsvToYaml(SIMPLE_CSV)));
	}

	@Test
	void convertCsvToXml() {
		XmlAssert.assertThat(Input.fromString(DocConverter.convertCsvToXml(SIMPLE_CSV)))
				.and(CSV_AS_XML).ignoreWhitespace().areIdentical();
	}

	@Test
	void convertCsvToJson() throws Exception {
		String actual = DocConverter.convertCsvToJson(SIMPLE_CSV);
		JSONAssert.assertEquals(CSV_AS_JSON, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void convertCsvToYaml() throws Exception {
		String actual = DocConverter.convertYamlToJson(DocConverter.convertCsvToYaml(SIMPLE_CSV));
		JSONAssert.assertEquals(CSV_AS_JSON, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void roundTripJsonToYamlAndBack() throws Exception {
		String yaml = DocConverter.convertJsonToYaml(SIMPLE_JSON);
		String backJson = DocConverter.convertYamlToJson(yaml);
		JSONAssert.assertEquals(SIMPLE_JSON, backJson, JSONCompareMode.LENIENT);
		assertThat(backJson).isNotBlank();
	}

	@Test
	void roundTripYamlToXmlAndBack() {
		String xml = DocConverter.convertYamlToXml(SIMPLE_YAML);
		String backYaml = DocConverter.convertXmlToYaml(xml);
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual = YAML_MAPPER.readValue(backYaml, MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void roundTripCsvToJsonAndBack() throws Exception {
		String json = DocConverter.convertCsvToJson(SIMPLE_CSV);
		assertCsvEquals(SIMPLE_CSV, DocConverter.convertJsonToCsv(json));
	}

	@Test
	void convertJsonToXmlEscapesAmpersandAndAngleBrackets() {
		String json = "{\"note\":\"a & b < c > d\"}";
		String xml = DocConverter.convertJsonToXml(json);
		assertThat(xml).doesNotContain(" & ")
				.doesNotContain(" < ");
	}

	@Test
	void convertXmlToJsonPreservesUnicode() throws Exception {
		String xml = "<data><label>中文テスト</label></data>";
		String json = DocConverter.convertXmlToJson(xml);
		assertThat(json).contains("中文テスト");
	}

	@Test
	void convertJsonToYamlPreservesUnicode() {
		String json = "{\"message\":\"héllo wörld\"}";
		String yaml = DocConverter.convertJsonToYaml(json);
		assertThat(yaml).contains("héllo wörld");
	}

	@Test
	void convertXmlToJsonPreservesQuotesInAttributeValues() throws Exception {
		String xml = "<item type=\"it&apos;s a test\">value</item>";
		String json = DocConverter.convertXmlToJson(xml);
		assertThat(json).isNotEmpty();
	}

	@Test
	void convertJsonToXmlHandlesNumericValues() {
		String json = "{\"stats\":{\"count\":42,\"ratio\":3.14}}";
		String xml = DocConverter.convertJsonToXml(json);
		assertThat(xml).contains("42").contains("3.14");
	}

	@Test
	void convertJsonToXmlHandlesBooleanValues() {
		String json = "{\"flags\":{\"active\":true,\"deleted\":false}}";
		String xml = DocConverter.convertJsonToXml(json);
		assertThat(xml).contains("true").contains("false");
	}

	@Test
	void convertYamlToJsonHandlesBooleanAndNumericValues() throws Exception {
		String yaml = "---\nactive: true\ncount: 7\nratio: 2.5\n";
		String json = DocConverter.convertYamlToJson(yaml);
		JSONAssert.assertEquals("{\"active\":true,\"count\":7,\"ratio\":2.5}", json, JSONCompareMode.STRICT);
		assertThat(json).isNotBlank();
	}

	@Test
	void convertJsonToXmlHandlesFlatStructure() throws Exception {
		String json = "{\"key\":\"value\"}";
		String xml = DocConverter.convertJsonToXml(json);
		assertThat(xml).contains("key").contains("value");
		assertThat(convertStringToDoc(xml).getDocumentElement().getTagName()).isEqualTo("key");
	}

	@Test
	void convertXmlToJsonHandlesDeeplyNested() throws Exception {
		String xml = "<a><b><c><d>deep</d></c></b></a>";
		String json = DocConverter.convertXmlToJson(xml);
		JSONAssert.assertEquals("{\"a\":{\"b\":{\"c\":{\"d\":\"deep\"}}}}", json, JSONCompareMode.LENIENT);
		assertThat(json).isNotBlank();
	}

	@Test
	void convertJsonToYamlHandlesArray() throws Exception {
		String json = "{\"items\":[\"one\",\"two\",\"three\"]}";
		String yaml = DocConverter.convertJsonToYaml(json);
		String backJson = DocConverter.convertYamlToJson(yaml);
		JSONAssert.assertEquals(json, backJson, JSONCompareMode.LENIENT);
		assertThat(backJson).isNotBlank();
	}

	@Test
	void convertXmlToJsonReturnsEmptyObjectForEmptyInput() {
		assertThat(DocConverter.convertXmlToJson("")).isEqualTo("{}");
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void convertJsonToXmlThrowsOrReturnsEmptyOnBlankInput(String blank) {
		assertThat(DocConverter.convertJsonToXml(blank)).isEmpty();
	}

	@Test
	void convertXmlToJsonThrowsOnMalformedXml() {
		assertThatThrownBy(() -> DocConverter.convertXmlToJson("<unclosed>"))
				.isInstanceOf(Exception.class);
	}

	@Test
	void convertJsonToXmlThrowsOnMalformedJson() {
		assertThatThrownBy(() -> DocConverter.convertJsonToXml("{bad json"))
				.isInstanceOf(Exception.class);
	}

	@Test
	void convertYamlToJsonThrowsOnMalformedYaml() {
		String badYaml = """
						key: value
						  invalid_indentation: true""";

		assertThatThrownBy(() -> DocConverter.convertYamlToJson(badYaml))
				.isInstanceOf(Exception.class);
	}

	@Test
	void isXMLValidDocumentReturnsTrue() {
		assertThat(isXML("<root><child/></root>")).isTrue();
	}

	@Test
	void isXMLJsonStringReturnsFalse() {
		assertThat(isXML("{\"key\": \"value\"}")).isFalse();
	}

	@Test
	void isXMLNullOrBlankReturnsFalse() {
		assertThat(isXML(null)).isFalse();
		assertThat(isXML("   ")).isFalse();
	}

	@Test
	void isJsonValidObjectReturnsTrue() {
		assertThat(isJson("{\"key\": \"value\"}")).isTrue();
	}

	@Test
	void isJsonValidArrayReturnsTrue() {
		assertThat(isJson("[1, 2, 3]")).isTrue();
	}

	@Test
	void isJsonNestedObjectReturnsTrue() {
		assertThat(isJson("{\"outer\": {\"inner\": 42}}")).isTrue();
	}

	@Test
	void isJsonValidWithLeadingWhitespaceReturnsTrue() {
		assertThat(isJson("   {\"key\": \"value\"}")).isTrue();
	}

	@Test
	void isJsonNullReturnsFalse() {
		assertThat(isJson(null)).isFalse();
	}

	@Test
	void isJsonBlankReturnsFalse() {
		assertThat(isJson("   ")).isFalse();
	}

	@Test
	void isJsonPlainTextReturnsFalse() {
		assertThat(isJson("hello world")).isFalse();
	}

	@Test
	void isJsonXmlStringReturnsFalse() {
		assertThat(isJson("<root><child/></root>")).isFalse();
	}

	@Test
	void isJsonYamlStringReturnsFalse() {
		assertThat(isJson("key: value")).isFalse();
	}

	@Test
	void isJsonMalformedObjectReturnsFalse() {
		assertThat(isJson("{key: value}")).isFalse();
	}

	@Test
	void isJsonUnclosedObjectReturnsFalse() {
		assertThat(isJson("{\"key\": \"value\"")).isFalse();
	}

	@Test
	void isJsonEmptyObjectReturnsTrue() {
		assertThat(isJson("{}")).isTrue();
	}

	@Test
	void isJsonEmptyArrayReturnsTrue() {
		assertThat(isJson("[]")).isTrue();
	}

	@Test
	void isYamlSimpleKeyValueReturnsTrue() {
		assertThat(isYaml("key: value")).isTrue();
	}

	@Test
	void isYamlMultipleKeyValuesReturnsTrue() {
		assertThat(isYaml("key1: value1\nkey2: value2")).isTrue();
	}

	@Test
	void isYamlWithDocumentMarkerReturnsTrue() {
		assertThat(isYaml("---\nkey: value")).isTrue();
	}

	@Test
	void isYamlNestedStructureReturnsTrue() {
		assertThat(isYaml("parent:\n  child: value")).isTrue();
	}

	@Test
	void isYamlListReturnsTrue() {
		assertThat(isYaml("- item1\n- item2\n- item3")).isTrue();
	}

	@Test
	void isYamlValidWithLeadingWhitespaceReturnsTrue() {
		assertThat(isYaml("   key: value")).isTrue();
	}

	@Test
	void isYamlNullReturnsFalse() {
		assertThat(isYaml(null)).isFalse();
	}

	@Test
	void isYamlBlankReturnsFalse() {
		assertThat(isYaml("   ")).isFalse();
	}

	@Test
	void isYamlInvalidTabIndentationReturnsFalse() {
		assertThat(isYaml("key:\n\tchild: value")).isFalse();
	}

	@Test
	void isYamlUnclosedQuoteReturnsFalse() {
		assertThat(isYaml("key: \"unclosed")).isFalse();
	}

	@Test
	void isYamlDuplicateKeysReturnsTrue() {
		assertThat(isYaml("key: value1\nkey: value2")).isTrue();
	}

	@Test
	void convertStreamToStringReturnsOriginalContent() {
		InputStream stream = new ByteArrayInputStream("hello world".getBytes(StandardCharsets.UTF_8));
		assertThat(convertStreamToString(stream)).isEqualTo("hello world");
	}

	@Test
	void convertStringToStreamReturnsReadableStream() throws Exception {
		InputStream stream = convertStringToStream("hello world");
		assertThat(new String(stream.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("hello world");
	}

	@Test
	void convertDocToStringContainsRootElement() throws Exception {
		Document doc = convertStringToDoc(ROOT_XML);
		assertThat(convertDocToString(doc)).contains("<root>", "<child>value</child>");
	}

	@Test
	void convertStringToDocParsesRootElement() throws Exception {
		Document doc = convertStringToDoc(ROOT_XML);
		assertThat(doc.getDocumentElement().getTagName()).isEqualTo("root");
	}

	@Test
	void convertNodeToStringReturnsNodeMarkup() throws Exception {
		Document doc = convertStringToDoc(ROOT_XML);
		Node child = doc.getDocumentElement().getFirstChild();
		assertThat(convertNodeToString(child)).contains("child", "value");
	}

	@Test
	void convertStringToNodeParsesTagName() throws Exception {
		Node node = convertStringToNode(NODE_XML);
		assertThat(node.getNodeName()).isEqualTo("child");
	}

	@Test
	void convertFileToStringDefaultEncodingReturnsContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		java.nio.file.Files.writeString(file, "file content");
		assertThat(convertFileToString(file.toString())).isEqualTo("file content");
	}

	@Test
	void convertFileToStringExplicitEncodingReturnsContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("test.txt");
		java.nio.file.Files.writeString(file, "file content");
		assertThat(convertFileToString(file.toString(), StandardCharsets.UTF_8)).isEqualTo("file content");
	}

	@Test
	void convertStringToFileWritesReadableFile(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("output.txt");
		convertStringToFile(file.toString(), "written content");
		assertThat(java.nio.file.Files.readString(file)).isEqualTo("written content");
	}

	@Test
	void convertStringToFileOverwritesExistingContent(@TempDir Path tempDir) throws Exception {
		Path file = tempDir.resolve("output.txt");
		convertStringToFile(file.toString(), "original content");
		convertStringToFile(file.toString(), "new");
		assertThat(java.nio.file.Files.readString(file)).isEqualTo("new");
	}

	@Test
	void convertStringToSourceReturnsNonNullSource() {
		assertThat(convertStringToSource("<root/>")).isNotNull();
	}

	@Test
	void convertListToStringJoinsWithComma() {
		assertThat(convertListToString(List.of("a", "b", "c"))).isEqualTo("a,b,c");
	}

	@Test
	void convertStringToListSplitsOnComma() {
		assertThat(convertStringToList("a,b,c")).containsExactly("a", "b", "c");
	}

	@Test
	void convertStringToReaderReturnsReadableContent() throws Exception {
		Reader reader = convertStringToReader("hello");
		assertThat(new BufferedReader(reader).readLine()).isEqualTo("hello");
	}

	@Test
	void convertReaderToStringReturnsOriginalContent() throws Exception {
		assertThat(convertReaderToString(new StringReader("hello"))).isEqualTo("hello");
	}

	@Test
	void convertObjectToStringReturnsStringRepresentation() {
		assertThat(convertObjectToString(42)).isEqualTo("42");
	}

	@Test
	void convertObjectToJSONStringSerializesFields() {
		record Person(String name, int age) {}
		assertThat(convertObjectToJSONString(new Person("Alice", 30)))
				.contains("\"name\"", "Alice", "\"age\"", "30");
	}

	@Test
	void convertFileToURIReturnsFileSchemeURI(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("test.txt").toFile();
		boolean created = file.createNewFile();
		assertThat(created).isTrue();
		URI uri = convertFileToURI(file);
		assertThat(uri.getScheme()).isEqualTo("file");
	}

	@Test
	void convertFileToURLReturnsFileSchemeURL(@TempDir Path tempDir) throws Exception {
		File file = tempDir.resolve("test.txt").toFile();
		boolean created = file.createNewFile();
		assertThat(created).isTrue();
		URL url = convertFileToURL(file);
		assertThat(url.getProtocol()).isEqualTo("file");
	}

	private static void assertCsvEquals(String expected, String actual) {
		String[] expectedRows = normalizeCsv(expected);
		String[] actualRows = normalizeCsv(actual);
		assertEquals(expectedRows.length, actualRows.length, "Row count mismatch");
		for (int i = 0; i < expectedRows.length; i++) {
			assertEquals(expectedRows[i], actualRows[i], "Mismatch at row " + i);
		}
	}

	private static String[] normalizeCsv(String csv) {
		return csv.strip().lines()
				.map(String::strip)
				.filter(line -> !line.isEmpty())
				.toArray(String[]::new);
	}
}
