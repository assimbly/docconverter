package org.assimbly.docconverter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.dataformat.yaml.YAMLMapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xmlunit.assertj.XmlAssert;
import org.xmlunit.builder.Input;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assimbly.docconverter.DocConverter.isJson;
import static org.assimbly.docconverter.DocConverter.isXML;
import static org.assimbly.docconverter.DocConverter.isYaml;
import static org.assimbly.docconverter.StringConverter.stringToDoc;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DocConverterTest {

	private static final YAMLMapper YAML_MAPPER = YAMLMapper.builder().build();
	private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {};

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
	void xmlToJson() throws Exception {
		String expected = "{\"headers\":{\"Content-Type\":{\"type\":\"header\",\"language\":\"constant\",\"value\":\"text/xml\"}}}";
		String actual = DocConverter.xmlToJson(SIMPLE_XML);
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void xmlToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(XML_AS_YAML, MAP_TYPE);
		Map<String, Object> actual = YAML_MAPPER.readValue(DocConverter.xmlToYaml(SIMPLE_XML), MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void xmlToCsv() throws Exception {
		assertCsvEquals(SIMPLE_CSV, DocConverter.xmlToCsv(CSV_AS_XML));
	}

	@Test
	void jsonToYaml() {
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual = YAML_MAPPER.readValue(DocConverter.jsonToYaml(SIMPLE_JSON), MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void jsonToXml() {
		String expected = "<headers><Content-Type><type>header</type><language>constant</language><content>text/xml</content></Content-Type></headers>";
		XmlAssert.assertThat(Input.fromString(DocConverter.jsonToXml(SIMPLE_JSON)))
				.and(Input.fromString(expected)).ignoreWhitespace().areSimilar();
	}

	@Test
	void jsonArrayToXml() {
		String xml = DocConverter.jsonToXml(SIMPLE_JSON_ARRAY);
		assertThat(xml).contains("Ford").contains("BMW").contains("Fiat");
		assertThat(stringToDoc(xml).getDocumentElement().getTagName()).isEqualTo("ArrayList");
	}

	@Test
	void jsonToCsv() throws Exception {
		assertCsvEquals(SIMPLE_CSV, DocConverter.jsonToCsv(CSV_AS_JSON));
	}

	@Test
	void yamlToXml() {
		String expected = "<headers><Content-Type><language>constant</language><type>header</type><content>text/xml</content></Content-Type></headers>";
		XmlAssert.assertThat(Input.fromString(DocConverter.yamlToXml(SIMPLE_YAML)))
				.and(Input.fromString(expected)).ignoreWhitespace().areSimilar();
	}

	@Test
	void yamlToJson() throws Exception {
		String expected = "{\"headers\":{\"Content-Type\":{\"language\":\"constant\",\"type\":\"header\",\"content\":\"text/xml\"}}}";
		String actual = DocConverter.yamlToJson(SIMPLE_YAML);
		JSONAssert.assertEquals(expected, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void yamlToCsv() throws Exception {
		assertCsvEquals(SIMPLE_CSV, DocConverter.yamlToCsv(DocConverter.csvToYaml(SIMPLE_CSV)));
	}

	@Test
	void csvToXml() {
		XmlAssert.assertThat(Input.fromString(DocConverter.csvToXml(SIMPLE_CSV)))
				.and(CSV_AS_XML).ignoreWhitespace().areIdentical();
	}

	@Test
	void csvToJson() throws Exception {
		String actual = DocConverter.csvToJson(SIMPLE_CSV);
		JSONAssert.assertEquals(CSV_AS_JSON, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void csvToYaml() throws Exception {
		String actual = DocConverter.yamlToJson(DocConverter.csvToYaml(SIMPLE_CSV));
		JSONAssert.assertEquals(CSV_AS_JSON, actual, JSONCompareMode.LENIENT);
		assertThat(actual).isNotBlank();
	}

	@Test
	void roundTripJsonToYamlAndBack() throws Exception {
		String yaml = DocConverter.jsonToYaml(SIMPLE_JSON);
		String backJson = DocConverter.yamlToJson(yaml);
		JSONAssert.assertEquals(SIMPLE_JSON, backJson, JSONCompareMode.LENIENT);
		assertThat(backJson).isNotBlank();
	}

	@Test
	void roundTripYamlToXmlAndBack() {
		String xml = DocConverter.yamlToXml(SIMPLE_YAML);
		String backYaml = DocConverter.xmlToYaml(xml);
		Map<String, Object> expected = YAML_MAPPER.readValue(SIMPLE_YAML, MAP_TYPE);
		Map<String, Object> actual = YAML_MAPPER.readValue(backYaml, MAP_TYPE);
		assertEquals(expected, actual);
	}

	@Test
	void roundTripCsvToJsonAndBack() throws Exception {
		String json = DocConverter.csvToJson(SIMPLE_CSV);
		assertCsvEquals(SIMPLE_CSV, DocConverter.jsonToCsv(json));
	}

	@Test
	void jsonToXmlEscapesAmpersandAndAngleBrackets() {
		String json = "{\"note\":\"a & b < c > d\"}";
		String xml = DocConverter.jsonToXml(json);
		assertThat(xml).doesNotContain(" & ")
				.doesNotContain(" < ");
	}

	@Test
	void xmlToJsonPreservesUnicode() throws Exception {
		String xml = "<data><label>中文テスト</label></data>";
		String json = DocConverter.xmlToJson(xml);
		assertThat(json).contains("中文テスト");
	}

	@Test
	void jsonToYamlPreservesUnicode() {
		String json = "{\"message\":\"héllo wörld\"}";
		String yaml = DocConverter.jsonToYaml(json);
		assertThat(yaml).contains("héllo wörld");
	}

	@Test
	void xmlToJsonPreservesQuotesInAttributeValues() throws Exception {
		String xml = "<item type=\"it&apos;s a test\">value</item>";
		String json = DocConverter.xmlToJson(xml);
		assertThat(json).isNotEmpty();
	}

	@Test
	void jsonToXmlHandlesNumericValues() {
		String json = "{\"stats\":{\"count\":42,\"ratio\":3.14}}";
		String xml = DocConverter.jsonToXml(json);
		assertThat(xml).contains("42").contains("3.14");
	}

	@Test
	void jsonToXmlHandlesBooleanValues() {
		String json = "{\"flags\":{\"active\":true,\"deleted\":false}}";
		String xml = DocConverter.jsonToXml(json);
		assertThat(xml).contains("true").contains("false");
	}

	@Test
	void yamlToJsonHandlesBooleanAndNumericValues() throws Exception {
		String yaml = "---\nactive: true\ncount: 7\nratio: 2.5\n";
		String json = DocConverter.yamlToJson(yaml);
		JSONAssert.assertEquals("{\"active\":true,\"count\":7,\"ratio\":2.5}", json, JSONCompareMode.STRICT);
		assertThat(json).isNotBlank();
	}

	@Test
	void jsonToXmlHandlesFlatStructure() {
		String json = "{\"key\":\"value\"}";
		String xml = DocConverter.jsonToXml(json);
		assertThat(xml).contains("key").contains("value");
		assertThat(stringToDoc(xml).getDocumentElement().getTagName()).isEqualTo("key");
	}

	@Test
	void xmlToJsonHandlesDeeplyNested() throws Exception {
		String xml = "<a><b><c><d>deep</d></c></b></a>";
		String json = DocConverter.xmlToJson(xml);
		JSONAssert.assertEquals("{\"a\":{\"b\":{\"c\":{\"d\":\"deep\"}}}}", json, JSONCompareMode.LENIENT);
		assertThat(json).isNotBlank();
	}

	@Test
	void jsonToYamlHandlesArray() throws Exception {
		String json = "{\"items\":[\"one\",\"two\",\"three\"]}";
		String yaml = DocConverter.jsonToYaml(json);
		String backJson = DocConverter.yamlToJson(yaml);
		JSONAssert.assertEquals(json, backJson, JSONCompareMode.LENIENT);
		assertThat(backJson).isNotBlank();
	}

	@ParameterizedTest
	@MethodSource("emptyDocuments")
	void jsonTargetTreatsEmptyInputAsEmptyObject(String input) {
		assertThat(DocConverter.xmlToJson(input)).isEqualTo("{}");
		assertThat(DocConverter.yamlToJson(input)).isEqualTo("{}");
		assertThat(DocConverter.csvToJson(input)).isEqualTo("{}");
	}

	@ParameterizedTest
	@MethodSource("emptyDocuments")
	void yamlTargetTreatsEmptyInputAsEmptyObject(String input) {
		assertThat(DocConverter.xmlToYaml(input)).isEqualTo("{}");
		assertThat(DocConverter.jsonToYaml(input)).isEqualTo("{}");
		assertThat(DocConverter.csvToYaml(input)).isEqualTo("{}");
	}

	@ParameterizedTest
	@MethodSource("emptyDocuments")
	void xmlTargetTreatsEmptyInputAsEmptyRoot(String input) {
		assertEmptyXml(DocConverter.jsonToXml(input));
		assertEmptyXml(DocConverter.yamlToXml(input));
		assertEmptyXml(DocConverter.csvToXml(input));
	}

	@ParameterizedTest
	@MethodSource("emptyDocuments")
	void csvTargetTreatsEmptyInputAsEmptyString(String input) {
		assertThat(DocConverter.xmlToCsv(input)).isEmpty();
		assertThat(DocConverter.jsonToCsv(input)).isEmpty();
		assertThat(DocConverter.yamlToCsv(input)).isEmpty();
	}

	@Test
	void jsonEmptyObjectToXmlUsesNormalConversion() {
		String xml = DocConverter.jsonToXml("{}");
		assertThat(xml).isNotBlank();
		assertThat(stringToDoc(xml).getDocumentElement()).isNotNull();
	}

	@Test
	void jsonEmptyArrayToXmlUsesArrayListRoot() {
		String xml = DocConverter.jsonToXml("[]");
		assertThat(stringToDoc(xml).getDocumentElement().getTagName()).isEqualTo("ArrayList");
	}

	@Test
	void xmlEmptyRootToJsonIsNotCanonicalEmptyJson() {
		String json = DocConverter.xmlToJson("<root/>");
		assertThat(json).isNotEqualTo("{}").contains("root");
	}

	@Test
	void xmlToJsonThrowsOnMalformedXml() {
		assertThatThrownBy(() -> DocConverter.xmlToJson("<unclosed>"))
				.isInstanceOf(DocConversionException.class)
				.hasMessage("Failed to convert XML to JSON")
				.hasCauseInstanceOf(Exception.class);
	}

	@Test
	void jsonToXmlThrowsOnMalformedJson() {
		assertThatThrownBy(() -> DocConverter.jsonToXml("{bad json"))
				.isInstanceOf(DocConversionException.class)
				.hasMessage("Failed to convert JSON to XML")
				.hasCauseInstanceOf(Exception.class);
	}

	@Test
	void yamlToJsonThrowsOnMalformedYaml() {
		String badYaml = """
						key: value
						  invalid_indentation: true""";

		assertThatThrownBy(() -> DocConverter.yamlToJson(badYaml))
				.isInstanceOf(DocConversionException.class)
				.hasMessage("Failed to convert YAML to JSON")
				.hasCauseInstanceOf(Exception.class);
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

	private static Stream<String> emptyDocuments() {
		return Stream.of(null, "", "   ");
	}

	private static void assertEmptyXml(String xml) {
		XmlAssert.assertThat(Input.fromString(xml))
				.and(Input.fromString("<root/>"))
				.ignoreWhitespace()
				.areSimilar();
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
